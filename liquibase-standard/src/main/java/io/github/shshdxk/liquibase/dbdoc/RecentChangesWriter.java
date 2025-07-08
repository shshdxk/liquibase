package io.github.shshdxk.liquibase.dbdoc;

import io.github.shshdxk.liquibase.change.Change;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.exception.DatabaseException;
import io.github.shshdxk.liquibase.exception.DatabaseHistoryException;
import io.github.shshdxk.liquibase.resource.Resource;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class RecentChangesWriter extends HTMLWriter {

    public RecentChangesWriter(Resource rootOutputDir, Database database) {
        super(rootOutputDir.resolve("recent"), database);
    }

    @Override
    protected String createTitle(Object object) {
        return "Recent Changes";
    }

    @Override
    protected void writeBody(Writer fileWriter, Object object, List<Change> ranChanges, List<Change> changesToRun) throws IOException, DatabaseHistoryException, DatabaseException {
        writeCustomHTML(fileWriter, object, ranChanges, database);
        writeChanges("Most Recent Changes", fileWriter, ranChanges);
    }

    @Override
    protected void writeCustomHTML(Writer fileWriter, Object object, List<Change> changes, Database database) throws IOException {
    }
}
