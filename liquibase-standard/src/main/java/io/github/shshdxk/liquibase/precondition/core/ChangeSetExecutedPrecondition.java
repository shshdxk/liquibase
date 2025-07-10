package io.github.shshdxk.liquibase.precondition.core;

import io.github.shshdxk.liquibase.changelog.ChangeSet;
import io.github.shshdxk.liquibase.changelog.DatabaseChangeLog;
import io.github.shshdxk.liquibase.changelog.RanChangeSet;
import io.github.shshdxk.liquibase.changelog.visitor.ChangeExecListener;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.ObjectQuotingStrategy;
import io.github.shshdxk.liquibase.exception.PreconditionErrorException;
import io.github.shshdxk.liquibase.exception.PreconditionFailedException;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.exception.Warnings;
import io.github.shshdxk.liquibase.precondition.AbstractPrecondition;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeSetExecutedPrecondition extends AbstractPrecondition {

    private String changeLogFile;
    private String id;
    private String author;

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public Warnings warn(Database database) {
        return new Warnings();
    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }
    
    @Override
    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet, ChangeExecListener changeExecListener)
            throws PreconditionFailedException, PreconditionErrorException {
        ObjectQuotingStrategy objectQuotingStrategy;
        if (changeSet == null) {
            objectQuotingStrategy = ObjectQuotingStrategy.LEGACY;
        } else {
            objectQuotingStrategy = changeSet.getObjectQuotingStrategy();
        }
        String changeLogFile = getChangeLogFile();
        if (changeLogFile == null) {
            changeLogFile = changeLog.getLogicalFilePath();
        }
        ChangeSet interestedChangeSet = new ChangeSet(getId(), getAuthor(), false, false, changeLogFile, null, null, false, objectQuotingStrategy, changeLog);
        RanChangeSet ranChangeSet;
        try {
            ranChangeSet = database.getRanChangeSet(interestedChangeSet);
        } catch (Exception e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
        if ((ranChangeSet == null) || (ranChangeSet.getExecType() == null) || !ranChangeSet.getExecType().ran) {
            throw new PreconditionFailedException("Changeset '"+interestedChangeSet.toString(false)+"' has not been run", changeLog, this);
        }
    }

    @Override
    public String getName() {
        return "changeSetExecuted";
    }
}
