package io.github.shshdxk.liquibase.logging;

import io.github.shshdxk.liquibase.Scope;
import io.github.shshdxk.liquibase.plugin.AbstractPluginFactory;

public class LogFactory extends AbstractPluginFactory<LogService> {

    private LogFactory() {
    }

    @Override
    protected Class<LogService> getPluginClass() {
        return LogService.class;
    }

    @Override
    protected int getPriority(LogService logService, Object... args) {
        return logService.getPriority();
    }

    /**
     * @deprecated Use {@link Scope#getLog(Class)}
     */
    @Deprecated
    public static Logger getLogger(String ignored) {
        return Scope.getCurrentScope().getLog(LogFactory.class);
    }

    /**
     * @deprecated Use {@link Scope#getLog(Class)}
     */
    @Deprecated
    public static Logger getLogger() {
        return Scope.getCurrentScope().getLog(LogFactory.class);
    }

    /**
     * @deprecated Use {@link Scope#getSingleton(Class)}
     */
    public static LogFactory getInstance() {
        return Scope.getCurrentScope().getSingleton(LogFactory.class);
    }
}
