package io.github.shshdxk.liquibase.statement.core;

import io.github.shshdxk.liquibase.statement.CompoundStatement;

public class RawCompoundStatement extends RawSqlStatement implements CompoundStatement {

    public RawCompoundStatement(String sql) {
        super(sql);
    }

    public RawCompoundStatement(String sql, String endDelimiter) {
        super(sql, endDelimiter);
    }
}
