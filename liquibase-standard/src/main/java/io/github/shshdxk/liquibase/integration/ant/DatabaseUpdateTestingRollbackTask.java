package io.github.shshdxk.liquibase.integration.ant;

import io.github.shshdxk.liquibase.Contexts;
import io.github.shshdxk.liquibase.Liquibase;
import io.github.shshdxk.liquibase.exception.LiquibaseException;
import org.apache.tools.ant.BuildException;

/**
 * Ant task for migrating a database forward testing rollback.
 */
public class DatabaseUpdateTestingRollbackTask extends AbstractChangeLogBasedTask {
    private boolean dropFirst;

    @Override
    public void executeWithLiquibaseClassloader() throws BuildException {
        Liquibase liquibase = getLiquibase();
        try {
            if (isDropFirst()) {
                liquibase.dropAll();
            }
            liquibase.updateTestingRollback(new Contexts(getContexts()), getLabelFilter());
        } catch (LiquibaseException e) {
            throw new BuildException("Unable to update database with a rollback test: " + e.getMessage(), e);
        }
    }

    public boolean isDropFirst() {
        return dropFirst;
    }

    public void setDropFirst(boolean dropFirst) {
        this.dropFirst = dropFirst;
    }
}
