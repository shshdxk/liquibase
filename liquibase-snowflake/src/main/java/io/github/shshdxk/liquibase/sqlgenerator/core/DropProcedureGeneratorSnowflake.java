package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.SnowflakeDatabase;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sql.UnparsedSql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.statement.core.DropProcedureStatement;
import io.github.shshdxk.liquibase.structure.core.Schema;

public class DropProcedureGeneratorSnowflake extends DropProcedureGenerator {

    @Override
    public boolean supports(DropProcedureStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public Sql[] generateSql(DropProcedureStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        // we shouldn't escape procedure name for Snowflake
        // DROP PROCEDURE "PUBLIC".proc3() -- works
        // DROP PROCEDURE proc3() -- works
        // DROP PROCEDURE "PUBLIC"."proc3()" -- default core implementation, doesn't work
        // DROP PROCEDURE "proc3()" -- doesn't work

        //
        // Drop with a catalog prefix does not work.  We only add the schema name here
        //
        StringBuilder unparsedSql = new StringBuilder("DROP PROCEDURE ");
        if (statement.getSchemaName() != null) {
            unparsedSql.append(database.escapeObjectName(statement.getSchemaName(), Schema.class));
            unparsedSql.append(".");
        }
        unparsedSql.append(statement.getProcedureName());
        unparsedSql.append("()");
        return new Sql[]{new UnparsedSql(unparsedSql.toString())};
    }
}
