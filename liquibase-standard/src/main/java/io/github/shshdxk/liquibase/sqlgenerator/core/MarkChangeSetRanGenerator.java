package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.change.Change;
import io.github.shshdxk.liquibase.change.core.TagDatabaseChange;
import io.github.shshdxk.liquibase.changelog.ChangeLogHistoryServiceFactory;
import io.github.shshdxk.liquibase.changelog.ChangeSet;
import io.github.shshdxk.liquibase.changelog.column.LiquibaseColumn;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.ObjectQuotingStrategy;
import io.github.shshdxk.liquibase.exception.LiquibaseException;
import io.github.shshdxk.liquibase.exception.UnexpectedLiquibaseException;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorFactory;
import io.github.shshdxk.liquibase.statement.DatabaseFunction;
import io.github.shshdxk.liquibase.statement.SqlStatement;
import io.github.shshdxk.liquibase.statement.core.InsertStatement;
import io.github.shshdxk.liquibase.statement.core.MarkChangeSetRanStatement;
import io.github.shshdxk.liquibase.statement.core.UpdateStatement;
import io.github.shshdxk.liquibase.util.LiquibaseUtil;
import io.github.shshdxk.liquibase.util.StringUtil;

public class MarkChangeSetRanGenerator extends AbstractSqlGenerator<MarkChangeSetRanStatement> {

    private static final String COMMENTS = "COMMENTS";
    private static final String CONTEXTS = "CONTEXTS";
    private static final String LABELS = "LABELS";

    @Override
    public ValidationErrors validate(MarkChangeSetRanStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("changeSet", statement.getChangeSet());

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(MarkChangeSetRanStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String dateValue = database.getCurrentDateTimeFunction();

        ChangeSet changeSet = statement.getChangeSet();

        SqlStatement runStatement;
        // use LEGACY quoting since we're dealing with system objects
        ObjectQuotingStrategy currentStrategy = database.getObjectQuotingStrategy();
        database.setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY);
        try {
            try {
                if (statement.getExecType().equals(ChangeSet.ExecType.FAILED) || statement.getExecType().equals(ChangeSet.ExecType.SKIPPED)) {
                    return EMPTY_SQL; //don't mark
                }

                String tag = null;
                for (Change change : changeSet.getChanges()) {
                    if (change instanceof TagDatabaseChange) {
                        TagDatabaseChange tagChange = (TagDatabaseChange) change;
                        tag = tagChange.getTag();
                    }
                }

            if (statement.getExecType().ranBefore) {
                runStatement = new UpdateStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName())
                        .addNewColumnValue("DATEEXECUTED", new DatabaseFunction(dateValue))
                        .addNewColumnValue("ORDEREXECUTED", ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database).getNextSequenceValue())
                        .addNewColumnValue("MD5SUM", changeSet.generateCheckSum().toString())
                        .addNewColumnValue("EXECTYPE", statement.getExecType().value)
                        .addNewColumnValue("DEPLOYMENT_ID", ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database).getDeploymentId())
                        .addNewColumnValue(COMMENTS, getCommentsColumn(changeSet))
                        .addNewColumnValue(CONTEXTS, getContextsColumn(changeSet))
                        .addNewColumnValue(LABELS, getLabelsColumn(changeSet))
                        .setWhereClause(database.escapeObjectName("ID", LiquibaseColumn.class) + " = ? " +
                                "AND " + database.escapeObjectName("AUTHOR", LiquibaseColumn.class) + " = ? " +
                                "AND " + database.escapeObjectName("FILENAME", LiquibaseColumn.class) + " = ?")
                        .addWhereParameters(changeSet.getId(), changeSet.getAuthor(), changeSet.getFilePath());

                    if (tag != null) {
                        ((UpdateStatement) runStatement).addNewColumnValue("TAG", tag);
                    }
                } else {
                    runStatement = new InsertStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName())
                            .addColumnValue("ID", changeSet.getId())
                            .addColumnValue("AUTHOR", changeSet.getAuthor())
                            .addColumnValue("FILENAME", changeSet.getFilePath())
                            .addColumnValue("DATEEXECUTED", new DatabaseFunction(dateValue))
                            .addColumnValue("ORDEREXECUTED", ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database).getNextSequenceValue())
                            .addColumnValue("MD5SUM", changeSet.generateCheckSum().toString())
                            .addColumnValue("DESCRIPTION", limitSize(changeSet.getDescription()))
                            .addColumnValue(COMMENTS, getCommentsColumn(changeSet))
                            .addColumnValue("EXECTYPE", statement.getExecType().value)
                            .addColumnValue(CONTEXTS, getContextsColumn(changeSet))
                            .addColumnValue(LABELS, getLabelsColumn(changeSet))
                        .addColumnValue("LIQUIBASE", StringUtil.limitSize(LiquibaseUtil.getBuildVersion()
                                                                                            .replaceAll("SNAPSHOT", "SNP")
                                                                                            .replaceAll("beta", "b")
                                                                                            .replaceAll("alpha", "b"), 20)
                            )
                            .addColumnValue("DEPLOYMENT_ID", ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database).getDeploymentId());

                    if (tag != null) {
                        ((InsertStatement) runStatement).addColumnValue("TAG", tag);
                    }
                }
            } catch (LiquibaseException e) {
                throw new UnexpectedLiquibaseException(e);
            }

            return SqlGeneratorFactory.getInstance().generateSql(runStatement, database);
        } finally {
            database.setObjectQuotingStrategy(currentStrategy);
        }
    }

    private String getCommentsColumn(ChangeSet changeSet) {
        return limitSize(StringUtil.trimToEmpty(changeSet.getComments()));
    }

    protected String getContextsColumn(ChangeSet changeSet) {
        return changeSet.buildFullContext();
    }

    protected String getLabelsColumn(ChangeSet changeSet) {
        return changeSet.buildFullLabels();
    }

    private String limitSize(String string) {
        int maxLength = 250;
        if (string.length() > maxLength) {
            return string.substring(0, maxLength - 3) + "...";
        }
        return string;
    }
}
