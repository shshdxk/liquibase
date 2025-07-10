package io.github.shshdxk.liquibase.changelog.filter;

import io.github.shshdxk.liquibase.changelog.ChangeSet;
import io.github.shshdxk.liquibase.changelog.RanChangeSet;

import java.util.List;

public class NotRanChangeSetFilter implements ChangeSetFilter {

    public List<RanChangeSet> ranChangeSets;

    public NotRanChangeSetFilter(List<RanChangeSet> ranChangeSets) {
        this.ranChangeSets = ranChangeSets;
    }

    @Override
    @SuppressWarnings({"RedundantIfStatement"})
    public ChangeSetFilterResult accepts(ChangeSet changeSet) {
        for (RanChangeSet ranChangeSet : ranChangeSets) {
            if (ranChangeSet.isSameAs(changeSet)) {
                return new ChangeSetFilterResult(false, "Changeset already ran", this.getClass(), getMdcName(), getDisplayName());
            }
        }
        return new ChangeSetFilterResult(true, "Changeset not yet ran", this.getClass(), getMdcName(), getDisplayName());
    }
}
