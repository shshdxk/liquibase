package io.github.shshdxk.liquibase.diff.output.changelog.core;

import io.github.shshdxk.liquibase.change.Change;
import io.github.shshdxk.liquibase.change.core.AddForeignKeyConstraintChange;
import io.github.shshdxk.liquibase.change.core.DropForeignKeyConstraintChange;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.diff.Difference;
import io.github.shshdxk.liquibase.diff.ObjectDifferences;
import io.github.shshdxk.liquibase.diff.output.DiffOutputControl;
import io.github.shshdxk.liquibase.diff.output.changelog.AbstractChangeGenerator;
import io.github.shshdxk.liquibase.diff.output.changelog.ChangeGeneratorChain;
import io.github.shshdxk.liquibase.diff.output.changelog.ChangedObjectChangeGenerator;
import io.github.shshdxk.liquibase.structure.DatabaseObject;
import io.github.shshdxk.liquibase.structure.core.Column;
import io.github.shshdxk.liquibase.structure.core.ForeignKey;
import io.github.shshdxk.liquibase.structure.core.Index;
import io.github.shshdxk.liquibase.structure.core.UniqueConstraint;
import io.github.shshdxk.liquibase.util.StringUtil;

public class ChangedForeignKeyChangeGenerator extends AbstractChangeGenerator implements ChangedObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (ForeignKey.class.isAssignableFrom(objectType)) {
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
        ForeignKey fk = (ForeignKey) changedObject;

        StringUtil.StringUtilFormatter<Column> formatter = obj -> obj.toString(false);

        Difference dropName = differences.getDifference("name");

        DropForeignKeyConstraintChange dropFkChange = new DropForeignKeyConstraintChange();
//        dropFkChange.setConstraintName(fk.getName());
        dropFkChange.setConstraintName(dropName == null ? fk.getName() : dropName.getComparedValue().toString());
        dropFkChange.setBaseTableName(fk.getForeignKeyTable().getName());

        AddForeignKeyConstraintChange addFkChange = new AddForeignKeyConstraintChange();
        addFkChange.setConstraintName(fk.getName());
        addFkChange.setBaseTableName(fk.getForeignKeyTable().getName());
        addFkChange.setBaseColumnNames(StringUtil.join(fk.getForeignKeyColumns(), ",", formatter));
        addFkChange.setReferencedTableName(fk.getPrimaryKeyTable().getName());
        addFkChange.setReferencedColumnNames(StringUtil.join(fk.getPrimaryKeyColumns(), ",", formatter));
        addFkChange.setOnDelete(fk.getDeleteRule());
        addFkChange.setOnUpdate(fk.getUpdateRule());

        if (control.getIncludeCatalog()) {
            dropFkChange.setBaseTableCatalogName(fk.getForeignKeyTable().getSchema().getCatalogName());

            addFkChange.setBaseTableCatalogName(fk.getForeignKeyTable().getSchema().getCatalogName());
            addFkChange.setReferencedTableCatalogName(fk.getPrimaryKeyTable().getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            dropFkChange.setBaseTableSchemaName(fk.getForeignKeyTable().getSchema().getName());

            addFkChange.setBaseTableSchemaName(fk.getForeignKeyTable().getSchema().getName());
            addFkChange.setReferencedTableSchemaName(fk.getPrimaryKeyTable().getSchema().getName());
        }

        if (fk.getBackingIndex() != null) {
            control.setAlreadyHandledChanged(fk.getBackingIndex());
        }

        return new Change[] { dropFkChange, addFkChange };
    }
}
