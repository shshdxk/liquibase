package io.github.shshdxk.liquibase.maven.plugins;

import io.github.shshdxk.liquibase.Contexts;
import io.github.shshdxk.liquibase.LabelExpression;
import io.github.shshdxk.liquibase.Liquibase;
import io.github.shshdxk.liquibase.GlobalConfiguration;
import io.github.shshdxk.liquibase.exception.LiquibaseException;
import io.github.shshdxk.liquibase.exception.UnexpectedLiquibaseException;

import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

/**
 * <p>Prints which changesets need to be applied to the database.</p>
 * 
 * @author JAmes Atwill
 * @goal status
 */
public class LiquibaseReportStatusMojo extends AbstractLiquibaseChangeLogMojo {

	@Override
	protected void performLiquibaseTask(Liquibase liquibase)
			throws LiquibaseException {
		try {
			liquibase.reportStatus(true, new Contexts(contexts), new LabelExpression(getLabelFilter()), new OutputStreamWriter(System.out, GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue()));
		} catch (UnsupportedEncodingException e) {
			throw new UnexpectedLiquibaseException(e);
		}
	}

}
