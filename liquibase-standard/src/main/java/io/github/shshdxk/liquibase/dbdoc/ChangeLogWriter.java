package io.github.shshdxk.liquibase.dbdoc;

import io.github.shshdxk.liquibase.GlobalConfiguration;
import io.github.shshdxk.liquibase.resource.OpenOptions;
import io.github.shshdxk.liquibase.resource.Resource;
import io.github.shshdxk.liquibase.resource.ResourceAccessor;
import io.github.shshdxk.liquibase.util.StreamUtil;

import java.io.*;

public class ChangeLogWriter {
    protected Resource outputDir;
    private ResourceAccessor resourceAccessor;

    public ChangeLogWriter(ResourceAccessor resourceAccessor, Resource rootOutputDir) {
        this.outputDir = rootOutputDir.resolve("changelogs");
        this.resourceAccessor = resourceAccessor;
    }

    public void writeChangeLog(String changeLog, String physicalFilePath) throws IOException {
        String changeLogOutFile = changeLog.replace(":", "_");
        Resource xmlFile = outputDir.resolve(changeLogOutFile.toLowerCase() + ".html");

        try (BufferedWriter changeLogStream = new BufferedWriter(new OutputStreamWriter(xmlFile.openOutputStream(new OpenOptions()),
                GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue()))) {
            Resource stylesheet = resourceAccessor.get(physicalFilePath);
            if (stylesheet == null) {
                throw new IOException("Can not find " + changeLog);
            }
            try (InputStream stream = stylesheet.openInputStream()) {
                changeLogStream.write("<html><body><pre>\n");
                changeLogStream.write(StreamUtil.readStreamAsString(stream).replace("<", "&lt;").replace(">", "&gt;"));
                changeLogStream.write("\n</pre></body></html>");
            }
        }
    }
}
