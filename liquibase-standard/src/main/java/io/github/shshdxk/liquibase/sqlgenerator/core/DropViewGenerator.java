package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.OracleDatabase;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sql.UnparsedSql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.statement.core.DropViewStatement;
import io.github.shshdxk.liquibase.structure.core.Relation;
import io.github.shshdxk.liquibase.structure.core.View;
import io.github.shshdxk.liquibase.util.ObjectUtil;

public class DropViewGenerator extends AbstractSqlGenerator<DropViewStatement> {

    @Override
    public ValidationErrors validate(DropViewStatement dropViewStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("viewName", dropViewStatement.getViewName());

        validationErrors.checkDisallowedField("ifExists", dropViewStatement.isIfExists(), database, OracleDatabase.class);
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(DropViewStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        final String command = "DROP VIEW " + (ObjectUtil.defaultIfNull(statement.isIfExists(), false) ? "IF EXISTS " : "");
        return new Sql[] {
                new UnparsedSql(command + database.escapeViewName(statement.getCatalogName(), statement.getSchemaName(), statement.getViewName()), getAffectedView(statement))
        };
    }

    protected Relation getAffectedView(DropViewStatement statement) {
        return new View().setName(statement.getViewName()).setSchema(statement.getCatalogName(), statement.getSchemaName());
    }
}
