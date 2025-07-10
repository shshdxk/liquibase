package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.InformixDatabase;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sql.UnparsedSql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGenerator;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.statement.core.AddUniqueConstraintStatement;

public class AddUniqueConstraintGeneratorInformix extends AddUniqueConstraintGenerator {

    @Override
    public int getPriority() {
        return SqlGenerator.PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddUniqueConstraintStatement statement, Database database) {
        return (database instanceof InformixDatabase);
    }

    @Override
    public Sql[] generateSql(AddUniqueConstraintStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {

        final String sqlNoConstraintNameTemplate = "ALTER TABLE %s ADD CONSTRAINT UNIQUE (%s)";
        final String sqlTemplate = "ALTER TABLE %s ADD CONSTRAINT UNIQUE (%s) CONSTRAINT %s";

        // Using an auto-generated name (a name beginning with space) when creating a new unique constraint is impossible
        String constraintName = statement.getConstraintName();
        if ((constraintName == null) || constraintName.startsWith(" ")) { // Names beginning with a space can't be created in informix using SQL
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
                        , database.escapeColumnNameList(statement.getColumnNames())
                        , database.escapeConstraintName(statement.getConstraintName())
                ), getAffectedUniqueConstraint(statement))
            };
        }

    }

}
