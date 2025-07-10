package io.github.shshdxk.liquibase.changelog.visitor;

import io.github.shshdxk.liquibase.changelog.ChangeLogIterator;
import io.github.shshdxk.liquibase.changelog.filter.ChangeSetFilter;
import io.github.shshdxk.liquibase.changelog.ChangeSet;
import io.github.shshdxk.liquibase.changelog.DatabaseChangeLog;
import io.github.shshdxk.liquibase.changelog.filter.ChangeSetFilterResult;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.exception.LiquibaseException;

import java.util.Set;

/**
 * Called by {@link ChangeLogIterator} when a {@link ChangeSetFilter} rejects a changeSet.
 * To use, {@link ChangeSetVisitor} implementations should implement this interface as well.
 */
public interface SkippedChangeSetVisitor {

    void skipped(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, Set<ChangeSetFilterResult> filterResults) throws LiquibaseException;

}
