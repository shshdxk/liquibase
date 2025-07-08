package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.OracleDatabase;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sql.UnparsedSql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.statement.StoredProcedureStatement;

public class StoredProcedureGenerator extends AbstractSqlGenerator<StoredProcedureStatement> {

    @Override
    public ValidationErrors validate(StoredProcedureStatement storedProcedureStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("procedureName", storedProcedureStatement.getProcedureName());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(StoredProcedureStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuilder string = new StringBuilder();
        string.append("exec ").append(statement.getProcedureName()).append("(");
        for (String param : statement.getParameters()) {
            string.append(" ").append(param).append(",");
        }
        String sql = string.toString().replaceFirst(",$", "")+")";

        if (database instanceof OracleDatabase) {
            sql = sql.replaceFirst("exec ", "BEGIN ").replaceFirst("\\)$", "); END;");
        }
        return new Sql[] { new UnparsedSql(sql)};

    }
}
