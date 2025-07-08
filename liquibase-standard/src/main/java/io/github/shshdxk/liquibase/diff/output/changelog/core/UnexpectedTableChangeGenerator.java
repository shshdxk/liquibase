package io.github.shshdxk.liquibase.diff.output.changelog.core;

import io.github.shshdxk.liquibase.change.Change;
import io.github.shshdxk.liquibase.change.core.DropTableChange;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.diff.output.DiffOutputControl;
import io.github.shshdxk.liquibase.diff.output.changelog.AbstractChangeGenerator;
import io.github.shshdxk.liquibase.diff.output.changelog.ChangeGeneratorChain;
import io.github.shshdxk.liquibase.diff.output.changelog.UnexpectedObjectChangeGenerator;
import io.github.shshdxk.liquibase.structure.DatabaseObject;
import io.github.shshdxk.liquibase.structure.core.Column;
import io.github.shshdxk.liquibase.structure.core.Index;
import io.github.shshdxk.liquibase.structure.core.PrimaryKey;
import io.github.shshdxk.liquibase.structure.core.Table;

public class UnexpectedTableChangeGenerator extends AbstractChangeGenerator implements UnexpectedObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Table.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return null;
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return new Class[] {Column.class, PrimaryKey.class};
    }

    @Override
    public Change[] fixUnexpected(DatabaseObject unexpectedObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        Table unexpectedTable = (Table) unexpectedObject;

        DropTableChange change = new DropTableChange();
        change.setTableName(unexpectedTable.getName());
        if (control.getIncludeCatalog()) {
            change.setCatalogName(unexpectedTable.getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            change.setSchemaName(unexpectedTable.getSchema().getName());
        }

        for (Column column : unexpectedTable.getColumns()) {
            control.setAlreadyHandledUnexpected(column);
        }
        control.setAlreadyHandledUnexpected(unexpectedTable.getPrimaryKey());

        for (Index index : unexpectedTable.getIndexes()) {
            control.setAlreadyHandledUnexpected(index);
        }
        control.setAlreadyHandledUnexpected(unexpectedTable.getPrimaryKey());
        if (unexpectedTable.getPrimaryKey() != null) {
            control.setAlreadyHandledUnexpected(unexpectedTable.getPrimaryKey().getBackingIndex());
        }

        return new Change[] { change };

    }
}
