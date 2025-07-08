package io.github.shshdxk.liquibase.maven.plugins;

import io.github.shshdxk.liquibase.Contexts;
import io.github.shshdxk.liquibase.GlobalConfiguration;
import io.github.shshdxk.liquibase.LabelExpression;
import io.github.shshdxk.liquibase.Liquibase;
import io.github.shshdxk.liquibase.exception.LiquibaseException;
import io.github.shshdxk.liquibase.exception.UnexpectedLiquibaseException;

import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

/**
 *
 * <p>Print a list of changesets that have been executed but are not in the current changelog</p>
 * 
 * @author Wesley Willard
 * @goal   unexpectedChangeSets
 *
 */
public class LiquibaseUnexpectedChangeSetsMojo extends AbstractLiquibaseChangeLogMojo {
    @Override
    protected void performLiquibaseTask(Liquibase liquibase)
        throws LiquibaseException {
        try {
            liquibase.reportUnexpectedChangeSets(true, new Contexts(contexts), new LabelExpression((getLabelFilter())), new OutputStreamWriter(System.out, GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue()));
        }
        catch (UnsupportedEncodingException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }
}
