package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.SQLiteDatabase;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sql.UnparsedSql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.statement.core.ReindexStatement;
import io.github.shshdxk.liquibase.structure.core.Table;

public class ReindexGeneratorSQLite extends AbstractSqlGenerator<ReindexStatement> {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(ReindexStatement statement, Database database) {
        return (database instanceof SQLiteDatabase);
    }

    @Override
    public ValidationErrors validate(ReindexStatement reindexStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", reindexStatement.getTableName());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(ReindexStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[] {
                new UnparsedSql("REINDEX "+database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()), new Table().setName(statement.getTableName()).setSchema(statement.getCatalogName(), statement.getSchemaName()))
        };
    }
}
