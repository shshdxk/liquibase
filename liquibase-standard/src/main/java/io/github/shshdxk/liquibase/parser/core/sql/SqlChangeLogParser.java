package io.github.shshdxk.liquibase.parser.core.sql;

import io.github.shshdxk.liquibase.Scope;
import io.github.shshdxk.liquibase.change.core.RawSQLChange;
import io.github.shshdxk.liquibase.changelog.*;
import io.github.shshdxk.liquibase.changeset.ChangeSetService;
import io.github.shshdxk.liquibase.changeset.ChangeSetServiceFactory;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.ObjectQuotingStrategy;
import io.github.shshdxk.liquibase.exception.ChangeLogParseException;
import io.github.shshdxk.liquibase.exception.LiquibaseException;
import io.github.shshdxk.liquibase.exception.UnexpectedLiquibaseException;
import io.github.shshdxk.liquibase.parser.ChangeLogParser;
import io.github.shshdxk.liquibase.resource.Resource;
import io.github.shshdxk.liquibase.resource.ResourceAccessor;
import io.github.shshdxk.liquibase.snapshot.SnapshotControl;
import io.github.shshdxk.liquibase.snapshot.SnapshotGeneratorFactory;
import io.github.shshdxk.liquibase.structure.core.Column;
import io.github.shshdxk.liquibase.structure.core.Table;
import io.github.shshdxk.liquibase.util.ExceptionUtil;
import io.github.shshdxk.liquibase.util.StreamUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("java:S2583")
public class SqlChangeLogParser implements ChangeLogParser {

    @Override
    public boolean supports(String changeLogFile, ResourceAccessor resourceAccessor) {
        return changeLogFile.toLowerCase().endsWith(".sql");
    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }
    
    @Override
    public DatabaseChangeLog parse(String physicalChangeLogLocation, ChangeLogParameters changeLogParameters, ResourceAccessor resourceAccessor) throws ChangeLogParseException {

        DatabaseChangeLog changeLog = new DatabaseChangeLog();
        changeLog.setPhysicalFilePath(physicalChangeLogLocation);

        RawSQLChange change = new RawSQLChange();

        try {
            //
            // Handle empty files with a WARNING message
            //
            Resource sqlResource = resourceAccessor.getExisting(physicalChangeLogLocation);
            String sql = StreamUtil.readStreamAsString(sqlResource.openInputStream());
            //
            // Handle empty files with a WARNING message
            //
            if (StringUtils.isEmpty(sql)) {
                String message = String.format("Unable to parse empty file: '%s'", physicalChangeLogLocation);
                Scope.getCurrentScope().getLog(getClass()).warning(message);
                throw new ChangeLogParseException(message);
            }
            change.setSql(sql);
        } catch (IOException e) {
            throw new ChangeLogParseException(e);
        }
        change.setSplitStatements(false);
        change.setStripComments(false, true);

        Database database = Scope.getCurrentScope().getDatabase();
        ChangeSetServiceFactory factory = ChangeSetServiceFactory.getInstance();
        ChangeSetService service = factory.createChangeSetService();
        ChangeSet changeSet =
           service.createChangeSet(generateId(physicalChangeLogLocation, database), "includeAll",
                false, false, physicalChangeLogLocation, null,
                  null, null, null, true,
                         ObjectQuotingStrategy.LEGACY, changeLog);
        changeSet.addChange(change);

        changeLog.addChangeSet(changeSet);

        ExceptionUtil.doSilently(() -> {
            Scope.getCurrentScope().getAnalyticsEvent().incrementSqlChangelogCount();
        });

        return changeLog;
    }

    /**
     *
     * Generate an change set ID based on the SQL file path, unless there is an existing
     * ran change set with an id/author of "raw::includeAll", which has always been
     * the hardcoded combination for SQL changelog change sets
     *
     * @param   physicalChangeLogLocation    the path to the changelog
     * @param   database                     the database we are using
     * @return  String                       a change set ID
     *
     */
    private String generateId(String physicalChangeLogLocation, Database database) {
        if (database == null || isOldFormat(database)) {
            return "raw";
        }

        List<RanChangeSet> ranChangeSets = new ArrayList<>();
        try {
            ranChangeSets = new ArrayList<>(
               Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(database).getRanChangeSets());
        } catch (Exception dbe) {
            return "raw";
        }

        String interimId = "raw_" + DatabaseChangeLog.normalizePath(physicalChangeLogLocation).replace("/", "_");
        Optional<RanChangeSet> ranChangeSet =
            ranChangeSets.stream().filter(rc -> {
                return rc.getId().equals(interimId) &&
                       rc.getAuthor().equals("includeAll") &&
                       rc.getChangeLog().equals(physicalChangeLogLocation);
            }).findFirst();
        if (ranChangeSet.isPresent()) {
            return interimId;
        }
        return "raw";
    }

    /**
     *
     * Handle the possibility that the changelog is an old format
     *
     * @param   database          The database in question
     * @return  boolean
     *
     */
    private static boolean isOldFormat(Database database) {
        Table changeLogTable = null;
        try {
            changeLogTable = SnapshotGeneratorFactory.getInstance().getDatabaseChangeLogTable(new SnapshotControl
                    (database, false, Table.class, Column.class), database);
        } catch (LiquibaseException e) {
            throw new UnexpectedLiquibaseException(e);
        }
        return changeLogTable != null && changeLogTable.getColumn("ORDEREXECUTED") == null;
    }
}
