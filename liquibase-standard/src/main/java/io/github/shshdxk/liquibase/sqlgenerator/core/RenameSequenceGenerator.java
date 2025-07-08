package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.*;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sql.UnparsedSql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.statement.core.RenameSequenceStatement;
import io.github.shshdxk.liquibase.structure.core.Sequence;

public class RenameSequenceGenerator extends AbstractSqlGenerator<RenameSequenceStatement> {

    @Override
    public boolean supports(RenameSequenceStatement statement, Database database) {
        return database.supportsSequences() 
            // TODO: following are not implemented/tested currently
            && !(database instanceof AbstractDb2Database)
            && !(database instanceof FirebirdDatabase)
            && !(database instanceof H2Database)
            && !(database instanceof HsqlDatabase)
            && !(database instanceof InformixDatabase)
            && !(database instanceof SQLiteDatabase);
    }

    @Override
    public ValidationErrors validate(RenameSequenceStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("newSequenceName", statement.getNewSequenceName());
        validationErrors.checkRequiredField("oldSequenceName", statement.getOldSequenceName());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(RenameSequenceStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String sql;

        if (database instanceof PostgresDatabase) {
            sql = "ALTER SEQUENCE " + database.escapeSequenceName(statement.getCatalogName(), statement.getSchemaName(), statement.getOldSequenceName()) + " RENAME TO " + database.escapeObjectName(statement.getNewSequenceName(), Sequence.class);
        } else if (database instanceof OracleDatabase) {
            sql = "RENAME " + database.escapeObjectName(statement.getOldSequenceName(), Sequence.class) + " TO " + database.escapeObjectName(statement.getNewSequenceName(), Sequence.class);
        } else if( database instanceof MSSQLDatabase){
            sql = "sp_rename " + database.escapeObjectName(statement.getOldSequenceName(), Sequence.class) + " ," + database.escapeObjectName(statement.getNewSequenceName(),Sequence.class);
        } else {
            sql = "ALTER SEQUENCE " + database.escapeSequenceName(statement.getCatalogName(), statement.getSchemaName(), statement.getOldSequenceName()) + " RENAME TO " + database.escapeObjectName(statement.getNewSequenceName(), Sequence.class);
        }

        return new Sql[]{
                new UnparsedSql(sql,
                        getAffectedOldSequence(statement),
                        getAffectedNewSequence(statement)
                )
        };
    }

    protected Sequence getAffectedNewSequence(RenameSequenceStatement statement) {
        return new Sequence().setName(statement.getNewSequenceName()).setSchema(statement.getCatalogName(), statement.getSchemaName());
    }

    protected Sequence getAffectedOldSequence(RenameSequenceStatement statement) {
        return new Sequence().setName(statement.getOldSequenceName()).setSchema(statement.getCatalogName(), statement.getSchemaName());
    }
}
