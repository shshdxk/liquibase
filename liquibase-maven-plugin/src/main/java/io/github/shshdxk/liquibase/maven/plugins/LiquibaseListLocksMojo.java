package io.github.shshdxk.liquibase.maven.plugins;

import io.github.shshdxk.liquibase.Liquibase;
import io.github.shshdxk.liquibase.exception.LiquibaseException;

/**
 * <p>Lists all Liquibase updater locks on the current database.</p>
 * 
 * @author JAmes Atwill
 * @goal listLocks
 */
public class LiquibaseListLocksMojo extends AbstractLiquibaseMojo {

	@Override
	protected void performLiquibaseTask(Liquibase liquibase)
			throws LiquibaseException {
		liquibase.reportLocks(System.out);
	}

}
