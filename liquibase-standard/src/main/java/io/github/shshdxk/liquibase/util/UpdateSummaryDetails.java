package io.github.shshdxk.liquibase.util;

import io.github.shshdxk.liquibase.Beta;
import io.github.shshdxk.liquibase.changelog.ChangeSet;
import io.github.shshdxk.liquibase.logging.mdc.customobjects.UpdateSummary;
import lombok.Data;
import lombok.ToString;

import java.util.Map;

/**
 * Container to handle sharing update summary message between different services
 */
@Data
@Beta
@ToString
public class UpdateSummaryDetails {
    private UpdateSummary summary;
    private String output;
    private Map<ChangeSet, String> skipped;
}
