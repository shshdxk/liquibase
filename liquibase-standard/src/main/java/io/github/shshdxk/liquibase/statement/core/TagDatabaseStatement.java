package io.github.shshdxk.liquibase.statement.core;

import io.github.shshdxk.liquibase.statement.AbstractSqlStatement;

public class TagDatabaseStatement extends AbstractSqlStatement {

    private String tag;

    public TagDatabaseStatement(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
