package io.github.shshdxk.liquibase.command.core;

import io.github.shshdxk.liquibase.*;
import io.github.shshdxk.liquibase.changelog.ChangeLogIterator;
import io.github.shshdxk.liquibase.changelog.ChangeLogParameters;
import io.github.shshdxk.liquibase.changelog.DatabaseChangeLog;
import io.github.shshdxk.liquibase.changelog.RanChangeSet;
import io.github.shshdxk.liquibase.changelog.filter.*;
import io.github.shshdxk.liquibase.changelog.visitor.ChangeExecListener;
import io.github.shshdxk.liquibase.changelog.visitor.ListVisitor;
import io.github.shshdxk.liquibase.changelog.visitor.RollbackVisitor;
import io.github.shshdxk.liquibase.command.AbstractCommandStep;
import io.github.shshdxk.liquibase.command.CommandResultsBuilder;
import io.github.shshdxk.liquibase.command.CommandScope;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.exception.LiquibaseException;
import io.github.shshdxk.liquibase.exception.LockException;
import io.github.shshdxk.liquibase.executor.ExecutorService;
import io.github.shshdxk.liquibase.executor.LoggingExecutor;
import io.github.shshdxk.liquibase.lockservice.LockService;
import io.github.shshdxk.liquibase.lockservice.LockServiceFactory;
import io.github.shshdxk.liquibase.util.LoggingExecutorTextUtil;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import static io.github.shshdxk.liquibase.Liquibase.MSG_COULD_NOT_RELEASE_LOCK;

public abstract class AbstractFutureRollbackCommandStep extends AbstractCommandStep {

    @Override
    public List<Class<?>> requiredDependencies() {
        // The order of these dependencies is important, because we want the writer to be executed before any of the
        // parent dependencies.
        return Arrays.asList(Writer.class, Database.class, DatabaseChangeLog.class, ChangeLogParameters.class, ChangeExecListener.class);
    }

    @Override
    public final void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        DatabaseChangeLog changeLog = (DatabaseChangeLog) commandScope.getDependency(DatabaseChangeLog.class);
        Database database = (Database) commandScope.getDependency(Database.class);
        ChangeExecListener changeExecListener = (ChangeExecListener) commandScope.getDependency(ChangeExecListener.class);
        ChangeLogParameters changeLogParameters = (ChangeLogParameters) commandScope.getDependency(ChangeLogParameters.class);
        Contexts contexts = changeLogParameters.getContexts();
        LabelExpression labels = changeLogParameters.getLabels();
        Writer writer = (Writer) commandScope.getDependency(Writer.class);

        futureRollbackSQL(getCount(commandScope), getTag(commandScope), contexts, labels, writer, database, changeLog, changeExecListener);
    }

    public Integer getCount(CommandScope commandScope) {
        return null;
    }

    public String getTag(CommandScope commandScope) {
        return null;
    }

    protected void futureRollbackSQL(Integer count, String tag, Contexts contexts, LabelExpression labelExpression,
                                     Writer output, Database database, DatabaseChangeLog changeLog, ChangeExecListener changeExecListener) throws LiquibaseException {

        LoggingExecutor outputTemplate = new LoggingExecutor(Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor(database),
                output, database);
        Scope.getCurrentScope().getSingleton(ExecutorService.class).setExecutor(database, outputTemplate);

        LoggingExecutorTextUtil.outputHeader("SQL to roll back currently unexecuted changes", database, changeLog.getFilePath());

        LockService lockService = LockServiceFactory.getInstance().getLockService(database);
        lockService.waitForLock();

        try {
            changeLog.validate(database, contexts, labelExpression);

            ChangeLogIterator logIterator;
            if ((count == null) && (tag == null)) {
                logIterator = new ChangeLogIterator(changeLog,
                        new NotRanChangeSetFilter(database.getRanChangeSetList()),
                        new ContextChangeSetFilter(contexts),
                        new LabelChangeSetFilter(labelExpression),
                        new IgnoreChangeSetFilter(),
                        new DbmsChangeSetFilter(database));
            } else if (count != null) {
                ChangeLogIterator forwardIterator = new ChangeLogIterator(changeLog,
                        new NotRanChangeSetFilter(database.getRanChangeSetList()),
                        new ContextChangeSetFilter(contexts),
                        new LabelChangeSetFilter(labelExpression),
                        new DbmsChangeSetFilter(database),
                        new IgnoreChangeSetFilter(),
                        new CountChangeSetFilter(count));
                final ListVisitor listVisitor = new ListVisitor();
                forwardIterator.run(listVisitor, new RuntimeEnvironment(database, contexts, labelExpression));

                logIterator = new ChangeLogIterator(changeLog,
                        new NotRanChangeSetFilter(database.getRanChangeSetList()),
                        new ContextChangeSetFilter(contexts),
                        new LabelChangeSetFilter(labelExpression),
                        new DbmsChangeSetFilter(database),
                        new IgnoreChangeSetFilter(),
                        changeSet -> new ChangeSetFilterResult(
                                listVisitor.getSeenChangeSets().contains(changeSet), null, null
                        ));
            } else {
                List<RanChangeSet> ranChangeSetList = database.getRanChangeSetList();
                UpToTagChangeSetFilter upToTagChangeSetFilter = new UpToTagChangeSetFilter(tag, ranChangeSetList);
                ChangeLogIterator forwardIterator = new ChangeLogIterator(changeLog,
                        new NotRanChangeSetFilter(ranChangeSetList),
                        new ContextChangeSetFilter(contexts),
                        new LabelChangeSetFilter(labelExpression),
                        new DbmsChangeSetFilter(database),
                        new IgnoreChangeSetFilter(),
                        upToTagChangeSetFilter);
                final ListVisitor listVisitor = new ListVisitor();
                forwardIterator.run(listVisitor, new RuntimeEnvironment(database, contexts, labelExpression));

                //
                // Check to see if the tag was found and stop if not
                //
                if (! upToTagChangeSetFilter.isSeenTag()) {
                    String message = "No tag matching '" + tag + "' found";
                    Scope.getCurrentScope().getUI().sendMessage("ERROR: " + message);
                    Scope.getCurrentScope().getLog(Liquibase.class).severe(message);
                    throw new LiquibaseException(new IllegalArgumentException(message));
                }

                logIterator = new ChangeLogIterator(changeLog,
                        new NotRanChangeSetFilter(ranChangeSetList),
                        new ContextChangeSetFilter(contexts),
                        new LabelChangeSetFilter(labelExpression),
                        new DbmsChangeSetFilter(database),
                        new IgnoreChangeSetFilter(),
                        changeSet -> new ChangeSetFilterResult(
                                listVisitor.getSeenChangeSets().contains(changeSet), null, null
                        ));
            }

            logIterator.run(new RollbackVisitor(database, changeExecListener),
                    new RuntimeEnvironment(database, contexts, labelExpression)
            );
        } finally {
            try {
                lockService.releaseLock();
            } catch (LockException e) {
                Scope.getCurrentScope().getLog(getClass()).severe(MSG_COULD_NOT_RELEASE_LOCK, e);
            }
        }

        flushOutputWriter(output);
    }

    private void flushOutputWriter(Writer output) throws LiquibaseException {
        if (output == null) {
            return;
        }

        try {
            output.flush();
        } catch (IOException e) {
            throw new LiquibaseException(e);
        }
    }


}
