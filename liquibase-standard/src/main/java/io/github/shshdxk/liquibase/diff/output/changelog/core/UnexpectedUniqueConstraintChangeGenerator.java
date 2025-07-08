package io.github.shshdxk.liquibase.diff.output.changelog.core;

import io.github.shshdxk.liquibase.change.Change;
import io.github.shshdxk.liquibase.change.core.DropUniqueConstraintChange;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.diff.output.DiffOutputControl;
import io.github.shshdxk.liquibase.diff.output.changelog.AbstractChangeGenerator;
import io.github.shshdxk.liquibase.diff.output.changelog.ChangeGeneratorChain;
import io.github.shshdxk.liquibase.diff.output.changelog.UnexpectedObjectChangeGenerator;
import io.github.shshdxk.liquibase.structure.DatabaseObject;
import io.github.shshdxk.liquibase.structure.core.Index;
import io.github.shshdxk.liquibase.structure.core.Table;
import io.github.shshdxk.liquibase.structure.core.UniqueConstraint;

public class UnexpectedUniqueConstraintChangeGenerator extends AbstractChangeGenerator implements UnexpectedObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (UniqueConstraint.class.isAssignableFrom(objectType)) {
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
        return new Class[] {
                Table.class,
                Index.class
        };
    }

    @Override
    public Change[] fixUnexpected(DatabaseObject unexpectedObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        UniqueConstraint uc = (UniqueConstraint) unexpectedObject;
        if (uc.getRelation() == null) {
            return null;
        }

        DropUniqueConstraintChange change = new DropUniqueConstraintChange();
        change.setTableName(uc.getRelation().getName());
        if (control.getIncludeCatalog()) {
            change.setCatalogName(uc.getRelation().getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            change.setSchemaName(uc.getRelation().getSchema().getName());
        }
        change.setConstraintName(uc.getName());

        Index backingIndex = uc.getBackingIndex();
//        if (backingIndex == null) {
//            Index exampleIndex = new Index().setTable(uc.getTable());
//            for (String col : uc.getColumns()) {
//                exampleIndex.getColumns().add(col);
//            }
//            control.setAlreadyHandledUnexpected(exampleIndex);
//        } else {
            control.setAlreadyHandledUnexpected(backingIndex);
//        }

        return new Change[] { change };
    }
}
