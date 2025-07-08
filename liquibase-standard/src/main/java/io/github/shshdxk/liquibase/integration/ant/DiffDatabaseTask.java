package io.github.shshdxk.liquibase.integration.ant;

import io.github.shshdxk.liquibase.GlobalConfiguration;
import io.github.shshdxk.liquibase.diff.DiffResult;
import io.github.shshdxk.liquibase.diff.output.report.DiffToReport;
import io.github.shshdxk.liquibase.exception.DatabaseException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.util.FileUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class DiffDatabaseTask extends AbstractDatabaseDiffTask {
    private FileResource outputFile;

    private String outputEncoding = GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue();

    @Override
    public void executeWithLiquibaseClassloader() throws BuildException {
        PrintStream printStream = null;
        try {
            printStream = new PrintStream(outputFile.getOutputStream(), true, getOutputEncoding());
            DiffResult diffResult = getDiffResult();
            DiffToReport diffReport = new DiffToReport(diffResult, printStream);
            log("Writing diff report " + outputFile.toString(), Project.MSG_INFO);
            diffReport.print();
        } catch (DatabaseException e) {
            throw new BuildException("Unable to make diff report: " + e.getMessage(), e);
        } catch (UnsupportedEncodingException e) {
            throw new BuildException("Unable to make diff report. Encoding [" + outputEncoding + "] is not supported.", e);
        } catch (IOException e) {
            throw new BuildException("Unable to make diff report. Error opening output stream.", e);
        } finally {
            FileUtils.close(printStream);
        }
    }

    @Override
    protected void validateParameters() {
        super.validateParameters();

        if(outputFile == null) {
            throw new BuildException("Unable to make diff report. Output file is required.");
        }
    }

    public void setOutputFile(FileResource outputFile) {
        this.outputFile = outputFile;
    }

    public String getOutputEncoding() {
        return outputEncoding;
    }

    public void setOutputEncoding(String outputEncoding) {
        this.outputEncoding = outputEncoding;
    }
}
