package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.InformixDatabase;
import io.github.shshdxk.liquibase.datatype.DataTypeFactory;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sql.UnparsedSql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.statement.core.AddAutoIncrementStatement;

public class AddAutoIncrementGeneratorInformix extends AddAutoIncrementGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddAutoIncrementStatement statement, Database database) {
        return database instanceof InformixDatabase;
    }

    @Override
    public ValidationErrors validate(
            AddAutoIncrementStatement addAutoIncrementStatement,
            Database database,
            SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = super.validate(
            addAutoIncrementStatement, database, sqlGeneratorChain);

        validationErrors.checkRequiredField(
            "columnDataType", addAutoIncrementStatement.getColumnDataType());

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
                    + " MODIFY "
                    + database.escapeColumnName(
                        statement.getCatalogName(),
                        statement.getSchemaName(),
                        statement.getTableName(),
                        statement.getColumnName())
                    + " "
                    + DataTypeFactory.getInstance().fromDescription(statement.getColumnDataType() + "{autoIncrement:true}", database).toDatabaseDataType(database),
                getAffectedColumn(statement))
        };
    }
}

