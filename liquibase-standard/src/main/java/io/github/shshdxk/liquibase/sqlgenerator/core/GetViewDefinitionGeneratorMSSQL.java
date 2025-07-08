package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.CatalogAndSchema;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.MSSQLDatabase;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sql.UnparsedSql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.statement.core.GetViewDefinitionStatement;
import io.github.shshdxk.liquibase.structure.core.View;

public class GetViewDefinitionGeneratorMSSQL extends GetViewDefinitionGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(GetViewDefinitionStatement statement, Database database) {
        return database instanceof MSSQLDatabase;
    }

    @Override
    public Sql[] generateSql(GetViewDefinitionStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        CatalogAndSchema schema = new CatalogAndSchema(statement.getCatalogName(), statement.getSchemaName()).customize(database);
        String viewNameEscaped = database.escapeObjectName(schema.getCatalogName(), schema.getSchemaName(), statement.getViewName(), View.class);
        String sql;
        sql = "SELECT OBJECT_DEFINITION(OBJECT_ID(N'" + database.escapeStringForDatabase(viewNameEscaped) + "')) AS [ObjectDefinition]";
        return new Sql[] { new UnparsedSql(sql) };
    }
}