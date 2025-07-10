package io.github.shshdxk.liquibase.diff.output.changelog.core;

import io.github.shshdxk.liquibase.change.Change;
import io.github.shshdxk.liquibase.change.core.AddPrimaryKeyChange;
import io.github.shshdxk.liquibase.change.core.DropPrimaryKeyChange;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.OracleDatabase;
import io.github.shshdxk.liquibase.diff.Difference;
import io.github.shshdxk.liquibase.diff.ObjectDifferences;
import io.github.shshdxk.liquibase.diff.output.DiffOutputControl;
import io.github.shshdxk.liquibase.diff.output.changelog.AbstractChangeGenerator;
import io.github.shshdxk.liquibase.diff.output.changelog.ChangeGeneratorChain;
import io.github.shshdxk.liquibase.diff.output.changelog.ChangeGeneratorFactory;
import io.github.shshdxk.liquibase.diff.output.changelog.ChangedObjectChangeGenerator;
import io.github.shshdxk.liquibase.structure.DatabaseObject;
import io.github.shshdxk.liquibase.structure.core.*;
import io.github.shshdxk.liquibase.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.github.shshdxk.liquibase.structure.core.PrimaryKey.CLUSTERED_ATTRIBUTE;
import static io.github.shshdxk.liquibase.structure.core.PrimaryKey.VALIDATE_ATRIBUTE;

public class ChangedPrimaryKeyChangeGenerator extends AbstractChangeGenerator implements ChangedObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (PrimaryKey.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return new Class[] {Index.class, UniqueConstraint.class };
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return null;
    }

    @Override
    public Change[] fixChanged(DatabaseObject changedObject, ObjectDifferences differences, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {

        removeInvalidDifferences(differences, referenceDatabase, comparisonDatabase);

        if (!differences.hasDifferences()) {
            return EMPTY_CHANGE;
        }

        PrimaryKey pk = (PrimaryKey) changedObject;

        List<Change> returnList = new ArrayList<>();


        DropPrimaryKeyChange dropPkChange = new DropPrimaryKeyChange();
        dropPkChange.setTableName(pk.getTable().getName());
        returnList.add(dropPkChange);

        AddPrimaryKeyChange addPkChange = new AddPrimaryKeyChange();
        addPkChange.setTableName(pk.getTable().getName());
        addPkChange.setColumnNames(pk.getColumnNames());
        addPkChange.setConstraintName(pk.getName());

        if (comparisonDatabase instanceof OracleDatabase) {
            Index backingIndex = pk.getBackingIndex();
            if ((backingIndex != null) && (backingIndex.getName() != null)) {
                Change[] indexChanges = ChangeGeneratorFactory.getInstance().fixMissing(backingIndex, control, referenceDatabase, comparisonDatabase);
                if (indexChanges != null) {
                    returnList.addAll(Arrays.asList(indexChanges));
                }

                addPkChange.setForIndexName(backingIndex.getName());
                Schema schema = backingIndex.getSchema();
                if (schema != null) {
                    if (control.getIncludeCatalog()) {
                        addPkChange.setForIndexCatalogName(schema.getCatalogName());
                    }
                    if (control.getIncludeSchema()) {
                        addPkChange.setForIndexSchemaName(schema.getName());
                    }
                }
            }
        }
        returnList.add(addPkChange);

        if (control.getIncludeCatalog()) {
            dropPkChange.setCatalogName(pk.getSchema().getCatalogName());
            addPkChange.setCatalogName(pk.getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            dropPkChange.setSchemaName(pk.getSchema().getName());
            addPkChange.setSchemaName(pk.getSchema().getName());
        }

        Difference columnDifferences = differences.getDifference("columns");
        List<Column> referenceColumns;
        List<Column> comparedColumns;
        if (columnDifferences == null) {
            referenceColumns = pk.getColumns();
            comparedColumns = pk.getColumns();
        } else {
            referenceColumns = (List<Column>) columnDifferences.getReferenceValue();
            comparedColumns = (List<Column>) columnDifferences.getComparedValue();
        }

        StringUtil.ToStringFormatter formatter = new StringUtil.ToStringFormatter();

        control.setAlreadyHandledChanged(new Index().setRelation(pk.getTable()).setColumns(referenceColumns));
        if (!StringUtil.join(referenceColumns, ",", formatter).equalsIgnoreCase(StringUtil.join(comparedColumns, ",", formatter))) {
            control.setAlreadyHandledChanged(new Index().setRelation(pk.getTable()).setColumns(comparedColumns));
        }

        control.setAlreadyHandledChanged(new UniqueConstraint().setRelation(pk.getTable()).setColumns(referenceColumns));
        if (!StringUtil.join(referenceColumns, ",", formatter).equalsIgnoreCase(StringUtil.join(comparedColumns, "," , formatter))) {
            control.setAlreadyHandledChanged(new UniqueConstraint().setRelation(pk.getTable()).setColumns(comparedColumns));
        }

        return returnList.toArray(EMPTY_CHANGE);
    }

    private void removeInvalidDifferences(ObjectDifferences differences, Database referenceDatabase, Database comparisonDatabase) {
        //don't try to recreate PKs that differ in just clustered
        Difference clusteredDiff = differences.getDifference(CLUSTERED_ATTRIBUTE);
        if (clusteredDiff != null && ((clusteredDiff.getReferenceValue() == null) || (clusteredDiff.getComparedValue() == null))) {
            differences.removeDifference(CLUSTERED_ATTRIBUTE);
        }

        // as only oracle supports PK validate, we will use it only for Oracle vs Oracle comparisons
        if (differences.getDifference(VALIDATE_ATRIBUTE) != null &&
            (!(comparisonDatabase instanceof OracleDatabase) || (!(referenceDatabase instanceof OracleDatabase)))) {
            differences.removeDifference(VALIDATE_ATRIBUTE);
        }
    }
}
