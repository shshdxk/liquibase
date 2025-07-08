package io.github.shshdxk.liquibase.maven.plugins;

import io.github.shshdxk.liquibase.Contexts;
import io.github.shshdxk.liquibase.LabelExpression;
import io.github.shshdxk.liquibase.Liquibase;
import io.github.shshdxk.liquibase.exception.LiquibaseException;

/**
 * <p>Marks all unapplied changes to the database as applied in the change log.</p>
 * 
 * @author James Atwill
 * @goal   changelogSync
 */
public class LiquibaseChangeLogSyncMojo extends AbstractLiquibaseChangeLogMojo {

    @Override
    protected void performLiquibaseTask(Liquibase liquibase)
  			throws LiquibaseException {
        super.performLiquibaseTask(liquibase);
	    	liquibase.changeLogSync(new Contexts(contexts), new LabelExpression(getLabelFilter()));
    }
}
