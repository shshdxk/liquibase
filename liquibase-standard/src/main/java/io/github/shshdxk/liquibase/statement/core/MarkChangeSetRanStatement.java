package io.github.shshdxk.liquibase.statement.core;

import io.github.shshdxk.liquibase.changelog.ChangeSet;
import io.github.shshdxk.liquibase.statement.AbstractSqlStatement;

public class MarkChangeSetRanStatement extends AbstractSqlStatement {

    private final ChangeSet changeSet;

    private final ChangeSet.ExecType execType;

    public MarkChangeSetRanStatement(ChangeSet changeSet, ChangeSet.ExecType execType) {
        this.changeSet = changeSet;
        this.execType = execType;
    }

    public ChangeSet getChangeSet() {
        return changeSet;
    }

    public ChangeSet.ExecType getExecType() {
        return execType;
    }
}
