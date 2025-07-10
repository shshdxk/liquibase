package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sql.UnparsedSql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.statement.core.RawParameterizedSqlStatement;

public class RawParameterizedSqlGenerator extends AbstractSqlGenerator<RawParameterizedSqlStatement> {

    @Override
    public ValidationErrors validate(RawParameterizedSqlStatement statement, Database database, SqlGeneratorChain<RawParameterizedSqlStatement> sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("sql", statement.getSql());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(RawParameterizedSqlStatement statement, Database database, SqlGeneratorChain<RawParameterizedSqlStatement> sqlGeneratorChain) {
        return new Sql[] {new UnparsedSql(statement.getSql(), statement.getEndDelimiter())};
    }
}
