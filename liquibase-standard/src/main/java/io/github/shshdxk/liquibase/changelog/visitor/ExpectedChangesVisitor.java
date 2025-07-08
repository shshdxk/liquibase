package io.github.shshdxk.liquibase.changelog.visitor;

import io.github.shshdxk.liquibase.changelog.ChangeSet;
import io.github.shshdxk.liquibase.changelog.DatabaseChangeLog;
import io.github.shshdxk.liquibase.changelog.RanChangeSet;
import io.github.shshdxk.liquibase.changelog.filter.ChangeSetFilterResult;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.exception.LiquibaseException;

import java.util.*;

public class ExpectedChangesVisitor implements ChangeSetVisitor {
    private final LinkedHashSet<RanChangeSet> unexpectedChangeSets;

    public ExpectedChangesVisitor(List<RanChangeSet> ranChangeSetList) {
        this.unexpectedChangeSets = new LinkedHashSet<>(ranChangeSetList);
    }

    @Override
    public Direction getDirection() {
        return ChangeSetVisitor.Direction.FORWARD;
    }

    @Override
    public void visit(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, Set<ChangeSetFilterResult> filterResults) throws LiquibaseException {
        unexpectedChangeSets.removeIf(ranChangeSet -> ranChangeSet.isSameAs(changeSet));
    }

    public Collection<RanChangeSet> getUnexpectedChangeSets() {
        return unexpectedChangeSets;
    }

}
