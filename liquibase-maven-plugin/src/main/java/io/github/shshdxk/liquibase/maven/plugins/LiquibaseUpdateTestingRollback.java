package io.github.shshdxk.liquibase.maven.plugins;

import io.github.shshdxk.liquibase.Contexts;
import io.github.shshdxk.liquibase.LabelExpression;
import io.github.shshdxk.liquibase.Liquibase;
import io.github.shshdxk.liquibase.Scope;
import io.github.shshdxk.liquibase.exception.LiquibaseException;

/**
 * <p>Applies the DatabaseChangeLogs to the database, testing rollback. This is
 * done by updating the database, rolling it back then updating it again.</p>
 *
 * @description Liquibase UpdateTestingRollback Maven plugin
 * @goal updateTestingRollback
 */
public class LiquibaseUpdateTestingRollback extends AbstractLiquibaseUpdateMojo {

    @Override
    protected void doUpdate(Liquibase liquibase) throws LiquibaseException {
        try {
            Scope.child("rollbackOnError", rollbackOnError, () -> {
                liquibase.updateTestingRollback(new Contexts(contexts), new LabelExpression(getLabelFilter()));
            });
        } catch (Exception exception) {
            if (exception instanceof LiquibaseException) {
                throw (LiquibaseException) exception;
            } else {
                throw new LiquibaseException(exception);
            }
        }
    }
}
