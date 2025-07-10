package io.github.shshdxk.liquibase.maven.plugins;

import io.github.shshdxk.liquibase.Liquibase;
import io.github.shshdxk.liquibase.Scope;
import io.github.shshdxk.liquibase.command.CommandScope;
import io.github.shshdxk.liquibase.exception.CommandExecutionException;
import org.apache.commons.lang3.StringUtils;

/**
 * List available checks, their configuration options, and current settings
 *
 * @goal checks.show
 */
public class LiquibaseChecksShowMojo extends AbstractLiquibaseChecksMojo {

    @Override
    protected void performLiquibaseTask(Liquibase liquibase) throws CommandExecutionException {
        try {
            CommandScope liquibaseCommand = new CommandScope("checks", "show");
            if (! doesMarkerClassExist()) {
                throw new CommandExecutionException(Scope.CHECKS_MESSAGE);
            }
            if (StringUtils.isNotEmpty(checksSettingsFile)) {
                liquibaseCommand.addArgumentValue("checksSettingsFile", checksSettingsFile);
            }
            liquibaseCommand.addArgumentValue("checksIntegration", "maven");
            liquibaseCommand.execute();
        } catch (IllegalArgumentException e) {
            throw new CommandExecutionException(Scope.CHECKS_MESSAGE);
        }
    }
}
