package io.github.shshdxk.liquibase.maven.plugins;

import io.github.shshdxk.liquibase.Contexts;
import io.github.shshdxk.liquibase.LabelExpression;
import io.github.shshdxk.liquibase.Liquibase;
import io.github.shshdxk.liquibase.exception.LiquibaseException;
import org.apache.maven.plugin.MojoFailureException;
import io.github.shshdxk.liquibase.maven.property.PropertyElement;

/**
 *
 * <p>Marks all unapplied changes up to and include a specified tag to the database as applied in the change log.</p>
 * 
 * @author James Atwill
 * @goal   changelogSyncToTag
 *
 */
public class LiquibaseChangeLogSyncToTagMojo extends AbstractLiquibaseChangeLogMojo {

	/**
	 * Update to the changeSet with the given tag command.
	 * @parameter property="liquibase.toTag"
	 */
    @PropertyElement
	protected String toTag;

    @Override
    protected void checkRequiredParametersAreSpecified() throws MojoFailureException {
        if (toTag == null || toTag.isEmpty()) {
            throw new MojoFailureException("\nYou must specify a changelog tag.");
        }
    }

    @Override
    protected void performLiquibaseTask(Liquibase liquibase)
             throws LiquibaseException {
        super.performLiquibaseTask(liquibase);
        liquibase.changeLogSync(toTag, new Contexts(contexts), new LabelExpression(getLabelFilter()));
    }
}
