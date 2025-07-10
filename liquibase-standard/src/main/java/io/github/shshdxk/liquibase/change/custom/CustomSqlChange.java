package io.github.shshdxk.liquibase.change.custom;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.exception.CustomChangeException;
import io.github.shshdxk.liquibase.statement.SqlStatement;

/**
 * Interface to implement when creating a custom change that generates SQL.  When updating a database,
 * implementing this interface is preferred over CustomTaskChange because the SQL can either be executed
 * directly or saved to a text file for later use depending on the migration mode used.  To allow
 * the change to be rolled back, also implement the CustomSqlRollback interface.  If your change requires sql-based
 * logic and non-sql-based logic, it is best to create a changeset that contains a mix of CustomSqlChange and CustomTaskChange calls.
 *
 * @see CustomSqlRollback
 * @see CustomTaskChange
  */
public interface CustomSqlChange extends CustomChange {
    /**
     * Generates the SQL statements required to run the change
     *
     * @param database the target {@link Database} associated to this change's statements
     * @return an array of {@link SqlStatement}s with the statements
     * @throws CustomChangeException if an exception occurs while processing this change
     */
    SqlStatement[] generateStatements(Database database) throws CustomChangeException;

}
