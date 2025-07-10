package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.change.ColumnConfig;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.ObjectQuotingStrategy;
import io.github.shshdxk.liquibase.database.core.OracleDatabase;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sql.UnparsedSql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.statement.core.SelectFromDatabaseChangeLogLockStatement;
import io.github.shshdxk.liquibase.util.StringUtil;

public class SelectFromDatabaseChangeLogLockGenerator extends AbstractSqlGenerator<SelectFromDatabaseChangeLogLockStatement> {

    @Override
    public ValidationErrors validate(SelectFromDatabaseChangeLogLockStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors errors = new ValidationErrors();
        errors.checkRequiredField("columnToSelect", statement.getColumnsToSelect());

        return errors;
    }

    @Override
    public Sql[] generateSql(SelectFromDatabaseChangeLogLockStatement statement, final Database database, SqlGeneratorChain sqlGeneratorChain) {
    	String liquibaseSchema;
   		liquibaseSchema = database.getLiquibaseSchemaName();

        // use LEGACY quoting since we're dealing with system objects
        ObjectQuotingStrategy currentStrategy = database.getObjectQuotingStrategy();
        database.setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY);
        try {
            String sql = "SELECT " + StringUtil.join(statement.getColumnsToSelect(), ",", (StringUtil.StringUtilFormatter<ColumnConfig>) col -> {
                if ((col.getComputed() != null) && col.getComputed()) {
                    return col.getName();
                } else {
                    return database.escapeColumnName(null, null, null, col.getName());
                }
            }) + " FROM " +
                    database.escapeTableName(database.getLiquibaseCatalogName(), liquibaseSchema, database.getDatabaseChangeLogLockTableName()) +
                " WHERE " + database.escapeColumnName(database.getLiquibaseCatalogName(), liquibaseSchema, database.getDatabaseChangeLogLockTableName(), "ID") + "=1";

            if (database instanceof OracleDatabase) {
                sql += " FOR UPDATE";
            }
            return new Sql[] {
                    new UnparsedSql(sql)
            };
        } finally {
            database.setObjectQuotingStrategy(currentStrategy);
        }
    }
}
