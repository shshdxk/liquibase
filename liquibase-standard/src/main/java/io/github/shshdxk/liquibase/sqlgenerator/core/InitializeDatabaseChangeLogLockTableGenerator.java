package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.ObjectQuotingStrategy;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorFactory;
import io.github.shshdxk.liquibase.statement.core.DeleteStatement;
import io.github.shshdxk.liquibase.statement.core.InitializeDatabaseChangeLogLockTableStatement;
import io.github.shshdxk.liquibase.statement.core.InsertStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InitializeDatabaseChangeLogLockTableGenerator extends AbstractSqlGenerator<InitializeDatabaseChangeLogLockTableStatement> {

    @Override
    public ValidationErrors validate(InitializeDatabaseChangeLogLockTableStatement initializeDatabaseChangeLogLockTableStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    @Override
    public Sql[] generateSql(InitializeDatabaseChangeLogLockTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ObjectQuotingStrategy currentStrategy = database.getObjectQuotingStrategy();
        database.setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY);
        try {
            DeleteStatement deleteStatement = new DeleteStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName());
            InsertStatement insertStatement = new InsertStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName())
                    .addColumnValue("ID", 1)
                    .addColumnValue("LOCKED", Boolean.FALSE);

            List<Sql> sql = new ArrayList<>();

            sql.addAll(Arrays.asList(SqlGeneratorFactory.getInstance().generateSql(deleteStatement, database)));
            sql.addAll(Arrays.asList(SqlGeneratorFactory.getInstance().generateSql(insertStatement, database)));

            return sql.toArray(EMPTY_SQL);
        } finally {
            database.setObjectQuotingStrategy(currentStrategy);
        }
    }
}
