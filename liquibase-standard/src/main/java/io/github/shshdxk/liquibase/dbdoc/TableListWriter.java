package io.github.shshdxk.liquibase.dbdoc;

import io.github.shshdxk.liquibase.resource.Resource;

public class TableListWriter extends HTMLListWriter {

    public TableListWriter(Resource outputDir) {
        super("Current Tables", "currenttables.html", "tables", outputDir);
    }


}
