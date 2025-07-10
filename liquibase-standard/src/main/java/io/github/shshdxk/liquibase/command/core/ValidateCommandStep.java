package io.github.shshdxk.liquibase.command.core;

import io.github.shshdxk.liquibase.Scope;
import io.github.shshdxk.liquibase.changelog.DatabaseChangeLog;
import io.github.shshdxk.liquibase.command.AbstractCommandStep;
import io.github.shshdxk.liquibase.command.CommandBuilder;
import io.github.shshdxk.liquibase.command.CommandDefinition;
import io.github.shshdxk.liquibase.command.CommandResultsBuilder;
import io.github.shshdxk.liquibase.database.Database;

import java.util.Arrays;
import java.util.List;

public class ValidateCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"validate"};

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][]{COMMAND_NAME};
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Validate the changelog for errors");
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        return Arrays.asList(Database.class, DatabaseChangeLog.class);
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        // Do nothing. This is all handled in DatabaseChangelogCommandStep.
        Scope.getCurrentScope().getUI().sendMessage(coreBundle.getString("no.validation.errors.found"));
        resultsBuilder.addResult("statusCode", 0);
    }
}
