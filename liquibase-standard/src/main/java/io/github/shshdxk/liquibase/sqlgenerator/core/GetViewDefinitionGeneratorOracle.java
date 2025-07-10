package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.CatalogAndSchema;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.OracleDatabase;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sql.UnparsedSql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.statement.core.GetViewDefinitionStatement;

public class GetViewDefinitionGeneratorOracle extends GetViewDefinitionGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(GetViewDefinitionStatement statement, Database database) {
        return database instanceof OracleDatabase;
    }

    @Override
    public Sql[] generateSql(GetViewDefinitionStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        CatalogAndSchema schema = new CatalogAndSchema(statement.getCatalogName(), statement.getSchemaName()).customize(database);

        return new Sql[]{
                new UnparsedSql("SELECT TEXT FROM ALL_VIEWS WHERE upper(VIEW_NAME)='" + statement.getViewName().toUpperCase() + "' AND OWNER='" + schema.getSchemaName() + "'")
        };
    }
}
