package io.github.shshdxk.liquibase.command.core.helpers;

import io.github.shshdxk.liquibase.command.core.DiffChangelogCommandStep;
import io.github.shshdxk.liquibase.command.core.DiffCommandStep;
import io.github.shshdxk.liquibase.command.CommandArgumentDefinition;
import io.github.shshdxk.liquibase.command.CommandBuilder;
import io.github.shshdxk.liquibase.command.CommandResultsBuilder;

import java.util.Collections;
import java.util.List;

/**
 * This class contains only the arguments used by {@link DiffCommandStep} and {@link DiffChangelogCommandStep}.
 */
public class DiffArgumentsCommandStep extends AbstractHelperCommandStep {

    public static final String[] COMMAND_NAME = new String[]{"diffArgumentsCommandStep"};

    public static final CommandArgumentDefinition<Boolean> IGNORE_MISSING_REFERENCES;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        IGNORE_MISSING_REFERENCES = builder.argument("ignoreMissingReferences", Boolean.class)
                .description("If true, diff operations will ignore referenced objects which are not found in a snapshot.")
                .defaultValue(false)
                .build();
    }


    @Override
    public String[][] defineCommandNames() {
        return new String[][]{COMMAND_NAME};
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        // do nothing
    }

    @Override
    public List<Class<?>> providedDependencies() {
        return Collections.singletonList(DiffArgumentsCommandStep.class);
    }
}
