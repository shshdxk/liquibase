package io.github.shshdxk.liquibase.changelog.visitor;

import io.github.shshdxk.liquibase.changelog.ChangeLogIterator;
import io.github.shshdxk.liquibase.changelog.filter.ChangeSetFilter;
import io.github.shshdxk.liquibase.Scope;
import io.github.shshdxk.liquibase.changelog.ChangeSet;
import io.github.shshdxk.liquibase.changelog.DatabaseChangeLog;
import io.github.shshdxk.liquibase.changelog.filter.ChangeSetFilterResult;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.exception.LiquibaseException;
import io.github.shshdxk.liquibase.logging.mdc.MdcKey;

import java.util.Set;

/**
 * Called by {@link ChangeLogIterator} when a {@link ChangeSetFilter} accept a changeSet.
 *
 * @see SkippedChangeSetVisitor
 *
 */
public interface ChangeSetVisitor {

    enum Direction {
        FORWARD,
        REVERSE
    }

    Direction getDirection(); 

    void visit(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, Set<ChangeSetFilterResult> filterResults) throws LiquibaseException;

    default void logMdcData(ChangeSet changeSet) {
        Scope scope = Scope.getCurrentScope();
        scope.addMdcValue(MdcKey.CHANGESET_ID, changeSet.getId());
        scope.addMdcValue(MdcKey.CHANGESET_AUTHOR, changeSet.getAuthor());
        scope.addMdcValue(MdcKey.CHANGESET_FILEPATH, changeSet.getFilePath());
    }
}
