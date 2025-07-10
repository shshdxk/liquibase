package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.statement.core.RuntimeStatement;

public class RuntimeGenerator extends AbstractSqlGenerator<RuntimeStatement> {

    @Override
    public ValidationErrors validate(RuntimeStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    @Override
    public Sql[] generateSql(RuntimeStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return statement.generate(database);
    }
}
