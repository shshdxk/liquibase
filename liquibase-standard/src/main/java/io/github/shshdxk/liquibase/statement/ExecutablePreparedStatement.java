package io.github.shshdxk.liquibase.statement;

import io.github.shshdxk.liquibase.database.PreparedStatementFactory;
import io.github.shshdxk.liquibase.exception.DatabaseException;

/**
 * To be implemented by instances that use a prepared statement for execution
 */
public interface ExecutablePreparedStatement extends SqlStatement {
    /**
     * Execute the prepared statement
     * @param factory for creating a <code>PreparedStatement</code> object
     * @throws DatabaseException
     */
    void execute(PreparedStatementFactory factory) throws DatabaseException;
}
