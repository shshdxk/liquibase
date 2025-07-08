package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.changelog.ChangeSet;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.ObjectQuotingStrategy;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorFactory;
import io.github.shshdxk.liquibase.statement.core.DeleteStatement;
import io.github.shshdxk.liquibase.statement.core.RemoveChangeSetRanStatusStatement;
import io.github.shshdxk.liquibase.changelog.column.LiquibaseColumn;

public class RemoveChangeSetRanStatusGenerator extends AbstractSqlGenerator<RemoveChangeSetRanStatusStatement> {

    @Override
    public ValidationErrors validate(RemoveChangeSetRanStatusStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors errors = new ValidationErrors();
        errors.checkRequiredField("changeSet", statement.getChangeSet());
        return errors;
    }

    @Override
    public Sql[] generateSql(RemoveChangeSetRanStatusStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ChangeSet changeSet = statement.getChangeSet();

        ObjectQuotingStrategy currentStrategy = database.getObjectQuotingStrategy();
        database.setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY);
        try {
            return SqlGeneratorFactory.getInstance().generateSql(new DeleteStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName())
                            .setWhere(database.escapeObjectName("ID", LiquibaseColumn.class) + " = ? " +
                                    "AND " + database.escapeObjectName("AUTHOR", LiquibaseColumn.class) + " = ? " +
                                    "AND " + database.escapeObjectName("FILENAME", LiquibaseColumn.class) + " = ?")
                        .addWhereParameters(changeSet.getId(), changeSet.getAuthor(), changeSet.getStoredFilePath())
                    , database);
        } finally {
            database.setObjectQuotingStrategy(currentStrategy);
        }
    }
}
