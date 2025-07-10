package io.github.shshdxk.liquibase.logging.core;

import io.github.shshdxk.liquibase.logging.LogService;
import io.github.shshdxk.liquibase.plugin.AbstractPluginFactory;

public class LogServiceFactory extends AbstractPluginFactory<LogService> {

    private LogServiceFactory() {
    }

    @Override
    protected Class<LogService> getPluginClass() {
        return LogService.class;
    }

    @Override
    protected int getPriority(LogService obj, Object... args) {
        return obj.getPriority();
    }

    public LogService getDefaultLogService() {
        return getPlugin();
    }
}
