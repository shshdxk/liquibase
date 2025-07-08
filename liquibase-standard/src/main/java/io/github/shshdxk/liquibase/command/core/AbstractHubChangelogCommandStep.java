package io.github.shshdxk.liquibase.command.core;

import io.github.shshdxk.liquibase.Scope;
import io.github.shshdxk.liquibase.changelog.ChangeLogParameters;
import io.github.shshdxk.liquibase.changelog.DatabaseChangeLog;
import io.github.shshdxk.liquibase.command.AbstractCommandStep;
import io.github.shshdxk.liquibase.exception.LiquibaseException;
import io.github.shshdxk.liquibase.parser.ChangeLogParser;
import io.github.shshdxk.liquibase.parser.ChangeLogParserFactory;
import io.github.shshdxk.liquibase.resource.ResourceAccessor;

public abstract class AbstractHubChangelogCommandStep extends AbstractCommandStep {

    protected DatabaseChangeLog parseChangeLogFile(String changeLogFile) throws LiquibaseException {
        ResourceAccessor resourceAccessor = Scope.getCurrentScope().getResourceAccessor();
        ChangeLogParser parser = ChangeLogParserFactory.getInstance().getParser(changeLogFile, resourceAccessor);
        ChangeLogParameters changeLogParameters = new ChangeLogParameters();
        return parser.parse(changeLogFile, changeLogParameters, resourceAccessor);
    }
}
