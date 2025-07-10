package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sql.UnparsedSql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.statement.core.RawSqlStatement;

/**
 * @deprecated use {@link RawParameterizedSqlGenerator}
 */
@Deprecated
public class RawSqlGenerator extends AbstractSqlGenerator<RawSqlStatement> {

    @Override
    public ValidationErrors validate(RawSqlStatement rawSqlStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("sql", rawSqlStatement.getSql());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(RawSqlStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[] {
           new UnparsedSql(statement.getSql(), statement.getEndDelimiter())
        };
    }
}
