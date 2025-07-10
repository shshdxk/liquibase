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
import io.github.shshdxk.liquibase.statement.core.UpdateChangeSetFilenameStatement;
import io.github.shshdxk.liquibase.statement.core.UpdateStatement;
import io.github.shshdxk.liquibase.util.StringUtil;

public class UpdateChangeSetFilenameGenerator extends AbstractSqlGenerator<UpdateChangeSetFilenameStatement> {
    @Override
    public ValidationErrors validate(UpdateChangeSetFilenameStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("changeSet", statement.getChangeSet());
        validationErrors.checkRequiredField("oldFilename", statement.getOldFilename());

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(UpdateChangeSetFilenameStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ChangeSet changeSet = statement.getChangeSet();
        ObjectQuotingStrategy currentStrategy = database.getObjectQuotingStrategy();
        database.setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY);
        try {
            SqlStatement runStatement = new UpdateStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName())
                    .addNewColumnValue("FILENAME", this.getFilePath(changeSet))
                    .setWhereClause(database.escapeObjectName("ID", LiquibaseColumn.class) + " = ? " +
                            "AND " + database.escapeObjectName("AUTHOR", LiquibaseColumn.class) + " = ? " +
                            "AND " + database.escapeObjectName("FILENAME", LiquibaseColumn.class) + " = ?")
                    .addWhereParameters(changeSet.getId(), changeSet.getAuthor(), statement.getOldFilename());

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
