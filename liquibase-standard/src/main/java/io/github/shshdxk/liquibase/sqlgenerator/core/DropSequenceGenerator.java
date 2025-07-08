package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.DerbyDatabase;
import io.github.shshdxk.liquibase.database.core.PostgresDatabase;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sql.UnparsedSql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.statement.core.DropSequenceStatement;
import io.github.shshdxk.liquibase.structure.core.Sequence;

public class DropSequenceGenerator extends AbstractSqlGenerator<DropSequenceStatement> {

    @Override
    public boolean supports(DropSequenceStatement statement, Database database) {
        return database.supportsSequences();
    }

    @Override
    public ValidationErrors validate(DropSequenceStatement dropSequenceStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("sequenceName", dropSequenceStatement.getSequenceName());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(DropSequenceStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String sql = "DROP SEQUENCE ";
        sql += database.escapeSequenceName(statement.getCatalogName(), statement.getSchemaName(), statement.getSequenceName());
        if (database instanceof PostgresDatabase) {
            sql += " CASCADE";
        }
        if (database instanceof DerbyDatabase) {
            sql += " RESTRICT";
        }
        return new Sql[] {
                new UnparsedSql(sql, getAffectedSequence(statement))
        };
    }

    protected Sequence getAffectedSequence(DropSequenceStatement statement) {
        return new Sequence().setName(statement.getSequenceName()).setSchema(statement.getCatalogName(), statement.getSchemaName());
    }
}
