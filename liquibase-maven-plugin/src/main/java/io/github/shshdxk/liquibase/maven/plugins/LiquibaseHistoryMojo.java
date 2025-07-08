package io.github.shshdxk.liquibase.maven.plugins;

import io.github.shshdxk.liquibase.Liquibase;
import io.github.shshdxk.liquibase.command.CommandScope;
import io.github.shshdxk.liquibase.command.core.InternalHistoryCommandStep;
import io.github.shshdxk.liquibase.exception.LiquibaseException;

/**
 * <p>Outputs history of deployments against the configured database.</p>
 *
 * @goal history
 */
public class LiquibaseHistoryMojo extends AbstractLiquibaseMojo {

    @Override
    protected void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException {
      CommandScope historyCommand = new CommandScope(InternalHistoryCommandStep.COMMAND_NAME);

      historyCommand.addArgumentValue(InternalHistoryCommandStep.DATABASE_ARG, getLiquibase().getDatabase());

      historyCommand.execute();
    }
}
