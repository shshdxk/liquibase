package io.github.shshdxk.liquibase.command.core;

import io.github.shshdxk.liquibase.*;
import io.github.shshdxk.liquibase.changelog.ChangeLogIterator;
import io.github.shshdxk.liquibase.changelog.ChangeLogParameters;
import io.github.shshdxk.liquibase.changelog.DatabaseChangeLog;
import io.github.shshdxk.liquibase.command.*;
import io.github.shshdxk.liquibase.changelog.filter.DbmsChangeSetFilter;
import io.github.shshdxk.liquibase.changelog.visitor.DBDocVisitor;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.exception.LiquibaseException;
import io.github.shshdxk.liquibase.lockservice.LockService;
import io.github.shshdxk.liquibase.resource.PathHandlerFactory;
import io.github.shshdxk.liquibase.resource.Resource;
import io.github.shshdxk.liquibase.resource.ResourceAccessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DbDocCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"dbDoc"};
    public static final CommandArgumentDefinition<String> OUTPUT_DIRECTORY_ARG;
    public static final CommandArgumentDefinition<String> SCHEMAS_ARG;
    public static final CommandArgumentDefinition<CatalogAndSchema[]> CATALOG_AND_SCHEMAS_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        OUTPUT_DIRECTORY_ARG = builder.argument("outputDirectory", String.class)
                .required()
                .description("The directory where the documentation is generated")
                .build();
        SCHEMAS_ARG = builder.argument("schemas", String.class)
                .description("Database schemas to include objects from in reporting")
                .build();
        CATALOG_AND_SCHEMAS_ARG = builder.argument("catalogAndSchemas", CatalogAndSchema[].class)
                .hidden()
                .optional()
                .build();
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        return Arrays.asList(Database.class, LockService.class, DatabaseChangeLog.class, ChangeLogParameters.class);
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][]{COMMAND_NAME};
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Generates JavaDoc documentation for the existing database and changelogs");
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        final CommandScope commandScope = resultsBuilder.getCommandScope();
        final Database database = (Database) commandScope.getDependency(Database.class);
        final ChangeLogParameters changeLogParameters = (ChangeLogParameters) commandScope.getDependency(ChangeLogParameters.class);
        final String outputDirectory = commandScope.getArgumentValue(OUTPUT_DIRECTORY_ARG);
        final String schemaList = commandScope.getArgumentValue(SCHEMAS_ARG);
        final ResourceAccessor resourceAccessor = Scope.getCurrentScope().getResourceAccessor();
        final CatalogAndSchema[] catalogAndSchemaArg = commandScope.getArgumentValue(CATALOG_AND_SCHEMAS_ARG);
        final CatalogAndSchema[] catalogAndSchemas = getCatalogAndSchema(schemaList, catalogAndSchemaArg, database);
        final Contexts contexts = changeLogParameters.getContexts();
        final LabelExpression labelExpression = changeLogParameters.getLabels();

        Scope.getCurrentScope().getLog(getClass()).info("Generating Database Documentation");

        try {
            final DatabaseChangeLog databaseChangeLog = (DatabaseChangeLog) commandScope.getDependency(DatabaseChangeLog.class);

            databaseChangeLog.validate(database, contexts, labelExpression);

            ChangeLogIterator logIterator = new ChangeLogIterator(databaseChangeLog, new DbmsChangeSetFilter(database));

            DBDocVisitor visitor = new DBDocVisitor(database);
            logIterator.run(visitor, new RuntimeEnvironment(database, contexts, labelExpression));

            final PathHandlerFactory pathHandlerFactory = Scope.getCurrentScope().getSingleton(PathHandlerFactory.class);
            Resource resource = pathHandlerFactory.getResource(outputDirectory);
            visitor.writeHTML(resource, resourceAccessor, catalogAndSchemas);
            resultsBuilder.addResult("statusCode", 0);
        } catch (IOException e) {
            resultsBuilder.addResult("statusCode", 1);
            throw new LiquibaseException(e);
        }
    }

    /**
     * Finds the appropriate catalog and schema for the query
     *
     * @param schemas             the comma list of schemas input to the command via the command framework or cli
     * @param catalogAndSchemaArg the list of catalog and schema from Main or Liquibase classes. This will be used if present.
     * @param database            the database to find the catalog and schema against
     * @return the catalog and schema array to use
     */
    private CatalogAndSchema[] getCatalogAndSchema(String schemas, CatalogAndSchema[] catalogAndSchemaArg, Database database) {
        if (catalogAndSchemaArg != null) {
            return catalogAndSchemaArg;
        } else if (schemas != null) {
            List<CatalogAndSchema> schemaList = new ArrayList<>();
            for (String schema : schemas.split(",")) {
                schemaList.add(new CatalogAndSchema(null, schema).customize(database));
            }
            return schemaList.toArray(new CatalogAndSchema[0]);
        }
        return new CatalogAndSchema[]{new CatalogAndSchema(null, null)};
    }
}
