package io.github.shshdxk.liquibase.exception;

import io.github.shshdxk.liquibase.changelog.DatabaseChangeLog;
import io.github.shshdxk.liquibase.precondition.FailedPrecondition;
import io.github.shshdxk.liquibase.precondition.Precondition;

import java.util.ArrayList;
import java.util.List;

/**
 * Thrown when a precondition failed. This is NOT the same as a PreconditionErrorException: A failure just means that
 * the specified condition evaluated to "does not apply".
 */
public class PreconditionFailedException extends Exception {

    private static final long serialVersionUID = 1L;
    private List<FailedPrecondition> failedPreconditions;

    public PreconditionFailedException(String message, DatabaseChangeLog changeLog, Precondition precondition) {
        this(new FailedPrecondition(message, changeLog, precondition));
    }

    public PreconditionFailedException(FailedPrecondition failedPrecondition) {
        super("Preconditions Failed");
        this.failedPreconditions = new ArrayList<>();
        failedPreconditions.add(failedPrecondition);
    }

    public PreconditionFailedException(List<FailedPrecondition> failedPreconditions) {
        super("Preconditions Failed");
        this.failedPreconditions = failedPreconditions;
    }

    public List<FailedPrecondition> getFailedPreconditions() {
        return failedPreconditions;
    }

}