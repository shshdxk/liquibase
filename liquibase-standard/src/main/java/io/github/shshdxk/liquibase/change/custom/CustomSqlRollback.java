package io.github.shshdxk.liquibase.change.custom;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.exception.CustomChangeException;
import io.github.shshdxk.liquibase.exception.RollbackImpossibleException;
import io.github.shshdxk.liquibase.statement.SqlStatement;

/**
 * Interface to implement that allows rollback of a custom sql change.
 *
 * @see CustomSqlChange
 */
public interface CustomSqlRollback {

    /**
     * Generates the SQL statements required to roll back the change
     *
     * @param database the target {@link Database} associated to this change's rollback statements
     * @return an array of {@link SqlStatement}s with the rollback statements
     * @throws CustomChangeException if an exception occurs while processing this rollback
     * @throws RollbackImpossibleException if rollback is not supported for this change
     */
    SqlStatement[] generateRollbackStatements(Database database) throws CustomChangeException, RollbackImpossibleException;

}
