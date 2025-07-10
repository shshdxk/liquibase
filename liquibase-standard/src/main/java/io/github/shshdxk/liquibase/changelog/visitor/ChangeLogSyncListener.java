package io.github.shshdxk.liquibase.changelog.visitor;

import io.github.shshdxk.liquibase.changelog.ChangeSet;
import io.github.shshdxk.liquibase.changelog.DatabaseChangeLog;
import io.github.shshdxk.liquibase.database.Database;

public interface ChangeLogSyncListener {
    void markedRan(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database);

    default void markedRanFailed(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, Exception e) {
        // do nothing by default
    }
}
