package io.github.shshdxk.liquibase.command.core;

import io.github.shshdxk.liquibase.*;
import io.github.shshdxk.liquibase.command.*;
import io.github.shshdxk.liquibase.changelog.ChangeLogHistoryService;
import io.github.shshdxk.liquibase.changelog.ChangeLogHistoryServiceFactory;
import io.github.shshdxk.liquibase.changelog.DatabaseChangeLog;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.exception.LiquibaseException;
import io.github.shshdxk.liquibase.executor.ExecutorService;
import io.github.shshdxk.liquibase.lockservice.LockServiceFactory;
import io.github.shshdxk.liquibase.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @deprecated Implement commands with {@link CommandStep} and call them with {@link CommandFactory#getCommandDefinition(String...)}.
 */
public class DropAllCommand extends AbstractCommand<CommandResult> {

    private Database database;
    private CatalogAndSchema[] schemas;
    private String changeLogFile;
    private UUID hubConnectionId;
    private Liquibase liquibase;

    @Override
    public String getName() {
        return "dropAll";
    }

    @Override
    public CommandValidationErrors validate() {
        return new CommandValidationErrors(this);
    }

    public void setLiquibase(Liquibase liquibase) {
        this.liquibase = liquibase;
    }

    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    public CatalogAndSchema[] getSchemas() {
        return schemas;
    }

    public void setSchemas(CatalogAndSchema[] schemas) {
        this.schemas = schemas;
    }

    public DropAllCommand setSchemas(String... schemas) {
        if ((schemas == null) || (schemas.length == 0) || (schemas[0] == null)) {
            this.schemas = null;
            return this;
        }

        schemas = StringUtil.join(schemas, ",").split("\\s*,\\s*");
        List<CatalogAndSchema> finalList = new ArrayList<>();
        for (String schema : schemas) {
            finalList.add(new CatalogAndSchema(null, schema).customize(database));
        }

        this.schemas = finalList.toArray(new CatalogAndSchema[0]);


        return this;

    }

    public String getChangeLogFile() {
        return changeLogFile;
    }

    public void setChangeLogFile(String changeLogFile) {
        this.changeLogFile = changeLogFile;
    }

    public void setHubConnectionId(String hubConnectionIdString) {
        if (hubConnectionIdString == null) {
            return;
        }
        this.hubConnectionId = UUID.fromString(hubConnectionIdString);
    }

    @Override
    public CommandResult run() throws Exception {
        final CommandScope commandScope = new CommandScope("dropAllInternal");
        commandScope.addArgumentValue(InternalDropAllCommandStep.CHANGELOG_ARG, this.liquibase.getDatabaseChangeLog());
        commandScope.addArgumentValue(InternalDropAllCommandStep.CHANGELOG_FILE_ARG, this.changeLogFile);
        commandScope.addArgumentValue(InternalDropAllCommandStep.DATABASE_ARG, this.database);
        commandScope.addArgumentValue(InternalDropAllCommandStep.HUB_CONNECTION_ID_ARG, this.hubConnectionId);
        commandScope.addArgumentValue(InternalDropAllCommandStep.SCHEMAS_ARG, this.schemas);

        final CommandResults results = commandScope.execute();

        return new CommandResult("All objects dropped from " + database.getConnection().getConnectionUserName() + "@" + database.getConnection().getURL());
    }

    protected void checkLiquibaseTables(boolean updateExistingNullChecksums, DatabaseChangeLog databaseChangeLog, Contexts contexts, LabelExpression labelExpression) throws LiquibaseException {
        ChangeLogHistoryService changeLogHistoryService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database);
        changeLogHistoryService.init();
        if (updateExistingNullChecksums) {
            changeLogHistoryService.upgradeChecksums(databaseChangeLog, contexts, labelExpression);
        }
        LockServiceFactory.getInstance().getLockService(database).init();
    }

    protected void resetServices() {
        LockServiceFactory.getInstance().resetAll();
        ChangeLogHistoryServiceFactory.getInstance().resetAll();
        Scope.getCurrentScope().getSingleton(ExecutorService.class).reset();
    }

}
