package io.github.shshdxk.liquibase.database;

import io.github.shshdxk.liquibase.Scope;
import io.github.shshdxk.liquibase.changelog.ChangeLogHistoryServiceFactory;
import io.github.shshdxk.liquibase.exception.DatabaseException;
import io.github.shshdxk.liquibase.lockservice.LockServiceFactory;

import java.util.Arrays;
import java.util.List;

public class StandardLiquibaseTableNames implements LiquibaseTableNames {
    @Override
    public List<String> getLiquibaseGeneratedTableNames(Database database) {
        return Arrays.asList(database.getDatabaseChangeLogTableName(), database.getDatabaseChangeLogLockTableName());
    }

    @Override
    public void destroy(Database database) throws DatabaseException {
        Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(database).destroy();
        LockServiceFactory.getInstance().getLockService(database).destroy();
    }

    @Override
    public int getOrder() {
        // We always want this to run last.
        return Integer.MAX_VALUE;
    }
}
