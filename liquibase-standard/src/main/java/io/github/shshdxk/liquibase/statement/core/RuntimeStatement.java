package io.github.shshdxk.liquibase.statement.core;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.statement.AbstractSqlStatement;

import static io.github.shshdxk.liquibase.sqlgenerator.SqlGenerator.EMPTY_SQL;

public class RuntimeStatement extends AbstractSqlStatement {
    public Sql[] generate(Database database) {
        return EMPTY_SQL;
    }
}
