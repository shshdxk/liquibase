package io.github.shshdxk.liquibase.change.custom;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.exception.CustomChangeException;
import io.github.shshdxk.liquibase.exception.RollbackImpossibleException;

/**
 * Interface to implement that allows rollback of a custom task change.
 *
 * @see CustomTaskChange
 */
public interface CustomTaskRollback {

    /**
     * Method called to rollback the change.
     * @param database Database the change is being executed against.
     * @throws CustomChangeException an exception occurs while processing this rollback
     * @throws RollbackImpossibleException if rollback is not supported for this change
     */
    void rollback(Database database) throws CustomChangeException, RollbackImpossibleException;
}
