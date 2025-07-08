package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.SnowflakeDatabase;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sql.UnparsedSql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.statement.core.RenameTableStatement;

public class RenameTableGeneratorSnowflake extends RenameTableGenerator{
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(RenameTableStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public Sql[] generateSql(RenameTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[] {
                new UnparsedSql(
                        "ALTER TABLE "
                                + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(),
                                statement.getOldTableName())
                                + " RENAME TO "
                                + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(),
                                statement.getNewTableName()),
                        getAffectedOldTable(statement), getAffectedNewTable(statement)) };
    }
}
