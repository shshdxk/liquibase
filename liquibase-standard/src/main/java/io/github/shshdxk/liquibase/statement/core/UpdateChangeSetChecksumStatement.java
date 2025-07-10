package io.github.shshdxk.liquibase.statement.core;

import io.github.shshdxk.liquibase.changelog.ChangeSet;
import io.github.shshdxk.liquibase.statement.AbstractSqlStatement;

public class UpdateChangeSetChecksumStatement extends AbstractSqlStatement {

    private final ChangeSet changeSet;

    public UpdateChangeSetChecksumStatement(ChangeSet changeSet) {
        this.changeSet = changeSet;
    }

    public ChangeSet getChangeSet() {
        return changeSet;
    }
}
