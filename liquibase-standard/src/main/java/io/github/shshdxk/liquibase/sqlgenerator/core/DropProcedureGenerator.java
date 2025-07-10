package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.MSSQLDatabase;
import io.github.shshdxk.liquibase.database.core.SQLiteDatabase;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sql.UnparsedSql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.statement.core.DropProcedureStatement;
import io.github.shshdxk.liquibase.structure.core.Schema;
import io.github.shshdxk.liquibase.structure.core.StoredProcedure;

public class DropProcedureGenerator extends AbstractSqlGenerator<DropProcedureStatement> {

    @Override
    public boolean supports(DropProcedureStatement statement, Database database) {
        return !(database instanceof SQLiteDatabase);
    }

    @Override
    public ValidationErrors validate(DropProcedureStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("procedureName", statement.getProcedureName());
        validationErrors.checkDisallowedField("catalogName", statement.getCatalogName(), database, MSSQLDatabase.class);
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(DropProcedureStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[] {
                new UnparsedSql("DROP PROCEDURE "+database.escapeObjectName(statement.getCatalogName(), statement.getSchemaName(), statement.getProcedureName(), StoredProcedure.class),
                        new StoredProcedure().setName(statement.getProcedureName()).setSchema(new Schema(statement.getCatalogName(), statement.getSchemaName())))
        };
    }
}
