package io.github.shshdxk.liquibase.statement.core;

import io.github.shshdxk.liquibase.statement.CallableSqlStatement;

public class RawCallStatement extends RawSqlStatement implements CallableSqlStatement {

    public RawCallStatement(String sql) {
        super(sql);
    }

    public RawCallStatement(String sql, String endDelimiter) {
        super(sql, endDelimiter);
    }
}
