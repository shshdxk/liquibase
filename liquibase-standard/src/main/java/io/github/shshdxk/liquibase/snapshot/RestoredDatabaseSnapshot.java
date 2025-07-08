package io.github.shshdxk.liquibase.snapshot;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.exception.DatabaseException;
import io.github.shshdxk.liquibase.structure.DatabaseObject;

public class RestoredDatabaseSnapshot extends DatabaseSnapshot {

    public RestoredDatabaseSnapshot(Database database) throws DatabaseException, InvalidExampleException {
        super(new DatabaseObject[0], database);
    }
}
