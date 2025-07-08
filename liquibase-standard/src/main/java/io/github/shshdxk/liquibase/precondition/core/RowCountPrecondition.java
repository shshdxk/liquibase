package io.github.shshdxk.liquibase.precondition.core;

import io.github.shshdxk.liquibase.Scope;
import io.github.shshdxk.liquibase.changelog.ChangeSet;
import io.github.shshdxk.liquibase.changelog.DatabaseChangeLog;
import io.github.shshdxk.liquibase.changelog.visitor.ChangeExecListener;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.exception.PreconditionErrorException;
import io.github.shshdxk.liquibase.exception.PreconditionFailedException;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.exception.Warnings;
import io.github.shshdxk.liquibase.executor.ExecutorService;
import io.github.shshdxk.liquibase.precondition.AbstractPrecondition;
import io.github.shshdxk.liquibase.statement.core.TableRowCountStatement;
import io.github.shshdxk.liquibase.util.StringUtil;

public class RowCountPrecondition extends AbstractPrecondition {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private Integer expectedRows;

    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = StringUtil.trimToNull(schemaName);
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Integer getExpectedRows() {
        return expectedRows;
    }

    public void setExpectedRows(Integer expectedRows) {
        this.expectedRows = expectedRows;
    }

    @Override
    public Warnings warn(Database database) {
        return new Warnings();
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", tableName);
        validationErrors.checkRequiredField("expectedRows", expectedRows);

        return validationErrors;
    }

    @Override
    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet, ChangeExecListener changeExecListener)
            throws PreconditionFailedException, PreconditionErrorException {
        try {
            TableRowCountStatement statement = new TableRowCountStatement(catalogName, schemaName, tableName);

            int result = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database).queryForInt(statement);
            if (result != expectedRows) {
                throw new PreconditionFailedException(getFailureMessage(result, expectedRows), changeLog, this);
            }

        } catch (PreconditionFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
    }

    protected String getFailureMessage(int result, int expectedRows) {
        return "Table "+tableName+" does not have the expected row count of "+expectedRows+". It contains "+result+" rows";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public String getName() {
        return "rowCount";
    }

}
