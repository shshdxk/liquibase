package io.github.shshdxk.liquibase.logging.core;

import io.github.shshdxk.liquibase.configuration.ConfigurationDefinition;
import io.github.shshdxk.liquibase.configuration.AutoloadedConfigurations;
import io.github.shshdxk.liquibase.logging.LogService;

/**
 * Configuration container for {@link LogService} properties
 * @deprecated not in use anywhere in Liquibase code and has been superseded by log-level and its associated command
 * line parameters
 */
@Deprecated
public class DefaultLoggerConfiguration implements AutoloadedConfigurations {

    @Deprecated
    public static ConfigurationDefinition<String> LOG_LEVEL;

    static {
        ConfigurationDefinition.Builder builder = new ConfigurationDefinition.Builder("liquibase.defaultlogger");
        LOG_LEVEL = builder.define("level", String.class)
                .setDescription("Logging level")
                .setDefaultValue("INFO")
                .setInternal(true)
                .build();
    }
}
