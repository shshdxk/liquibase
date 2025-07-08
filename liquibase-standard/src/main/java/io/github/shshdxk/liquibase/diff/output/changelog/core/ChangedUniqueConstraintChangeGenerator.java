package io.github.shshdxk.liquibase.diff.output.changelog.core;

import io.github.shshdxk.liquibase.change.Change;
import io.github.shshdxk.liquibase.change.core.AddUniqueConstraintChange;
import io.github.shshdxk.liquibase.change.core.DropUniqueConstraintChange;
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
import io.github.shshdxk.liquibase.structure.core.Index;
import io.github.shshdxk.liquibase.structure.core.Schema;
import io.github.shshdxk.liquibase.structure.core.UniqueConstraint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChangedUniqueConstraintChangeGenerator extends AbstractChangeGenerator implements ChangedObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (UniqueConstraint.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return new Class[]{Index.class};
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return null;
    }

    @Override
    public Change[] fixChanged(DatabaseObject changedObject, ObjectDifferences differences, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        List<Change> returnList = new ArrayList<>();

        UniqueConstraint uniqueConstraint = (UniqueConstraint) changedObject;

        Difference dropName = differences.getDifference("name");

        DropUniqueConstraintChange dropUniqueConstraintChange = createDropUniqueConstraintChange();
        dropUniqueConstraintChange.setTableName(uniqueConstraint.getRelation().getName());
        dropUniqueConstraintChange.setConstraintName(dropName == null ? uniqueConstraint.getName() : dropName.getComparedValue().toString());

        AddUniqueConstraintChange addUniqueConstraintChange = createAddUniqueConstraintChange();
        addUniqueConstraintChange.setConstraintName(uniqueConstraint.getName());
        addUniqueConstraintChange.setTableName(uniqueConstraint.getRelation().getName());
        addUniqueConstraintChange.setColumnNames(uniqueConstraint.getColumnNames());

        returnList.add(dropUniqueConstraintChange);

        if (control.getIncludeCatalog()) {
            dropUniqueConstraintChange.setCatalogName(uniqueConstraint.getSchema().getCatalogName());
            addUniqueConstraintChange.setCatalogName(uniqueConstraint.getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            dropUniqueConstraintChange.setSchemaName(uniqueConstraint.getSchema().getName());
            addUniqueConstraintChange.setSchemaName(uniqueConstraint.getSchema().getName());
        }

        Index backingIndex = uniqueConstraint.getBackingIndex();
        if (comparisonDatabase instanceof OracleDatabase) {
            if ((backingIndex != null) && (backingIndex.getName() != null)) {
                Change[] missingIndexChanges = ChangeGeneratorFactory.getInstance().fixMissing(backingIndex, control, referenceDatabase, comparisonDatabase);
                if (missingIndexChanges != null) {
                    returnList.addAll(Arrays.asList(missingIndexChanges));
                }

                addUniqueConstraintChange.setForIndexName(backingIndex.getName());
                Schema schema = backingIndex.getSchema();
                if (schema != null) {
                    if (control.getIncludeCatalog()) {
                        addUniqueConstraintChange.setForIndexCatalogName(schema.getCatalogName());
                    }
                    if (control.getIncludeSchema()) {
                        addUniqueConstraintChange.setForIndexSchemaName(schema.getName());
                    }
                }
            }
        }

        control.setAlreadyHandledChanged(backingIndex);

        returnList.add(addUniqueConstraintChange);

        return returnList.toArray(EMPTY_CHANGE);
    }

    protected DropUniqueConstraintChange createDropUniqueConstraintChange() {
        return new DropUniqueConstraintChange();
    }

    protected AddUniqueConstraintChange createAddUniqueConstraintChange() {
        return new AddUniqueConstraintChange();
    }
}
