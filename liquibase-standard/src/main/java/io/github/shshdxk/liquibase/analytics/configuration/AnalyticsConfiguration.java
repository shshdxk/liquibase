package io.github.shshdxk.liquibase.analytics.configuration;

import io.github.shshdxk.liquibase.plugin.Plugin;

public interface AnalyticsConfiguration extends Plugin {
    int getPriority();

    boolean isOssAnalyticsEnabled() throws Exception;
    boolean isProAnalyticsEnabled() throws Exception;
}
