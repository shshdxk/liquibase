package io.github.shshdxk.liquibase.command.core.helpers;

import io.github.shshdxk.liquibase.Liquibase;
import io.github.shshdxk.liquibase.Scope;
import io.github.shshdxk.liquibase.command.CleanUpCommandStep;
import io.github.shshdxk.liquibase.command.CommandResultsBuilder;
import io.github.shshdxk.liquibase.command.CommandScope;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.exception.LockException;
import io.github.shshdxk.liquibase.lockservice.LockService;
import io.github.shshdxk.liquibase.lockservice.LockServiceFactory;

import java.util.Collections;
import java.util.List;

/**
 * Internal command step to be used on CommandStep pipeline to create lock services.
 */
public class LockServiceCommandStep extends AbstractHelperCommandStep implements CleanUpCommandStep {

    protected static final String[] COMMAND_NAME = {"lockServiceCommandStep"};

    private Database database;

    @Override
    public List<Class<?>> requiredDependencies() {
        return Collections.singletonList(Database.class);
    }

    @Override
    public List<Class<?>> providedDependencies() {
        return Collections.singletonList(LockService.class);
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        database = (Database) commandScope.getDependency(Database.class);
        LockServiceFactory.getInstance().getLockService(database).waitForLock();
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void cleanUp(CommandResultsBuilder resultsBuilder) {
        try {
            LockServiceFactory.getInstance().getLockService(database).releaseLock();
        } catch (LockException e) {
            Scope.getCurrentScope().getLog(getClass()).severe(Liquibase.MSG_COULD_NOT_RELEASE_LOCK, e);
        }
        LockServiceFactory.getInstance().resetAll();
    }
}
