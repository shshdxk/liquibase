package io.github.shshdxk.liquibase.maven.plugins;

import io.github.shshdxk.liquibase.Liquibase;
import io.github.shshdxk.liquibase.command.CommandScope;
import io.github.shshdxk.liquibase.command.core.HistoryCommandStep;
import io.github.shshdxk.liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep;
import io.github.shshdxk.liquibase.exception.LiquibaseException;

/**
 * <p>Outputs history of deployments against the configured database.</p>
 *
 * @goal history
 */
public class LiquibaseHistoryMojo extends AbstractLiquibaseMojo {

    @Override
    protected void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException {
      CommandScope historyCommand = new CommandScope(HistoryCommandStep.COMMAND_NAME);

      historyCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, getLiquibase().getDatabase());

      historyCommand.execute();
    }
}
