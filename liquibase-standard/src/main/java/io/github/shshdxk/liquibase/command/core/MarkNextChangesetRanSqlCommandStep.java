package io.github.shshdxk.liquibase.command.core;

import io.github.shshdxk.liquibase.command.CommandDefinition;
import io.github.shshdxk.liquibase.command.CommandResultsBuilder;
import io.github.shshdxk.liquibase.command.CommandScope;
import io.github.shshdxk.liquibase.command.core.helpers.DatabaseChangelogCommandStep;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.util.LoggingExecutorTextUtil;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class MarkNextChangesetRanSqlCommandStep extends MarkNextChangesetRanCommandStep {

    public static final String[] COMMAND_NAME = {"markNextChangesetRanSql"};

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Writes the SQL used to mark the next change you apply as executed in your database");
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        List<Class<?>> dependencies = new ArrayList<>();
        dependencies.add(Writer.class);
        dependencies.addAll(super.requiredDependencies());
        return dependencies;
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        final CommandScope commandScope = resultsBuilder.getCommandScope();
        final Database database = (Database) commandScope.getDependency(Database.class);
        final String changelogFile = commandScope.getArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG);
        LoggingExecutorTextUtil.outputHeader("SQL to add the next changeset to database history table", database, changelogFile);
        super.run(resultsBuilder);
    }
}
