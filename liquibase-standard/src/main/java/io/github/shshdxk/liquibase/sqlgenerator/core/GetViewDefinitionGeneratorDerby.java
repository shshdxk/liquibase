package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.CatalogAndSchema;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.DerbyDatabase;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sql.UnparsedSql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.statement.core.GetViewDefinitionStatement;

public class GetViewDefinitionGeneratorDerby extends GetViewDefinitionGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(GetViewDefinitionStatement statement, Database database) {
        return database instanceof DerbyDatabase;
    }

    @Override
    public Sql[] generateSql(GetViewDefinitionStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        CatalogAndSchema schema = new CatalogAndSchema(statement.getCatalogName(), statement.getSchemaName()).customize(database);

        return new Sql[] {
                    new UnparsedSql("select V.VIEWDEFINITION from SYS.SYSVIEWS V, SYS.SYSTABLES T, SYS.SYSSCHEMAS S WHERE  V.TABLEID=T.TABLEID AND T.SCHEMAID=S.SCHEMAID AND T.TABLETYPE='V' AND T.TABLENAME='" + statement.getViewName() + "' AND S.SCHEMANAME='"+schema.getSchemaName()+"'")
            };
    }
}