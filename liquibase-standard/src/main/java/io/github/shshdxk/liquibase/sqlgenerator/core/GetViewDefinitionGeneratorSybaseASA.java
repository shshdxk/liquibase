package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.SybaseASADatabase;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sql.UnparsedSql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.statement.core.GetViewDefinitionStatement;

public class GetViewDefinitionGeneratorSybaseASA extends GetViewDefinitionGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(GetViewDefinitionStatement statement, Database database) {
        return database instanceof SybaseASADatabase;
    }

    @Override
    public Sql[] generateSql(GetViewDefinitionStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[]{
                new UnparsedSql("select viewtext from sysviews where upper(viewname)='" + statement.getViewName().toUpperCase() + "' and upper(vcreator) = '" + statement.getSchemaName().toUpperCase() + '\'')
        };
    }
}