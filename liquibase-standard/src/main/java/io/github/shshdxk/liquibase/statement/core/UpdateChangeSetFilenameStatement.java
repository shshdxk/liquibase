package io.github.shshdxk.liquibase.statement.core;

import io.github.shshdxk.liquibase.changelog.ChangeSet;
import io.github.shshdxk.liquibase.statement.AbstractSqlStatement;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateChangeSetFilenameStatement extends AbstractSqlStatement {
    private final ChangeSet changeSet;
    private final String oldFilename;
}
