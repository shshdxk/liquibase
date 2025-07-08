package io.github.shshdxk.liquibase.hub;

import io.github.shshdxk.liquibase.exception.LiquibaseException;

public class LiquibaseHubException extends LiquibaseException {

    public LiquibaseHubException() {
    }

    public LiquibaseHubException(String message) {
        super(message);
    }

    public LiquibaseHubException(String message, Throwable cause) {
        super(message, cause);
    }

    public LiquibaseHubException(Throwable cause) {
        super(cause);
    }
}
