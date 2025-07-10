package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.CatalogAndSchema;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.AbstractDb2Database;
import io.github.shshdxk.liquibase.database.core.Db2zDatabase;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sql.UnparsedSql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGenerator;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.statement.core.GetViewDefinitionStatement;

public class GetViewDefinitionGeneratorDB2 extends GetViewDefinitionGenerator {
    @Override
    public int getPriority() {
        return SqlGenerator.PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(GetViewDefinitionStatement statement, Database database) {
        return database instanceof AbstractDb2Database;
    }

    @Override
    public Sql[] generateSql(GetViewDefinitionStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        CatalogAndSchema schema = new CatalogAndSchema(statement.getCatalogName(), statement.getSchemaName()).customize(database);

        if (database instanceof Db2zDatabase) {
            return new Sql[] {
                    new UnparsedSql("select STATEMENT AS view_definition from SYSIBM.SYSVIEWS where NAME='" + statement.getViewName() + "' and CREATOR = '" + schema.getSchemaName() + "'")
            };
        }
        return new Sql[] {
                new UnparsedSql("select view_definition from SYSIBM.VIEWS where TABLE_NAME='" + statement.getViewName() + "' and TABLE_SCHEMA='" + schema.getSchemaName() + "'")
        };
    }
}
