package io.github.shshdxk.liquibase.precondition.core;

import io.github.shshdxk.liquibase.changelog.ChangeSet;
import io.github.shshdxk.liquibase.changelog.DatabaseChangeLog;
import io.github.shshdxk.liquibase.changelog.visitor.ChangeExecListener;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.ObjectQuotingStrategy;
import io.github.shshdxk.liquibase.exception.PreconditionErrorException;
import io.github.shshdxk.liquibase.exception.PreconditionFailedException;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.exception.Warnings;
import io.github.shshdxk.liquibase.precondition.AbstractPrecondition;

public class ObjectQuotingStrategyPrecondition extends AbstractPrecondition {
    private ObjectQuotingStrategy strategy;

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public String getName() {
        return "expectedQuotingStrategy";
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
        try {
            if (changeLog.getObjectQuotingStrategy() != strategy) {
                throw new PreconditionFailedException("Quoting strategy Precondition failed: expected "
                        + strategy +", got "+changeSet.getObjectQuotingStrategy(), changeLog, this);
            }
        } catch (PreconditionFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
    }

    public void setStrategy(String strategy) {
        this.strategy = ObjectQuotingStrategy.valueOf(strategy);
    }
}
