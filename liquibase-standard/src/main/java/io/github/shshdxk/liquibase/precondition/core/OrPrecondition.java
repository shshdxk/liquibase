package io.github.shshdxk.liquibase.precondition.core;

import io.github.shshdxk.liquibase.changelog.ChangeSet;
import io.github.shshdxk.liquibase.changelog.DatabaseChangeLog;
import io.github.shshdxk.liquibase.changelog.visitor.ChangeExecListener;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.exception.PreconditionErrorException;
import io.github.shshdxk.liquibase.exception.PreconditionFailedException;
import io.github.shshdxk.liquibase.exception.Warnings;
import io.github.shshdxk.liquibase.precondition.FailedPrecondition;
import io.github.shshdxk.liquibase.precondition.Precondition;
import io.github.shshdxk.liquibase.precondition.PreconditionLogic;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for controlling "or" logic in preconditions.
 */
public class OrPrecondition extends PreconditionLogic {

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public Warnings warn(Database database) {
        return new Warnings();
    }

    @Override
    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet, ChangeExecListener changeExecListener)
            throws PreconditionFailedException, PreconditionErrorException {
        boolean onePassed = false;
        List<FailedPrecondition> failures = new ArrayList<>();
        for (Precondition precondition : getNestedPreconditions()) {
            try {
                precondition.check(database, changeLog, changeSet, changeExecListener);
                onePassed = true;
                break;
            } catch (PreconditionFailedException e) {
                failures.addAll(e.getFailedPreconditions());
            }
        }
        if (!onePassed) {
            throw new PreconditionFailedException(failures);
        }
    }

    @Override
    public String getName() {
        return "or";
    }
}
