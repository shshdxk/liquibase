package io.github.shshdxk.liquibase.command.core;

import io.github.shshdxk.liquibase.Contexts;
import io.github.shshdxk.liquibase.LabelExpression;
import io.github.shshdxk.liquibase.RuntimeEnvironment;
import io.github.shshdxk.liquibase.Scope;
import io.github.shshdxk.liquibase.changelog.ChangeLogIterator;
import io.github.shshdxk.liquibase.changelog.DatabaseChangeLog;
import io.github.shshdxk.liquibase.changelog.filter.*;
import io.github.shshdxk.liquibase.changelog.visitor.ChangeLogSyncListener;
import io.github.shshdxk.liquibase.changelog.visitor.ChangeLogSyncVisitor;
import io.github.shshdxk.liquibase.command.AbstractCommandStep;
import io.github.shshdxk.liquibase.command.CommandDefinition;
import io.github.shshdxk.liquibase.command.CommandResultsBuilder;
import io.github.shshdxk.liquibase.command.CommandScope;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.executor.ExecutorService;

import java.util.Arrays;
import java.util.List;

public class MarkNextChangesetRanCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"markNextChangesetRan"};

    @Override
    public List<Class<?>> requiredDependencies() {
        return Arrays.asList(Database.class, DatabaseChangeLog.class);
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Marks the next change you apply as executed in your database");
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        try {
            CommandScope commandScope = resultsBuilder.getCommandScope();
            DatabaseChangeLog changeLog = (DatabaseChangeLog) commandScope.getDependency(DatabaseChangeLog.class);
            Database database = ((Database) commandScope.getDependency(Database.class));
            Contexts contexts = ((Contexts) commandScope.getDependency(Contexts.class));
            LabelExpression labelExpression = ((LabelExpression) commandScope.getDependency(LabelExpression.class));

            ChangeLogIterator logIterator = new ChangeLogIterator(changeLog,
                    new NotRanChangeSetFilter(database.getRanChangeSetList()),
                    new ContextChangeSetFilter(contexts),
                    new LabelChangeSetFilter(labelExpression),
                    new DbmsChangeSetFilter(database),
                    new IgnoreChangeSetFilter(),
                    new CountChangeSetFilter(1));

            logIterator.run(new ChangeLogSyncVisitor(database, getChangeExecListener()),
                    new RuntimeEnvironment(database, contexts, labelExpression)
            );
        } finally {
            Scope.getCurrentScope().getSingleton(ExecutorService.class).reset();
        }
    }

    public ChangeLogSyncListener getChangeExecListener() {
        return null;
    }
}
