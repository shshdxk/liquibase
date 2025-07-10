package io.github.shshdxk.liquibase.serializer.core.json;

import io.github.shshdxk.liquibase.serializer.core.yaml.YamlSnapshotSerializer;

public class JsonSnapshotSerializer extends YamlSnapshotSerializer {

    @Override
    public String[] getValidFileExtensions() {
        return new String[]{
                "json"
        };
    }

}
