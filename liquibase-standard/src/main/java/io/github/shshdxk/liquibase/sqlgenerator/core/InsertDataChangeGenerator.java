package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.statement.InsertExecutablePreparedStatement;

/**
 * Dummy SQL generator for <code>InsertDataChange.ExecutableStatement</code><br>
 */
public class InsertDataChangeGenerator extends AbstractSqlGenerator<InsertExecutablePreparedStatement> {
    @Override
    public ValidationErrors validate(InsertExecutablePreparedStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    @Override
    public Sql[] generateSql(InsertExecutablePreparedStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return EMPTY_SQL;
    }
}
