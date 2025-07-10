package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.OracleDatabase;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sql.UnparsedSql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.statement.core.DropTableStatement;
import io.github.shshdxk.liquibase.structure.core.Relation;
import io.github.shshdxk.liquibase.structure.core.Table;

public class DropTableGenerator extends AbstractSqlGenerator<DropTableStatement> {

    @Override
    public ValidationErrors validate(DropTableStatement dropTableStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", dropTableStatement.getTableName());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(DropTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("DROP TABLE ").append(database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()));
        if (statement.isCascadeConstraints()) {
            if (database.supportsDropTableCascadeConstraints()) {
                if (database instanceof OracleDatabase) {
                    buffer.append(" CASCADE CONSTRAINTS");
                } else {
                    buffer.append(" CASCADE");
                }
            }
        }

        return new Sql[]{
                new UnparsedSql(buffer.toString(), getAffectedTable(statement))
        };
    }

    protected Relation getAffectedTable(DropTableStatement statement) {
        return new Table().setName(statement.getTableName()).setSchema(statement.getCatalogName(), statement.getSchemaName());
    }
}
