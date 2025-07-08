package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.changelog.ChangeSet;
import io.github.shshdxk.liquibase.changelog.column.LiquibaseColumn;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.ObjectQuotingStrategy;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorFactory;
import io.github.shshdxk.liquibase.statement.SqlStatement;
import io.github.shshdxk.liquibase.statement.core.UpdateChangeSetChecksumStatement;
import io.github.shshdxk.liquibase.statement.core.UpdateStatement;
import io.github.shshdxk.liquibase.util.StringUtil;

public class UpdateChangeSetChecksumGenerator extends AbstractSqlGenerator<UpdateChangeSetChecksumStatement> {
    @Override
    public ValidationErrors validate(UpdateChangeSetChecksumStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("changeSet", statement.getChangeSet());

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(UpdateChangeSetChecksumStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ChangeSet changeSet = statement.getChangeSet();
        ObjectQuotingStrategy currentStrategy = database.getObjectQuotingStrategy();
        database.setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY);
        try {
            SqlStatement runStatement = null;
            runStatement = new UpdateStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName())
                    .addNewColumnValue("MD5SUM", changeSet.generateCheckSum().toString())
                    .setWhereClause(database.escapeObjectName("ID", LiquibaseColumn.class) + " = ? " +
                            "AND " + database.escapeObjectName("AUTHOR", LiquibaseColumn.class) + " = ? " +
                            "AND " + database.escapeObjectName("FILENAME", LiquibaseColumn.class) + " = ?")
                    .addWhereParameters(changeSet.getId(), changeSet.getAuthor(), this.getFilePath(changeSet));

            return SqlGeneratorFactory.getInstance().generateSql(runStatement, database);
        } finally {
            database.setObjectQuotingStrategy(currentStrategy);
        }
    }

    private String getFilePath(ChangeSet changeSet) {
        if (StringUtil.isNotEmpty(changeSet.getStoredFilePath())) {
            return changeSet.getStoredFilePath();
        }
        return changeSet.getFilePath();
    }
}
