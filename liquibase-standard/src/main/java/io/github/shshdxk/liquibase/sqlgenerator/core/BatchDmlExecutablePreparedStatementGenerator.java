package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.statement.BatchDmlExecutablePreparedStatement;

/**
 * Dummy SQL generator for ${@link BatchDmlExecutablePreparedStatement}
 */
public class BatchDmlExecutablePreparedStatementGenerator extends AbstractSqlGenerator<BatchDmlExecutablePreparedStatement>  {
    @Override
    public ValidationErrors validate(BatchDmlExecutablePreparedStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    @Override
    public Sql[] generateSql(BatchDmlExecutablePreparedStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return EMPTY_SQL;
    }
}
