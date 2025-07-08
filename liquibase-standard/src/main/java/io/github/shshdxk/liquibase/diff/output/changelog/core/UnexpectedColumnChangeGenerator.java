package io.github.shshdxk.liquibase.diff.output.changelog.core;

import io.github.shshdxk.liquibase.change.Change;
import io.github.shshdxk.liquibase.change.core.DropColumnChange;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.Db2zDatabase;
import io.github.shshdxk.liquibase.diff.output.DiffOutputControl;
import io.github.shshdxk.liquibase.diff.output.changelog.AbstractChangeGenerator;
import io.github.shshdxk.liquibase.diff.output.changelog.ChangeGeneratorChain;
import io.github.shshdxk.liquibase.diff.output.changelog.UnexpectedObjectChangeGenerator;
import io.github.shshdxk.liquibase.structure.DatabaseObject;
import io.github.shshdxk.liquibase.structure.core.*;
import io.github.shshdxk.liquibase.util.BooleanUtil;

public class UnexpectedColumnChangeGenerator extends AbstractChangeGenerator implements UnexpectedObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Column.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return new Class[] {
                PrimaryKey.class,
                ForeignKey.class,
                Table.class,
        };
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return null;
    }

    @Override
    public Change[] fixUnexpected(DatabaseObject unexpectedObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        Column column = (Column) unexpectedObject;
//        if (!shouldModifyColumn(column)) {
//            continue;
//        }

        if (BooleanUtil.isTrue(column.getComputed()) || BooleanUtil.isTrue(column.getDescending()) ) { //not really a column to drop, probably part of an index or something
            return null;
        }
        if (column.getRelation() instanceof View) {
            return null;
        }

        if (column.getRelation().getSnapshotId() == null) { //not an actual table, maybe an alias, maybe in a different schema. Don't fix it.
            return null;
        }

        if (comparisonDatabase instanceof Db2zDatabase) { //Db2 zOS column drop is handled by table change
            return null;
        }

        DropColumnChange change = new DropColumnChange();
        change.setTableName(column.getRelation().getName());
        if (control.getIncludeCatalog()) {
            change.setCatalogName(column.getRelation().getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            change.setSchemaName(column.getRelation().getSchema().getName());
        }
        change.setColumnName(column.getName());

        return new Change[] { change };

    }
}
