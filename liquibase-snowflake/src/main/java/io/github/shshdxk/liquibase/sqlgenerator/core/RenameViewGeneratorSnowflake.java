package io.github.shshdxk.liquibase.sqlgenerator.core;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.SnowflakeDatabase;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sql.UnparsedSql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.statement.core.RenameViewStatement;

public class RenameViewGeneratorSnowflake extends RenameViewGenerator{
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(RenameViewStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public Sql[] generateSql(RenameViewStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[] {
                new UnparsedSql(
                        "ALTER VIEW "
                                + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(),
                                statement.getOldViewName())
                                + " RENAME TO "
                                + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(),
                                statement.getNewViewName()),
                        getAffectedOldView(statement), getAffectedNewView(statement)) };
    }

}
