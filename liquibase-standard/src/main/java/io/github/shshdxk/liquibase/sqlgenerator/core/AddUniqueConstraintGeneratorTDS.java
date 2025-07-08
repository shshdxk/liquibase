package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.SybaseASADatabase;
import io.github.shshdxk.liquibase.database.core.SybaseDatabase;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sql.UnparsedSql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.statement.core.AddUniqueConstraintStatement;

public class AddUniqueConstraintGeneratorTDS extends AddUniqueConstraintGenerator {

    public AddUniqueConstraintGeneratorTDS() {

    }

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddUniqueConstraintStatement statement, Database database) {
        return  (database instanceof SybaseDatabase)
            || (database instanceof SybaseASADatabase)
        ;
    }

    @Override
    public Sql[] generateSql(AddUniqueConstraintStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {

        final String sqlTemplate = "ALTER TABLE %s ADD CONSTRAINT %s UNIQUE (%s)";
        final String sqlNoConstraintNameTemplate = "ALTER TABLE %s ADD UNIQUE (%s)";

        if (statement.getConstraintName() == null) {
            return new Sql[] {
                new UnparsedSql(String.format(sqlNoConstraintNameTemplate
                        , database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())
                        , database.escapeColumnNameList(statement.getColumnNames())
                ), getAffectedUniqueConstraint(statement))
            };
        } else {
            return new Sql[] {
                new UnparsedSql(String.format(sqlTemplate
                        , database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())
                        , database.escapeConstraintName(statement.getConstraintName())
                        , database.escapeColumnNameList(statement.getColumnNames())
                ), getAffectedUniqueConstraint(statement))
            };
        }
    }


}
