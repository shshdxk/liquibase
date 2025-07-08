package io.github.shshdxk.liquibase.command.core;

import io.github.shshdxk.liquibase.Contexts;
import io.github.shshdxk.liquibase.LabelExpression;
import io.github.shshdxk.liquibase.RuntimeEnvironment;
import io.github.shshdxk.liquibase.Scope;
import io.github.shshdxk.liquibase.changelog.ChangeLogIterator;
import io.github.shshdxk.liquibase.changelog.ChangeLogParameters;
import io.github.shshdxk.liquibase.changelog.DatabaseChangeLog;
import io.github.shshdxk.liquibase.changelog.RanChangeSet;
import io.github.shshdxk.liquibase.changelog.filter.*;
import io.github.shshdxk.liquibase.command.*;
import io.github.shshdxk.liquibase.changelog.visitor.ChangeExecListener;
import io.github.shshdxk.liquibase.changelog.visitor.ChangeLogSyncVisitor;
import io.github.shshdxk.liquibase.command.core.helpers.DatabaseChangelogCommandStep;
import io.github.shshdxk.liquibase.command.core.helpers.HubHandler;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.exception.DatabaseException;
import io.github.shshdxk.liquibase.hub.listener.HubChangeExecListener;
import io.github.shshdxk.liquibase.lockservice.LockService;
import io.github.shshdxk.liquibase.logging.core.BufferedLogService;
import io.github.shshdxk.liquibase.logging.core.CompositeLogService;
import io.github.shshdxk.liquibase.logging.mdc.MdcKey;
import io.github.shshdxk.liquibase.logging.mdc.MdcObject;
import io.github.shshdxk.liquibase.logging.mdc.MdcValue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ChangelogSyncCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"changelogSync"};

    public static final CommandArgumentDefinition<ChangeExecListener> HUB_CHANGE_EXEC_LISTENER_ARG;

    private String tag = null;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);

        HUB_CHANGE_EXEC_LISTENER_ARG = builder.argument("changeExecListener", ChangeExecListener.class)
                .hidden().description("Class that will be used to listen to changes to be sent to Hub (if required)").build();

    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME};
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Marks all changes as executed in the database");
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        return Arrays.asList(LockService.class, DatabaseChangeLog.class, ChangeLogParameters.class);
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        final CommandScope commandScope = resultsBuilder.getCommandScope();
        final Database database = (Database) commandScope.getDependency(Database.class);
        final String changeLogFile = commandScope.getArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG);
        final DatabaseChangeLog changeLog = (DatabaseChangeLog) commandScope.getDependency(DatabaseChangeLog.class);
        final ChangeLogParameters changeLogParameters = (ChangeLogParameters) commandScope.getDependency(ChangeLogParameters.class);

        BufferedLogService bufferLog = new BufferedLogService();
        HubHandler hubHandler = null;

        try {
            ChangeLogIterator runChangeLogIterator = buildChangeLogIterator(tag, changeLog, changeLogParameters.getContexts(), changeLogParameters.getLabels(), database);
            CompositeLogService compositeLogService = new CompositeLogService(true, bufferLog);

            hubHandler = new HubHandler(database, changeLog, changeLogFile, commandScope.getArgumentValue(HUB_CHANGE_EXEC_LISTENER_ARG));
            HubChangeExecListener changeLogSyncListener = hubHandler.startHubForChangelogSync(changeLogParameters, tag,
                    buildChangeLogIterator(tag, changeLog, changeLogParameters.getContexts(), changeLogParameters.getLabels(), database));

            AtomicInteger changesetCount = new AtomicInteger(0);
            Map<String, Object> scopeVars = new HashMap<>(2);
            scopeVars.put(Scope.Attr.logService.name(), compositeLogService);
            scopeVars.put("changesetCount", changesetCount);
            Scope.child(scopeVars, () ->
                    runChangeLogIterator.run(new ChangeLogSyncVisitor(database, changeLogSyncListener),
                    new RuntimeEnvironment(database, changeLogParameters.getContexts(), changeLogParameters.getLabels())));
            Scope.getCurrentScope().addMdcValue(MdcKey.CHANGESET_SYNC_COUNT, changesetCount.toString());

            hubHandler.postUpdateHub(bufferLog);
            try (MdcObject changelogSyncOutcome = Scope.getCurrentScope().addMdcValue(MdcKey.CHANGELOG_SYNC_OUTCOME, MdcValue.COMMAND_SUCCESSFUL)) {
                Scope.getCurrentScope().getLog(getClass()).info("Finished executing " + defineCommandNames()[0][0] + " command");
            }
        } catch (Exception e) {
            if (hubHandler != null) {
                hubHandler.postUpdateHubExceptionHandling(bufferLog, e.getMessage());
            }
            try (MdcObject changelogSyncOutcome = Scope.getCurrentScope().addMdcValue(MdcKey.CHANGELOG_SYNC_OUTCOME, MdcValue.COMMAND_FAILED)) {
                Scope.getCurrentScope().getLog(getClass()).warning("Failed executing " + defineCommandNames()[0][0] + " command");
            }
            throw e;
        }
    }

    private ChangeLogIterator buildChangeLogIterator(String tag, DatabaseChangeLog changeLog, Contexts contexts,
                                                       LabelExpression labelExpression, Database database) throws DatabaseException {

        if (tag == null) {
            return new ChangeLogIterator(changeLog,
                    new NotRanChangeSetFilter(database.getRanChangeSetList()),
                    new ContextChangeSetFilter(contexts),
                    new LabelChangeSetFilter(labelExpression),
                    new IgnoreChangeSetFilter(),
                    new DbmsChangeSetFilter(database));
        } else {
            List<RanChangeSet> ranChangeSetList = database.getRanChangeSetList();
            return new ChangeLogIterator(changeLog,
                    new NotRanChangeSetFilter(database.getRanChangeSetList()),
                    new ContextChangeSetFilter(contexts),
                    new LabelChangeSetFilter(labelExpression),
                    new IgnoreChangeSetFilter(),
                    new DbmsChangeSetFilter(database),
                    new UpToTagChangeSetFilter(tag, ranChangeSetList));
        }
    }

    /**
     * Tag value can be set by subclasses that implements "SyncToTag"
     */
    protected void setTag(String tag) {
        this.tag = tag;
    }
}
