package io.github.shshdxk.liquibase.changelog.visitor;

import io.github.shshdxk.liquibase.CatalogAndSchema;
import io.github.shshdxk.liquibase.change.Change;
import io.github.shshdxk.liquibase.changelog.ChangeSet;
import io.github.shshdxk.liquibase.changelog.DatabaseChangeLog;
import io.github.shshdxk.liquibase.changelog.filter.ChangeSetFilterResult;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.FirebirdDatabase;
import io.github.shshdxk.liquibase.dbdoc.*;
import io.github.shshdxk.liquibase.exception.LiquibaseException;
import io.github.shshdxk.liquibase.resource.OpenOptions;
import io.github.shshdxk.liquibase.resource.Resource;
import io.github.shshdxk.liquibase.resource.ResourceAccessor;
import io.github.shshdxk.liquibase.snapshot.DatabaseSnapshot;
import io.github.shshdxk.liquibase.snapshot.SnapshotControl;
import io.github.shshdxk.liquibase.snapshot.SnapshotGeneratorFactory;
import io.github.shshdxk.liquibase.structure.DatabaseObject;
import io.github.shshdxk.liquibase.structure.core.Column;
import io.github.shshdxk.liquibase.structure.core.Schema;
import io.github.shshdxk.liquibase.structure.core.Table;
import io.github.shshdxk.liquibase.util.StreamUtil;
import io.github.shshdxk.liquibase.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class DBDocVisitor implements ChangeSetVisitor {

    private static final int MAX_RECENT_CHANGE = 50;
    private final Database database;
    private final SortedSet<ChangeLogInfo> changeLogs;
    private final Map<DatabaseObject, List<Change>> changesByObject;
    private final Map<String, List<Change>> changesByAuthor;
    private final Map<DatabaseObject, List<Change>> changesToRunByObject;
    private final Map<String, List<Change>> changesToRunByAuthor;
    private final List<Change> changesToRun;
    private List<Change> recentChanges;
    private String rootChangeLogName;
    private DatabaseChangeLog rootChangeLog;

    public DBDocVisitor(Database database) {
        this.database = database;

        changesByObject = new HashMap<>();
        changesByAuthor = new HashMap<>();
        changeLogs = new TreeSet<>();

        changesToRunByObject = new HashMap<>();
        changesToRunByAuthor = new HashMap<>();
        changesToRun = new ArrayList<>();
        recentChanges = new ArrayList<>();
    }

    @Override
    public ChangeSetVisitor.Direction getDirection() {
        return ChangeSetVisitor.Direction.FORWARD;
    }

    @Override
    public void visit(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, Set<ChangeSetFilterResult> filterResults) throws LiquibaseException {
        ChangeSet.RunStatus runStatus = this.database.getRunStatus(changeSet);
        if (rootChangeLogName == null) {
            rootChangeLogName = changeSet.getFilePath();
        }

        if (rootChangeLog == null) {
            this.rootChangeLog = databaseChangeLog;
        }

        if (!changesByAuthor.containsKey(changeSet.getAuthor())) {
            changesByAuthor.put(changeSet.getAuthor(), new ArrayList<>());
        }
        if (!changesToRunByAuthor.containsKey(changeSet.getAuthor())) {
            changesToRunByAuthor.put(changeSet.getAuthor(), new ArrayList<>());
        }

        boolean toRun = runStatus.equals(ChangeSet.RunStatus.NOT_RAN) || runStatus.equals(ChangeSet.RunStatus.RUN_AGAIN);
        for (Change change : changeSet.getChanges()) {
            if (toRun) {
                changesToRunByAuthor.get(changeSet.getAuthor()).add(change);
                changesToRun.add(change);
            } else {
                changesByAuthor.get(changeSet.getAuthor()).add(change);
                recentChanges.add(0, change);
            }
        }


        ChangeLogInfo changeLogInfo = new ChangeLogInfo(changeSet.getChangeLog().getLogicalFilePath(), changeSet.getChangeLog().getPhysicalFilePath());
        if (!changeLogs.contains(changeLogInfo)) {
            changeLogs.add(changeLogInfo);
        }

        for (Change change : changeSet.getChanges()) {
            Set<DatabaseObject> affectedDatabaseObjects = change.getAffectedDatabaseObjects(database);
            if (affectedDatabaseObjects != null) {
                for (DatabaseObject dbObject : affectedDatabaseObjects) {
                    if (toRun) {
                        if (!changesToRunByObject.containsKey(dbObject)) {
                            changesToRunByObject.put(dbObject, new ArrayList<>());
                        }
                        changesToRunByObject.get(dbObject).add(change);
                    } else {
                       if (!changesByObject.containsKey(dbObject)) {
                           changesByObject.put(dbObject, new ArrayList<>());
                       }
                       changesByObject.get(dbObject).add(change);
                    }
                }
            }
        }
    }

    public void writeHTML(Resource rootOutputDir, ResourceAccessor resourceAccessor, CatalogAndSchema... schemaList) throws IOException,
        LiquibaseException {
        ChangeLogWriter changeLogWriter = new ChangeLogWriter(resourceAccessor, rootOutputDir);
        HTMLWriter authorWriter = new AuthorWriter(rootOutputDir, database);
        TableWriter tableWriter = new TableWriter(rootOutputDir, database);
        HTMLWriter columnWriter = new ColumnWriter(rootOutputDir, database);
        HTMLWriter pendingChangesWriter = new PendingChangesWriter(rootOutputDir, database);
        HTMLWriter recentChangesWriter = new RecentChangesWriter(rootOutputDir, database);
        HTMLWriter pendingSQLWriter = new PendingSQLWriter(rootOutputDir, database, rootChangeLog);

        CatalogAndSchema[] computedSchemaList = schemaList;
        if (schemaList == null) {
            computedSchemaList = new CatalogAndSchema[]{database.getDefaultSchema()};
        }

        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(computedSchemaList, database, new SnapshotControl(database));
        if (schemaList != null && schemaList.length != 0 && !(database instanceof FirebirdDatabase)) {
            this.validateRequiredSchemas(snapshot, computedSchemaList);
        }

        copyFile("liquibase/dbdoc/stylesheet.css", rootOutputDir);
        copyFile("liquibase/dbdoc/index.html", rootOutputDir);
        copyFile("liquibase/dbdoc/globalnav.html", rootOutputDir);
        copyFile("liquibase/dbdoc/overview-summary.html", rootOutputDir);

        new ChangeLogListWriter(rootOutputDir).writeHTML(changeLogs);
        SortedSet<Table> tables = new TreeSet<>(snapshot.get(Table.class));
        tables.removeIf(database::isLiquibaseObject);

        new TableListWriter(rootOutputDir).writeHTML(tables);
        new AuthorListWriter(rootOutputDir).writeHTML(new TreeSet<Object>(changesByAuthor.keySet()));

        for (String author : changesByAuthor.keySet()) {
            authorWriter.writeHTML(author, changesByAuthor.get(author), changesToRunByAuthor.get(author), rootChangeLogName);
        }

        for (Table table : tables) {
            if (database.isLiquibaseObject(table)) {
                continue;
            }
            tableWriter.writeHTML(table, changesByObject.get(table), changesToRunByObject.get(table), rootChangeLogName, table.getAttribute("schema", new Schema()).toString());
        }

        for (Column column : snapshot.get(Column.class)) {
            if (shouldNotWriteColumnHtml(column)) {
                continue;
            }
            columnWriter.writeHTML(column, changesByObject.get(column), changesToRunByObject.get(column), rootChangeLogName);
        }

        for (ChangeLogInfo changeLog : changeLogs) {
            changeLogWriter.writeChangeLog(changeLog.logicalPath, changeLog.physicalPath);
        }

        pendingChangesWriter.writeHTML("index", null, changesToRun, rootChangeLogName);
        pendingSQLWriter.writeHTML("sql", null, changesToRun, rootChangeLogName);

        if (recentChanges.size() > MAX_RECENT_CHANGE) {
            recentChanges = recentChanges.subList(0, MAX_RECENT_CHANGE);
        }
        recentChangesWriter.writeHTML("index", recentChanges, null, rootChangeLogName);

    }

    private void validateRequiredSchemas(DatabaseSnapshot snapshot, CatalogAndSchema[] schemaList) throws LiquibaseException {
        Set<Schema> schemasFoundAtDb = snapshot.get(Schema.class);
        if (schemasFoundAtDb == null || schemasFoundAtDb.isEmpty()) {
            throw new LiquibaseException("Could not find any of the required schemas at the configured database.");
        }

        Set<String> schemasNamesAtDb = schemasFoundAtDb.stream().filter(s -> !StringUtils.isEmpty(s.getName()))
                .map(s -> s.getName().toLowerCase()).collect(Collectors.toSet());
        Set<String> requiredSchemaNames = Arrays.stream(schemaList).filter(s -> !StringUtils.isEmpty(s.getSchemaName()))
                .map(s -> s.getSchemaName().toLowerCase()).collect(Collectors.toSet());
        List<String> notFoundSchemas = new ArrayList<>();

        for (String required : requiredSchemaNames) {
            if (!schemasNamesAtDb.contains(required)) {
                notFoundSchemas.add(required);
            }
        }
        if (!notFoundSchemas.isEmpty()) {
            throw new LiquibaseException("The following schema(s) could not be found at database: " + StringUtil.join(notFoundSchemas, ","));
        }
    }

    private boolean shouldNotWriteColumnHtml(Column column) {
        return database.isLiquibaseObject(column.getRelation()) ||
                Boolean.TRUE.equals(column.getComputed());
    }

    private void copyFile(String fileToCopy, Resource rootOutputDir) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileToCopy);
        OutputStream outputStream = null;
        try {
            if (inputStream == null) {
                throw new IOException("Can not find " + fileToCopy);
            }
            outputStream = rootOutputDir.resolve(fileToCopy.replaceFirst(".*\\/", "")).openOutputStream(new OpenOptions());
            StreamUtil.copy(inputStream, outputStream);
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    private static class ChangeLogInfo implements Comparable<ChangeLogInfo> {
        public String logicalPath;
        public String physicalPath;


        private ChangeLogInfo(String logicalPath, String physicalPath) {
            this.logicalPath = logicalPath;
            this.physicalPath = physicalPath;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if ((o == null) || (getClass() != o.getClass())) return false;

            ChangeLogInfo that = (ChangeLogInfo) o;

            return logicalPath.equals(that.logicalPath);

        }

        @Override
        public int hashCode() {
            return logicalPath.hashCode();
        }

        @Override
        public int compareTo(ChangeLogInfo o) {
            return this.logicalPath.compareTo(o.logicalPath);
        }

        @Override
        public String toString() {
            return logicalPath;
        }
    }
}
