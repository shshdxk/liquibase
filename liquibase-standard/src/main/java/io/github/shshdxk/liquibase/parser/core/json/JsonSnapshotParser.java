package io.github.shshdxk.liquibase.parser.core.json;

import io.github.shshdxk.liquibase.parser.core.yaml.YamlSnapshotParser;

public class JsonSnapshotParser extends YamlSnapshotParser {

    @Override
    protected String[] getSupportedFileExtensions() {
        return new String[] {"json"};
    }
}
