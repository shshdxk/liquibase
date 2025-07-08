package io.github.shshdxk.liquibase.diff.output.changelog.core;

import io.github.shshdxk.liquibase.change.Change;
import io.github.shshdxk.liquibase.change.core.DropIndexChange;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.diff.output.DiffOutputControl;
import io.github.shshdxk.liquibase.diff.output.changelog.AbstractChangeGenerator;
import io.github.shshdxk.liquibase.diff.output.changelog.ChangeGeneratorChain;
import io.github.shshdxk.liquibase.diff.output.changelog.UnexpectedObjectChangeGenerator;
import io.github.shshdxk.liquibase.structure.DatabaseObject;
import io.github.shshdxk.liquibase.structure.core.ForeignKey;
import io.github.shshdxk.liquibase.structure.core.Index;

public class UnexpectedIndexChangeGenerator extends AbstractChangeGenerator implements UnexpectedObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Index.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return new Class[] {
                ForeignKey.class
        };
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return null;
    }

    @Override
    public Change[] fixUnexpected(DatabaseObject unexpectedObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        Index index = (Index) unexpectedObject;

//        if (index.getAssociatedWith().contains(Index.MARK_PRIMARY_KEY) || index.getAssociatedWith().contains(Index.MARK_FOREIGN_KEY) || index.getAssociatedWith().contains(Index.MARK_UNIQUE_CONSTRAINT)) {
//            return null;
//        }

        DropIndexChange change = new DropIndexChange();
        change.setTableName(index.getRelation().getName());
        if (control.getIncludeCatalog()) {
            change.setCatalogName(index.getRelation().getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            change.setSchemaName(index.getRelation().getSchema().getName());
        }
        change.setIndexName(index.getName());
        change.setAssociatedWith(index.getAssociatedWithAsString());

        return new Change[] { change };

    }
}
