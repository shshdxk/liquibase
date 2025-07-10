package io.github.shshdxk.liquibase.changelog.filter;

import io.github.shshdxk.liquibase.changelog.ChangeSet;
import io.github.shshdxk.liquibase.changelog.RanChangeSet;

import java.util.List;

public abstract class RanChangeSetFilter implements ChangeSetFilter {
    public List<RanChangeSet> ranChangeSets;

    public RanChangeSetFilter(List<RanChangeSet> ranChangeSets) {
        this.ranChangeSets = ranChangeSets;
    }

    public RanChangeSet getRanChangeSet(ChangeSet changeSet) {
        for (RanChangeSet ranChangeSet : ranChangeSets) {
            if (ranChangeSet.isSameAs(changeSet)) {
                return ranChangeSet;
            }
        }
        return null;

    }
}
