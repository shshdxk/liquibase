package io.github.shshdxk.liquibase.maven.plugins;

import io.github.shshdxk.liquibase.Liquibase;
import io.github.shshdxk.liquibase.command.CommandScope;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.exception.LiquibaseException;

/**
 * <p>Test connection to the configured database.</p>
 *
 * @goal connect
 */
public class LiquibaseConnectMojo extends AbstractLiquibaseMojo {
    @Override
    protected void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException {
      CommandScope connectCommand = new CommandScope("connect");
      connectCommand.addArgumentValue("url", url);
      connectCommand.addArgumentValue("username", username);
      connectCommand.addArgumentValue("password", password);
      connectCommand.addArgumentValue("catalog", defaultCatalogName);
      connectCommand.addArgumentValue("schema", defaultSchemaName);
      connectCommand.provideDependency(Database.class, getLiquibase().getDatabase());
      connectCommand.execute();
    }
}
