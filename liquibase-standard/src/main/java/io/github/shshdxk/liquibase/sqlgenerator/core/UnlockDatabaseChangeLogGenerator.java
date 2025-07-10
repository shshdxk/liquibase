package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.ObjectQuotingStrategy;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorFactory;
import io.github.shshdxk.liquibase.statement.core.UnlockDatabaseChangeLogStatement;
import io.github.shshdxk.liquibase.statement.core.UpdateStatement;

public class UnlockDatabaseChangeLogGenerator extends AbstractSqlGenerator<UnlockDatabaseChangeLogStatement> {

    @Override
    public ValidationErrors validate(UnlockDatabaseChangeLogStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    @Override
    public Sql[] generateSql(UnlockDatabaseChangeLogStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
    	String liquibaseSchema = database.getLiquibaseSchemaName();
        ObjectQuotingStrategy currentStrategy = database.getObjectQuotingStrategy();
        database.setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY);
        try {
            UpdateStatement releaseStatement = new UpdateStatement(database.getLiquibaseCatalogName(), liquibaseSchema, database.getDatabaseChangeLogLockTableName());
            releaseStatement.addNewColumnValue("LOCKED", false);
            releaseStatement.addNewColumnValue("LOCKGRANTED", null);
            releaseStatement.addNewColumnValue("LOCKEDBY", null);
            releaseStatement.setWhereClause(database.escapeColumnName(database.getLiquibaseCatalogName(), liquibaseSchema, database.getDatabaseChangeLogTableName(), "ID")+" = 1");

            return SqlGeneratorFactory.getInstance().generateSql(releaseStatement, database);
        } finally {
            database.setObjectQuotingStrategy(currentStrategy);
        }
    }
}
