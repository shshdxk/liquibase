package io.github.shshdxk.liquibase.parser.core.yaml;

import io.github.shshdxk.liquibase.Scope;
import io.github.shshdxk.liquibase.logging.Logger;
import io.github.shshdxk.liquibase.parser.LiquibaseParser;
import io.github.shshdxk.liquibase.resource.ResourceAccessor;
import io.github.shshdxk.liquibase.util.SnakeYamlUtil;
import org.yaml.snakeyaml.LoaderOptions;

public abstract class YamlParser implements LiquibaseParser {

    protected Logger log = Scope.getCurrentScope().getLog(getClass());

    public static LoaderOptions createLoaderOptions() {
        LoaderOptions options = new LoaderOptions();
        SnakeYamlUtil.setCodePointLimitSafely(options, Integer.MAX_VALUE);
        SnakeYamlUtil.setProcessCommentsSafely(options, false);
        options.setAllowRecursiveKeys(false);
        return options;
    }

    public boolean supports(String changeLogFile, ResourceAccessor resourceAccessor) {
        for (String extension : getSupportedFileExtensions()) {
            if (changeLogFile.toLowerCase().endsWith("." + extension)) {
                return true;
            }
        }
        return false;
    }

    protected String[] getSupportedFileExtensions() {
        return new String[] {"yaml", "yml"};
    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }


}
