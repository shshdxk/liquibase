package io.github.shshdxk.liquibase.dbdoc;

import io.github.shshdxk.liquibase.resource.Resource;

public class ChangeLogListWriter extends HTMLListWriter {

    public ChangeLogListWriter(Resource outputDir) {
        super("All Change Logs", "changelogs.html", "changelogs", outputDir);
    }

    @Override
    public String getTargetExtension() {
        return ".html";
    }

}
