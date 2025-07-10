package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.H2Database;
import io.github.shshdxk.liquibase.database.core.HsqlDatabase;
import io.github.shshdxk.liquibase.datatype.DataTypeFactory;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sql.UnparsedSql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.statement.core.AddAutoIncrementStatement;

public class AddAutoIncrementGeneratorHsqlH2 extends AddAutoIncrementGenerator {

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddAutoIncrementStatement statement, Database database) {
        return (database instanceof HsqlDatabase) || (database instanceof H2Database);
    }

    @Override
    public Sql[] generateSql(
            AddAutoIncrementStatement statement,
            Database database,
            SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[]{
            new UnparsedSql(
                "ALTER TABLE "
                    + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())
                    + " ALTER COLUMN "
                    + database.escapeColumnName(
                        statement.getCatalogName(),
                        statement.getSchemaName(),
                        statement.getTableName(),
                        statement.getColumnName())
                    + " "
                    + DataTypeFactory.getInstance().fromDescription(statement.getColumnDataType(), database)
                    + " "
                    + database.getAutoIncrementClause(
                        statement.getStartWith(), statement.getIncrementBy(), statement.getGenerationType(), statement.getDefaultOnNull()),
                getAffectedColumn(statement))
        };
    }
}
