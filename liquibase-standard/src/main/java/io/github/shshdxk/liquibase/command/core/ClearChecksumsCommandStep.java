package io.github.shshdxk.liquibase.command.core;

import io.github.shshdxk.liquibase.Scope;
import io.github.shshdxk.liquibase.changelog.ChangeLogHistoryService;
import io.github.shshdxk.liquibase.changelog.ChangeLogHistoryServiceFactory;
import io.github.shshdxk.liquibase.command.AbstractCommandStep;
import io.github.shshdxk.liquibase.command.CommandDefinition;
import io.github.shshdxk.liquibase.command.CommandResultsBuilder;
import io.github.shshdxk.liquibase.command.CommandScope;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.lockservice.LockService;

import java.util.Arrays;
import java.util.List;

public class ClearChecksumsCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"clearChecksums"};

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Clears all checksums");
        commandDefinition.setLongDescription("Clears all checksums and nullifies the MD5SUM column of the " +
                "DATABASECHANGELOG table so that they will be re-computed on the next database update");
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        final Database database = (Database) commandScope.getDependency(Database.class);

        ChangeLogHistoryService changeLogHistoryService = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(database);
        changeLogHistoryService.init();
        Scope.getCurrentScope().getLog(getClass()).info(String.format("Clearing database change log checksums for database %s", database.getShortName()));
        changeLogHistoryService.clearAllCheckSums();
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        return Arrays.asList(Database.class, LockService.class);
    }

}
