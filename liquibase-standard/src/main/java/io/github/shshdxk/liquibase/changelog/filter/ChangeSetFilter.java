package io.github.shshdxk.liquibase.changelog.filter;

import io.github.shshdxk.liquibase.changelog.ChangeSet;
import io.github.shshdxk.liquibase.util.ShowSummaryUtil;

public interface ChangeSetFilter {

    ChangeSetFilterResult accepts(ChangeSet changeSet);

    /**
     * @return a descriptive name for the filter, which will be used in the MDC entries for this filter
     */
    default String getMdcName() {
        return getClass().getSimpleName();
    }

    /**
     * @return a descriptive name for the filter, which will be used in the update show-summary feature, see
     * {@link ShowSummaryUtil} for usages
     */
    default String getDisplayName() {
        return getClass().getSimpleName().replace("ChangeSetFilter", "Filter");
    }
}
