package io.github.shshdxk.liquibase.diff.output.changelog.core;

import io.github.shshdxk.liquibase.Scope;
import io.github.shshdxk.liquibase.change.Change;
import io.github.shshdxk.liquibase.change.core.AddUniqueConstraintChange;
import io.github.shshdxk.liquibase.database.AbstractJdbcDatabase;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.MSSQLDatabase;
import io.github.shshdxk.liquibase.database.core.OracleDatabase;
import io.github.shshdxk.liquibase.diff.DiffResult;
import io.github.shshdxk.liquibase.diff.output.DiffOutputControl;
import io.github.shshdxk.liquibase.diff.output.changelog.AbstractChangeGenerator;
import io.github.shshdxk.liquibase.diff.output.changelog.ChangeGeneratorChain;
import io.github.shshdxk.liquibase.diff.output.changelog.ChangeGeneratorFactory;
import io.github.shshdxk.liquibase.diff.output.changelog.MissingObjectChangeGenerator;
import io.github.shshdxk.liquibase.snapshot.SnapshotGeneratorFactory;
import io.github.shshdxk.liquibase.structure.DatabaseObject;
import io.github.shshdxk.liquibase.structure.core.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MissingUniqueConstraintChangeGenerator extends AbstractChangeGenerator implements MissingObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (UniqueConstraint.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return new Class[]{
                Table.class,
                Column.class
        };
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return new Class[]{Index.class};
    }

    @Override
    public Change[] fixMissing(DatabaseObject missingObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        List<Change> returnList = new ArrayList<>();

        UniqueConstraint uc = (UniqueConstraint) missingObject;

        if (uc.getRelation() == null) {
            return null;
        }

        AddUniqueConstraintChange change = createAddUniqueConstraintChange();
        change.setTableName(uc.getRelation().getName());
        if ((uc.getBackingIndex() != null) && control.getIncludeTablespace()) {
            change.setTablespace(uc.getBackingIndex().getTablespace());
        }
        if (control.getIncludeCatalog()) {
            change.setCatalogName(uc.getRelation().getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            change.setSchemaName(uc.getRelation().getSchema().getName());
        }
        change.setConstraintName(uc.getName());
        change.setColumnNames(uc.getColumnNames());
        change.setDeferrable(uc.isDeferrable() ? Boolean.TRUE : null);
        change.setValidate(!uc.shouldValidate() ? Boolean.FALSE : null);
        change.setInitiallyDeferred(uc.isInitiallyDeferred() ? Boolean.TRUE : null);
        change.setDisabled(uc.isDisabled() ? Boolean.TRUE : null);
        if (referenceDatabase instanceof MSSQLDatabase) {
            change.setClustered(uc.isClustered() ? Boolean.TRUE : null);
        }

        if (comparisonDatabase instanceof OracleDatabase) {
            Index backingIndex = uc.getBackingIndex();
            if ((backingIndex != null) && (backingIndex.getName() != null)) {
                if (referenceDatabase.equals(comparisonDatabase) || !alreadyExists(backingIndex, comparisonDatabase, control)) {
                    Change[] changes = ChangeGeneratorFactory.getInstance().fixMissing(backingIndex, control, referenceDatabase, comparisonDatabase);
                    if (changes != null) {
                        returnList.addAll(Arrays.asList(changes));

                        change.setForIndexName(backingIndex.getName());
                        Schema schema = backingIndex.getSchema();
                        if (schema != null) {
                            if (control.getIncludeCatalog()) {
                                change.setForIndexCatalogName(schema.getCatalogName());
                            }
                            if (control.getIncludeSchema()) {
                                change.setForIndexSchemaName(schema.getName());
                            }
                        }
                    }
                }
            }
        }


        Index backingIndex = uc.getBackingIndex();
//        if (backingIndex == null) {
//            Index exampleIndex = new Index().setTable(uc.getTable());
//            for (String col : uc.getColumns()) {
//                exampleIndex.getColumns().add(col);
//            }
//            control.setAlreadyHandledMissing(exampleIndex);
//        } else {
            control.setAlreadyHandledMissing(backingIndex);
//        }

        returnList.add(change);

        return returnList.toArray(EMPTY_CHANGE);


    }

    private boolean alreadyExists(Index backingIndex, Database comparisonDatabase, DiffOutputControl control) {
        boolean found = false;
        try {
            String catalogName = null;
            String schemaName = null;
            if (control.getIncludeCatalog()) {
                catalogName = backingIndex.getTable().getSchema().getCatalogName();
            }
            if (control.getIncludeSchema()) {
                schemaName = backingIndex.getTable().getSchema().getName();
            }

            Index backingIndexCopy = new Index(backingIndex.getName(), catalogName, schemaName, backingIndex.getTable().getName());
            for (Column column : backingIndex.getColumns()) {
                backingIndexCopy.addColumn(column);
            }

            // get the diffResult from the database object
            // This was set from DiffToChangeLog#generateChangeSets() so that we can access it here
            DiffResult diffResult = null;
            if (comparisonDatabase instanceof AbstractJdbcDatabase) {
                diffResult = (DiffResult) ((AbstractJdbcDatabase) comparisonDatabase).get("diffResult");
            }

            if (diffResult != null) {
                // check against the snapshot (better performance)
                Index foundIndex = diffResult.getComparisonSnapshot().get(backingIndexCopy);
                found = foundIndex != null;
            } else {
                // go to the db to find out
                found = SnapshotGeneratorFactory.getInstance().has(backingIndexCopy, comparisonDatabase);
            }
        } catch (Exception e) {
            Scope.getCurrentScope().getLog(getClass()).warning("Error checking for backing index "+backingIndex.toString()+": "+e.getMessage(), e);
        }
        return found;
    }

    protected AddUniqueConstraintChange createAddUniqueConstraintChange() {
        return new AddUniqueConstraintChange();
    }
}
