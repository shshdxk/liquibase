package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sql.UnparsedSql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.statement.core.DeleteStatement;
import io.github.shshdxk.liquibase.structure.core.Relation;
import io.github.shshdxk.liquibase.structure.core.Table;

import static io.github.shshdxk.liquibase.util.SqlUtil.replacePredicatePlaceholders;

public class DeleteGenerator extends AbstractSqlGenerator<DeleteStatement> {

    @Override
    public ValidationErrors validate(DeleteStatement deleteStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", deleteStatement.getTableName());
        if ((deleteStatement.getWhereParameters() != null) && !deleteStatement.getWhereParameters().isEmpty() &&
            (deleteStatement.getWhere() == null)) {
            validationErrors.addError("whereParams set but no whereClause");
        }
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(DeleteStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuilder sql = new StringBuilder("DELETE FROM ")
            .append(database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()));

        if (statement.getWhere() != null) {
            sql.append(" WHERE ").append(replacePredicatePlaceholders(database, statement.getWhere(), statement.getWhereColumnNames(), statement.getWhereParameters()));
        }

        return new Sql[] { new UnparsedSql(sql.toString(), getAffectedTable(statement)) };
    }

    protected Relation getAffectedTable(DeleteStatement statement) {
        return new Table().setName(statement.getTableName()).setSchema(statement.getCatalogName(), statement.getSchemaName());
    }
}
