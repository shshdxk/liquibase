package io.github.shshdxk.liquibase.diff.output.changelog.core;

import io.github.shshdxk.liquibase.change.AddColumnConfig;
import io.github.shshdxk.liquibase.change.Change;
import io.github.shshdxk.liquibase.change.core.CreateIndexChange;
import io.github.shshdxk.liquibase.change.core.DropIndexChange;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.diff.Difference;
import io.github.shshdxk.liquibase.diff.ObjectDifferences;
import io.github.shshdxk.liquibase.diff.compare.DatabaseObjectComparatorFactory;
import io.github.shshdxk.liquibase.diff.output.DiffOutputControl;
import io.github.shshdxk.liquibase.diff.output.changelog.AbstractChangeGenerator;
import io.github.shshdxk.liquibase.diff.output.changelog.ChangeGeneratorChain;
import io.github.shshdxk.liquibase.diff.output.changelog.ChangeGeneratorFactory;
import io.github.shshdxk.liquibase.diff.output.changelog.ChangedObjectChangeGenerator;
import io.github.shshdxk.liquibase.structure.DatabaseObject;
import io.github.shshdxk.liquibase.structure.core.Column;
import io.github.shshdxk.liquibase.structure.core.Index;
import io.github.shshdxk.liquibase.structure.core.Table;
import io.github.shshdxk.liquibase.structure.core.UniqueConstraint;
import io.github.shshdxk.liquibase.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class ChangedIndexChangeGenerator extends AbstractChangeGenerator implements ChangedObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Index.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return null;
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return null;
    }

    @Override
    public Change[] fixChanged(DatabaseObject changedObject, ObjectDifferences differences, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        //don't try to recreate indexes that differ in just clustered
        Difference clusteredDiff = differences.getDifference("clustered");
        if (clusteredDiff != null) {
            if ((clusteredDiff.getReferenceValue() == null) || (clusteredDiff.getComparedValue() == null)) {
                differences.removeDifference("clustered");
            }
        }

        for (String field : getIgnoredFields()) {
            differences.removeDifference(field);
        }

        if (!differences.hasDifferences()) {
            return EMPTY_CHANGE;
        }

        Index index = (Index) changedObject;

        if (index.getRelation() != null  && index.getRelation() instanceof Table) {
            if ((((Table) index.getRelation()).getPrimaryKey() != null) && DatabaseObjectComparatorFactory.getInstance()
                .isSameObject(((Table) index.getRelation()).getPrimaryKey().getBackingIndex(), changedObject, differences
                    .getSchemaComparisons(), comparisonDatabase)) {
                return ChangeGeneratorFactory.getInstance().fixChanged(((Table) index.getRelation()).getPrimaryKey(), differences, control, referenceDatabase, comparisonDatabase);
            }

            List<UniqueConstraint> uniqueConstraints = index.getRelation().getUniqueConstraints();
            if (uniqueConstraints != null) {
                for (UniqueConstraint constraint : uniqueConstraints) {
                    if ((constraint.getBackingIndex() != null) && DatabaseObjectComparatorFactory.getInstance()
                        .isSameObject(constraint.getBackingIndex(), changedObject, differences.getSchemaComparisons()
                            , comparisonDatabase)) {
                        return ChangeGeneratorFactory.getInstance().fixChanged(constraint, differences, control, referenceDatabase, comparisonDatabase);
                    }

                }
            }
        }

        DropIndexChange dropIndexChange = createDropIndexChange();
        dropIndexChange.setTableName(index.getRelation().getName());
        dropIndexChange.setIndexName(index.getName());

        CreateIndexChange addIndexChange = createCreateIndexChange();
        addIndexChange.setTableName(index.getRelation().getName());
        List<AddColumnConfig> columns = new ArrayList<>();
        for (Column col : index.getColumns()) {
            columns.add(new AddColumnConfig(col));
        }
        addIndexChange.setColumns(columns);
        addIndexChange.setIndexName(index.getName());
        addIndexChange.setUnique(index.isUnique());

        if (control.getIncludeCatalog()) {
            dropIndexChange.setCatalogName(index.getSchema().getCatalogName());
            addIndexChange.setCatalogName(index.getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            dropIndexChange.setSchemaName(index.getSchema().getName());
            addIndexChange.setSchemaName(index.getSchema().getName());
        }

        Difference columnsDifference = differences.getDifference("columns");

        if (columnsDifference != null) {
            List<Column> referenceColumns = (List<Column>) columnsDifference.getReferenceValue();
            List<Column> comparedColumns = (List<Column>) columnsDifference.getComparedValue();

            StringUtil.StringUtilFormatter<Column> formatter = obj -> obj.toString(false);

            control.setAlreadyHandledChanged(new Index().setRelation(index.getRelation()).setColumns(referenceColumns));
            if (!StringUtil.join(referenceColumns, ",", formatter).equalsIgnoreCase(StringUtil.join(comparedColumns, ",", formatter))) {
                control.setAlreadyHandledChanged(new Index().setRelation(index.getRelation()).setColumns(comparedColumns));
            }

            if ((index.isUnique() != null) && index.isUnique()) {
                control.setAlreadyHandledChanged(new UniqueConstraint().setRelation(index.getRelation()).setColumns(referenceColumns));
                if (!StringUtil.join(referenceColumns, ",", formatter).equalsIgnoreCase(StringUtil.join(comparedColumns, ",", formatter))) {
                    control.setAlreadyHandledChanged(new UniqueConstraint().setRelation(index.getRelation()).setColumns(comparedColumns));
                }
            }
        }

        return new Change[] { dropIndexChange, addIndexChange };
    }

    protected String[] getIgnoredFields() {
        return new String[] {
                "padIndex",
                "fillFactor",
                "ignoreDuplicateKeys",
                "recomputeStatistics",
                "incrementalStatistics",
                "allowRowLocks",
                "allowPageLocks",
                "dataCompression",
                "includedColumns"
        };
    }

    protected DropIndexChange createDropIndexChange() {
        return new DropIndexChange();
    }

    protected CreateIndexChange createCreateIndexChange() {
        return new CreateIndexChange();
    }
}
