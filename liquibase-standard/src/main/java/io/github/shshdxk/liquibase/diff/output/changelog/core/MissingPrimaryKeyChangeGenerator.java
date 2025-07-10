package io.github.shshdxk.liquibase.diff.output.changelog.core;

import io.github.shshdxk.liquibase.CatalogAndSchema;
import io.github.shshdxk.liquibase.change.Change;
import io.github.shshdxk.liquibase.change.core.AddPrimaryKeyChange;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.AbstractDb2Database;
import io.github.shshdxk.liquibase.database.core.MSSQLDatabase;
import io.github.shshdxk.liquibase.database.core.OracleDatabase;
import io.github.shshdxk.liquibase.database.core.PostgresDatabase;
import io.github.shshdxk.liquibase.structure.core.*;
import io.github.shshdxk.liquibase.diff.output.DiffOutputControl;
import io.github.shshdxk.liquibase.diff.output.changelog.AbstractChangeGenerator;
import io.github.shshdxk.liquibase.diff.output.changelog.ChangeGeneratorChain;
import io.github.shshdxk.liquibase.diff.output.changelog.ChangeGeneratorFactory;
import io.github.shshdxk.liquibase.diff.output.changelog.MissingObjectChangeGenerator;
import io.github.shshdxk.liquibase.exception.UnexpectedLiquibaseException;
import io.github.shshdxk.liquibase.snapshot.SnapshotGeneratorFactory;
import io.github.shshdxk.liquibase.structure.DatabaseObject;

import java.util.ArrayList;
import java.util.List;

public class MissingPrimaryKeyChangeGenerator extends AbstractChangeGenerator implements MissingObjectChangeGenerator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (PrimaryKey.class.isAssignableFrom(objectType)) {
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
        return new Class[]{
                Index.class
        };
    }

    @Override
    public Change[] fixMissing(DatabaseObject missingObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        List<Change> returnList = new ArrayList<>();

        PrimaryKey pk = (PrimaryKey) missingObject;

        AddPrimaryKeyChange change = createAddPrimaryKeyChange();
        change.setTableName(pk.getTable().getName());
        if (control.getIncludeCatalog()) {
            change.setCatalogName(pk.getTable().getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            change.setSchemaName(pk.getTable().getSchema().getName());
        }
        change.setConstraintName(pk.getName());
        change.setColumnNames(pk.getColumnNames());
        if (control.getIncludeTablespace()) {
            change.setTablespace(pk.getTablespace());
        }

        if ((referenceDatabase instanceof MSSQLDatabase) && (pk.getBackingIndex() != null) && (pk.getBackingIndex()
            .getClustered() != null) && !pk.getBackingIndex().getClustered()) {
            change.setClustered(false);
        }
        if ((referenceDatabase instanceof PostgresDatabase) && (pk.getBackingIndex() != null) && (pk.getBackingIndex
            ().getClustered() != null) && pk.getBackingIndex().getClustered()) {
            change.setClustered(true);
        }

        if ((comparisonDatabase instanceof OracleDatabase) || ((comparisonDatabase instanceof AbstractDb2Database) && (pk
            .getBackingIndex() != null) && !comparisonDatabase.isSystemObject(pk.getBackingIndex()))) {
            Index backingIndex = pk.getBackingIndex();
            if ((backingIndex != null) && (backingIndex.getName() != null)) {
                // Save the original schema and catalog, so we can find it again if we need to examine diff results
                // after standard command execution
                Schema originalRelationSchema = backingIndex.getRelation().getSchema();
                try {
                    if (!control.getIncludeCatalog() && !control.getIncludeSchema()) {
                        // I'm not too sure what this is accomplishing
                        CatalogAndSchema schema = comparisonDatabase.getDefaultSchema().customize(comparisonDatabase);
                        backingIndex.getRelation().setSchema(schema.getCatalogName(), schema.getSchemaName()); //set table schema so it is found in the correct schema
                    }
                    if (referenceDatabase.equals(comparisonDatabase) || !SnapshotGeneratorFactory.getInstance().has(backingIndex, comparisonDatabase)) {
                        Change[] fixes = ChangeGeneratorFactory.getInstance().fixMissing(backingIndex, control, referenceDatabase, comparisonDatabase);

                        if (fixes != null) {
                            for (Change fix : fixes) {
                                if (fix != null) {
                                    returnList.add(fix);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    throw new UnexpectedLiquibaseException(e);
                }


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
                // We've generated our changes with the updated schema if needed, set it back to the original.
                backingIndex.getRelation().setSchema(originalRelationSchema);
            }
        }

        control.setAlreadyHandledMissing(pk.getBackingIndex());
        returnList.add(change);

        return returnList.toArray(EMPTY_CHANGE);

    }

    protected AddPrimaryKeyChange createAddPrimaryKeyChange() {
        return new AddPrimaryKeyChange();
    }

}
