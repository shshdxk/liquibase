package io.github.shshdxk.liquibase.command.core;

import io.github.shshdxk.liquibase.Scope;
import io.github.shshdxk.liquibase.changelog.RanChangeSet;
import io.github.shshdxk.liquibase.changelog.filter.ExecutedAfterChangeSetFilter;
import io.github.shshdxk.liquibase.command.*;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.logging.mdc.MdcKey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class RollbackToDateCommandStep extends AbstractRollbackCommandStep {

    public static final String[] COMMAND_NAME = {"rollbackToDate"};

    public static final CommandArgumentDefinition<Date> DATE_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        DATE_ARG = builder.argument("date", Date.class).required()
            .description("Date to rollback changes to").build();

        builder.addArgument(AbstractRollbackCommandStep.ROLLBACK_SCRIPT_ARG).build();
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        Date dateToRollBackTo = commandScope.getArgumentValue(DATE_ARG);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        Scope.getCurrentScope().addMdcValue(MdcKey.ROLLBACK_TO_DATE, formatter.format(dateToRollBackTo));

        Database database = (Database) commandScope.getDependency(Database.class);

        List<RanChangeSet> ranChangeSetList = database.getRanChangeSetList();
        this.doRollback(resultsBuilder, ranChangeSetList, new ExecutedAfterChangeSetFilter(dateToRollBackTo, ranChangeSetList));
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Rollback changes made to the database based on the specific date");
    }
}
