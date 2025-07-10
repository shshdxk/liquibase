package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.SnowflakeDatabase;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sql.UnparsedSql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.statement.core.DropDefaultValueStatement;

public class DropDefaultValueGeneratorSnowflake extends DropDefaultValueGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(DropDefaultValueStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public Sql[] generateSql(DropDefaultValueStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[]{
                new UnparsedSql(
                        "ALTER TABLE "
                                + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())
                                + " ALTER COLUMN "
                                + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(),
                                statement.getTableName(), statement.getColumnName())
                                + " DROP DEFAULT")};
    }

}
