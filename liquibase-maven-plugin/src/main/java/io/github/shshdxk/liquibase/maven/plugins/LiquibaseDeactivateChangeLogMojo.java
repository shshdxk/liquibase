package io.github.shshdxk.liquibase.maven.plugins;

import io.github.shshdxk.liquibase.Liquibase;
import io.github.shshdxk.liquibase.command.CommandScope;
import io.github.shshdxk.liquibase.command.core.DeactivateChangelogCommandStep;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.exception.LiquibaseException;

/**
 *
 * <p>Deactivates a change log from Hub.</p>
 * 
 * @author Wesley Willard
 * @goal   deactivateChangeLog
 *
 */
public class LiquibaseDeactivateChangeLogMojo extends AbstractLiquibaseChangeLogMojo {

    @Override
    protected void performLiquibaseTask(Liquibase liquibase)
        throws LiquibaseException {
        super.performLiquibaseTask(liquibase);
        Database database = liquibase.getDatabase();

        CommandScope liquibaseCommand = new CommandScope("deactivateChangeLog");
        liquibaseCommand
                .addArgumentValue(DeactivateChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile);
        liquibaseCommand.execute();
    }
}
