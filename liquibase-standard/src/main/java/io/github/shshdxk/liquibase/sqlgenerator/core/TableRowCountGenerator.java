package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sql.UnparsedSql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.statement.core.TableRowCountStatement;

public class TableRowCountGenerator extends AbstractSqlGenerator<TableRowCountStatement> {

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public boolean supports(TableRowCountStatement statement, Database database) {
        return true;
    }

    @Override
    public ValidationErrors validate(TableRowCountStatement dropColumnStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", dropColumnStatement.getTableName());
        return validationErrors;
    }

    protected String generateCountSql(TableRowCountStatement statement, Database database) {
        return "SELECT COUNT(*) FROM "+database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName());
    }

    @Override
    public Sql[] generateSql(TableRowCountStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[] { new UnparsedSql(generateCountSql(statement, database)) };
    }


}
