package io.github.shshdxk.liquibase.command.core;

import io.github.shshdxk.liquibase.Scope;
import io.github.shshdxk.liquibase.changelog.ChangeLogParameters;
import io.github.shshdxk.liquibase.changeset.ChangeSetService;
import io.github.shshdxk.liquibase.changeset.ChangeSetServiceFactory;
import io.github.shshdxk.liquibase.command.*;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.exception.DatabaseException;
import io.github.shshdxk.liquibase.exception.LiquibaseException;
import io.github.shshdxk.liquibase.executor.Executor;
import io.github.shshdxk.liquibase.executor.ExecutorService;
import io.github.shshdxk.liquibase.lockservice.LockService;
import io.github.shshdxk.liquibase.resource.PathHandlerFactory;
import io.github.shshdxk.liquibase.resource.Resource;
import io.github.shshdxk.liquibase.statement.core.RawParameterizedSqlStatement;
import io.github.shshdxk.liquibase.util.FileUtil;
import io.github.shshdxk.liquibase.util.StreamUtil;
import io.github.shshdxk.liquibase.util.StringUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class ExecuteSqlCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"executeSql"};
    public static final CommandArgumentDefinition<String> SQL_ARG;
    public static final CommandArgumentDefinition<String> SQLFILE_ARG;
    public static final CommandArgumentDefinition<String> DELIMITER_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        SQL_ARG = builder.argument("sql", String.class)
                .description("SQL string to execute").build();
        SQLFILE_ARG = builder.argument("sqlFile", String.class)
                .description("SQL script to execute").build();
        DELIMITER_ARG = builder.argument("delimiter", String.class)
                .description("Delimiter to use when executing SQL script").build();
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][]{COMMAND_NAME};
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Execute a SQL string or file");
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        return Arrays.asList(Database.class, LockService.class);
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        final CommandScope commandScope = resultsBuilder.getCommandScope();
        final Database database = (Database) commandScope.getDependency(Database.class);
        final String sql = commandScope.getArgumentValue(SQL_ARG);
        final String sqlFile = commandScope.getArgumentValue(SQLFILE_ARG);
        final Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);
        final String sqlText = getSqlScript(sql, sqlFile);
        final StringBuilder out = new StringBuilder();
        final String[] sqlStrings = StringUtil.processMultiLineSQL(sqlText, true, true, determineEndDelimiter(commandScope), null);

        ChangeLogParameters changeLogParameters = new ChangeLogParameters(database);
        for (String sqlString : sqlStrings) {
            sqlString = changeLogParameters.expandExpressions(sqlString, null);
            if (sqlString.toLowerCase().matches("(?s)\\s*select\\s+.*")) {
                out.append(handleSelect(sqlString, executor));
            } else {
                executor.execute(new RawParameterizedSqlStatement(sqlString));
                out.append("Successfully Executed: ").append(sqlString).append("\n");
            }
            out.append("\n");
        }

        database.commit();
        handleOutput(resultsBuilder, out.toString());
        resultsBuilder.addResult("output", out.toString());
    }

    protected static String determineEndDelimiter(CommandScope commandScope) {
        String delimiter = commandScope.getArgumentValue(DELIMITER_ARG);
        return getEndDelimiter(delimiter);
    }

    public static String getEndDelimiter(String delimiter) {
        if (delimiter == null) {
            ChangeSetService service = ChangeSetServiceFactory.getInstance().createChangeSetService();
            delimiter = service.getEndDelimiter(null);
        }
        return delimiter;
    }

    protected String getSqlScript(String sql, String sqlFile) throws IOException, LiquibaseException {
        return getSqlFromSource(sql, sqlFile);
    }

    public static String getSqlFromSource(String sql, String sqlFile) throws IOException, LiquibaseException {
        if (sqlFile == null) {
            return sql;
        } 
        final PathHandlerFactory pathHandlerFactory = Scope.getCurrentScope().getSingleton(PathHandlerFactory.class);
        Resource resource = pathHandlerFactory.getResource(sqlFile);
        if (!resource.exists()) {
            throw new LiquibaseException(FileUtil.getFileNotFoundMessage(sqlFile));
        }
        return StreamUtil.readStreamAsString(resource.openInputStream());
    }

    private String handleSelect(String sqlString, Executor executor) throws DatabaseException {
        StringBuilder out = new StringBuilder();
        List<Map<String, ?>> rows = executor.queryForList(new RawParameterizedSqlStatement(sqlString));
        out.append("Output of ").append(sqlString).append(":\n");
        if (rows.isEmpty()) {
            out.append("-- Empty Resultset --\n");
        } else {
            LinkedHashSet<String> keys = new LinkedHashSet<>();
            for (Map<String, ?> row : rows) {
                keys.addAll(row.keySet());
            }
            out.append(StringUtil.join(keys, " | ")).append(" |\n");

            for (Map<String, ?> row : rows) {
                for (String key : keys) {
                    out.append(row.get(key)).append(" | ");
                }
                out.append("\n");
            }
        }
        return out.toString();
    }
}
