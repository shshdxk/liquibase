package io.github.shshdxk.liquibase.command.core;

import io.github.shshdxk.liquibase.Scope;
import io.github.shshdxk.liquibase.changelog.RanChangeSet;
import io.github.shshdxk.liquibase.changelog.filter.CountChangeSetFilter;
import io.github.shshdxk.liquibase.command.*;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.logging.mdc.MdcKey;

import java.util.List;

public class RollbackCountCommandStep extends AbstractRollbackCommandStep {

    public static final String[] COMMAND_NAME = {"rollbackCount"};

    public static final CommandArgumentDefinition<Integer> COUNT_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        COUNT_ARG = builder.argument("count", Integer.class).required()
            .description("The number of changes to rollback").build();
        builder.addArgument(AbstractRollbackCommandStep.ROLLBACK_SCRIPT_ARG).build();
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        Integer changesToRollback = commandScope.getArgumentValue(COUNT_ARG);
        Scope.getCurrentScope().addMdcValue(MdcKey.ROLLBACK_COUNT, String.valueOf(changesToRollback));

        Database database = (Database) commandScope.getDependency(Database.class);

        List<RanChangeSet> ranChangeSetList = database.getRanChangeSetList();
        this.doRollback(resultsBuilder, ranChangeSetList, new CountChangeSetFilter(changesToRollback));
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Rollback the specified number of changes made to the database");
    }
}
