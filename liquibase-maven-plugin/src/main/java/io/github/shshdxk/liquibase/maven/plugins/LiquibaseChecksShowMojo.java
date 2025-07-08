package io.github.shshdxk.liquibase.maven.plugins;

import io.github.shshdxk.liquibase.Liquibase;
import io.github.shshdxk.liquibase.command.CommandScope;
import io.github.shshdxk.liquibase.exception.CommandExecutionException;
import io.github.shshdxk.liquibase.util.StringUtil;

/**
 * List available checks, their configuration options, and current settings
 *
 * @goal checks.show
 */
public class LiquibaseChecksShowMojo extends AbstractLiquibaseChecksMojo {

    @Override
    protected void performLiquibaseTask(Liquibase liquibase) throws CommandExecutionException {
        CommandScope liquibaseCommand = new CommandScope("checks", "show");
        if (StringUtil.isNotEmpty(checksSettingsFile)) {
            liquibaseCommand.addArgumentValue("checksSettingsFile", checksSettingsFile);
        }
        liquibaseCommand.addArgumentValue("checksIntegration", "maven");
        liquibaseCommand.execute();
    }
}
