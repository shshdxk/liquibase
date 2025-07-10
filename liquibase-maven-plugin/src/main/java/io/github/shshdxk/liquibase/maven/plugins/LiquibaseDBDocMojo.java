package io.github.shshdxk.liquibase.maven.plugins;

import io.github.shshdxk.liquibase.Liquibase;
import io.github.shshdxk.liquibase.exception.LiquibaseException;
import io.github.shshdxk.liquibase.maven.property.PropertyElement;

/**
 * <p>Generates dbDocs against the database.</p>
 *
 * @author Ryan Connolly
 * @goal dbDoc
 */
public class LiquibaseDBDocMojo extends AbstractLiquibaseChangeLogMojo {

    /**
     * @parameter
     *      property="liquibase.outputDirectory"
     *      default-value="${project.build.directory}/liquibase/dbDoc"
     */
    @PropertyElement
    private String outputDirectory;


    @Override
    protected void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException
    {
        liquibase.generateDocumentation(outputDirectory);
    }


    public String getOutputDirectory()
    {
        return outputDirectory;
    }

}
