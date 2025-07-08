package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.Scope;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.OracleDatabase;
import io.github.shshdxk.liquibase.executor.ExecutorService;
import io.github.shshdxk.liquibase.executor.LoggingExecutor;
import io.github.shshdxk.liquibase.statement.core.InsertOrUpdateStatement;

public class InsertOrUpdateGeneratorOracle extends InsertOrUpdateGenerator {


    @Override
    public boolean supports(InsertOrUpdateStatement statement, Database database) {
        return database instanceof OracleDatabase;
    }

    @Override
    protected String getRecordCheck(InsertOrUpdateStatement insertOrUpdateStatement, Database database, String whereClause) {
        return String.format("DECLARE\n" +
                "\tv_reccount NUMBER := 0;\n" +
                "BEGIN\n" +
                "\tSELECT COUNT(*) INTO v_reccount FROM %s WHERE %s;\n" +
                "\tIF v_reccount = 0 THEN\n",
            database.escapeTableName(insertOrUpdateStatement.getCatalogName(), insertOrUpdateStatement.getSchemaName(), insertOrUpdateStatement.getTableName()),
            whereClause
        );
    }

    @Override
    protected String getElse(Database database){
               return "\tELSIF v_reccount = 1 THEN\n";
    }

    @Override
    protected String getPostUpdateStatements(Database database){
        StringBuilder endStatements = new StringBuilder();
        endStatements.append("END IF;\n");
        endStatements.append("END;\n");

        if (Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database) instanceof LoggingExecutor) {
            endStatements.append("/\n");
        }

        return endStatements.toString();

    }
}
