package io.github.shshdxk.liquibase.statement;

import io.github.shshdxk.liquibase.sql.Sql;

/**
 * Marker interface to indicate that a {@link SqlStatement} can generate different {@link Sql}
 * which should be specifically executed
 */
public interface CompoundStatement extends SqlStatement {
}
