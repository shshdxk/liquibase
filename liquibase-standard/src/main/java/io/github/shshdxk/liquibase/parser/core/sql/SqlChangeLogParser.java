package io.github.shshdxk.liquibase.parser.core.sql;

import io.github.shshdxk.liquibase.change.core.RawSQLChange;
import io.github.shshdxk.liquibase.changelog.ChangeLogParameters;
import io.github.shshdxk.liquibase.changelog.ChangeSet;
import io.github.shshdxk.liquibase.changelog.DatabaseChangeLog;
import io.github.shshdxk.liquibase.database.ObjectQuotingStrategy;
import io.github.shshdxk.liquibase.exception.ChangeLogParseException;
import io.github.shshdxk.liquibase.parser.ChangeLogParser;
import io.github.shshdxk.liquibase.resource.Resource;
import io.github.shshdxk.liquibase.resource.ResourceAccessor;
import io.github.shshdxk.liquibase.util.StreamUtil;

import java.io.IOException;

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
            Resource sqlResource = resourceAccessor.getExisting(physicalChangeLogLocation);
            String sql = StreamUtil.readStreamAsString(sqlResource.openInputStream());
            change.setSql(sql);
        } catch (IOException e) {
            throw new ChangeLogParseException(e);
        }
        change.setSplitStatements(false);
        change.setStripComments(false);

        ChangeSet changeSet = new ChangeSet("raw", "includeAll", false, false, physicalChangeLogLocation, null, null, true, ObjectQuotingStrategy.LEGACY, changeLog);
        changeSet.addChange(change);

        changeLog.addChangeSet(changeSet);

        return changeLog;
    }
}
