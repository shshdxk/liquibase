package io.github.shshdxk.liquibase.parser.core.json;

import io.github.shshdxk.liquibase.parser.core.ParserSupportedFileExtension;
import io.github.shshdxk.liquibase.parser.core.yaml.YamlChangeLogParser;

public class JsonChangeLogParser extends YamlChangeLogParser {

    @Override
    protected String[] getSupportedFileExtensions() {
        return ParserSupportedFileExtension.JSON_SUPPORTED_EXTENSIONS.toArray(new String[0]);
    }
}
