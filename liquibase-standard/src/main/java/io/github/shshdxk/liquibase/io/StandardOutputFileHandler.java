package io.github.shshdxk.liquibase.io;

import io.github.shshdxk.liquibase.Scope;
import io.github.shshdxk.liquibase.command.CommandScope;
import io.github.shshdxk.liquibase.resource.OpenOptions;
import io.github.shshdxk.liquibase.resource.PathHandlerFactory;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Standard Liquibase behavior of redirecting console output to output-file.
 */
public class StandardOutputFileHandler implements OutputFileHandler {

    protected OutputStream outputStream;

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public void create(String outputFile, CommandScope commandScope) throws IOException {
        final PathHandlerFactory pathHandlerFactory = Scope.getCurrentScope().getSingleton(PathHandlerFactory.class);
        outputStream = pathHandlerFactory.openResourceOutputStream(outputFile, new OpenOptions());
        commandScope.setOutput(outputStream);
    }

    @Override
    public void close() throws IOException {
        if (outputStream != null) {
            outputStream.flush();
            outputStream.close();
        }
    }
}
