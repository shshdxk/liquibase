package io.github.shshdxk.liquibase.report;

import io.github.shshdxk.liquibase.changelog.ChangeLogIterator;
import io.github.shshdxk.liquibase.changelog.ChangeSetStatus;

import java.util.Collections;
import java.util.List;

public class StandardShowSummaryGenerator implements ShowSummaryGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public List<ChangeSetStatus> getAllAdditionalChangeSetStatus(ChangeLogIterator runChangeLogIterator) {
        return Collections.emptyList();
    }

    @Override
    public void appendAdditionalSummaryMessages(StringBuilder builder, ChangeLogIterator runChangeLogIterator) {

    }
}
