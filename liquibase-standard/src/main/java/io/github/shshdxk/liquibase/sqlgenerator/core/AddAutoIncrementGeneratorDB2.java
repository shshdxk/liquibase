package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.AbstractDb2Database;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sql.UnparsedSql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.statement.core.AddAutoIncrementStatement;

public class AddAutoIncrementGeneratorDB2 extends AddAutoIncrementGenerator {

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddAutoIncrementStatement statement, Database database) {
        return database instanceof AbstractDb2Database;
    }

    @Override
    public ValidationErrors validate(
            AddAutoIncrementStatement statement,
            Database database,
            SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();

        validationErrors.checkRequiredField("columnName", statement.getColumnName());
        validationErrors.checkRequiredField("tableName", statement.getTableName());

        return validationErrors;
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
                    + " SET "
                    + database.getAutoIncrementClause(
                        statement.getStartWith(), statement.getIncrementBy(), statement.getGenerationType(), statement.getDefaultOnNull()),
                getAffectedColumn(statement))
        };
    }
}
