package io.github.shshdxk.liquibase.serializer.core.json;

import io.github.shshdxk.liquibase.changelog.ChangeLogChild;
import io.github.shshdxk.liquibase.GlobalConfiguration;
import io.github.shshdxk.liquibase.serializer.core.yaml.YamlChangeLogSerializer;
import io.github.shshdxk.liquibase.util.StringUtil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

public class JsonChangeLogSerializer extends YamlChangeLogSerializer {

    @Override
    public <T extends ChangeLogChild> void write(List<T> children, OutputStream out) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue()));
        writer.write("{ \"databaseChangeLog\": [\n");
        int i = 0;
        for (T child : children) {
            String serialized = serialize(child, true);
            if (++i < children.size()) {
                serialized = serialized.replaceFirst("}\\s*$", "},\n");
            }
            writer.write(StringUtil.indent(serialized, 2));
            writer.write("\n");
        }
        writer.write("]}");
        writer.flush();
    }

    @Override
    public String[] getValidFileExtensions() {
        return new String[]{
                "json"
        };
    }

}
