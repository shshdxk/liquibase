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
import io.github.shshdxk.liquibase.statement.core.DatabaseTableIdentifier;
import io.github.shshdxk.liquibase.statement.core.TableIsEmptyStatement;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TableIsEmptyPrecondition extends AbstractPrecondition {

    private String catalogName;

    private String schemaName;

    private String tableName;

    @Override
    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet, ChangeExecListener changeExecListener) throws PreconditionFailedException, PreconditionErrorException {
        try {
            TableIsEmptyStatement statement = 
                new TableIsEmptyStatement( new DatabaseTableIdentifier(getCatalogName(), getSchemaName(), getTableName()));

            int result = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database).queryForInt(statement);
            if (result > 0) {
                throw new PreconditionFailedException("Table " + getTableName() + " is not empty.", changeLog, this);
            }

        } catch (PreconditionFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
    }

    @Override
    public Warnings warn(Database database) {
        return new Warnings();
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", tableName);

        return validationErrors;
    }


    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public String getName() {
        return "tableIsEmpty";
    }

}
