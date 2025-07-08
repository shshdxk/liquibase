package io.github.shshdxk.liquibase.command.core;

import io.github.shshdxk.liquibase.Scope;
import io.github.shshdxk.liquibase.changelog.RanChangeSet;
import io.github.shshdxk.liquibase.changelog.filter.AfterTagChangeSetFilter;
import io.github.shshdxk.liquibase.command.*;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.logging.mdc.MdcKey;

import java.util.List;

/**
 * RollbackCommandStep performs the rollback-to-tag logic. For backwards compatibility issues it is not called "RollbackToTag"
 */
public class RollbackCommandStep extends AbstractRollbackCommandStep {

    public static final String[] COMMAND_NAME = {"rollback"};

    public static final CommandArgumentDefinition<String> TAG_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        TAG_ARG = builder.argument("tag", String.class).required()
            .description("Tag to rollback to").build();

        builder.addArgument(AbstractRollbackCommandStep.ROLLBACK_SCRIPT_ARG).build();
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();

        String tagToRollBackTo = commandScope.getArgumentValue(TAG_ARG);
        Scope.getCurrentScope().addMdcValue(MdcKey.ROLLBACK_TO_TAG, tagToRollBackTo);

        Database database = (Database) commandScope.getDependency(Database.class);

        List<RanChangeSet> ranChangeSetList = database.getRanChangeSetList();
        this.doRollback(resultsBuilder, ranChangeSetList, new AfterTagChangeSetFilter(tagToRollBackTo, ranChangeSetList));
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Rollback changes made to the database based on the specific tag");
    }


}
