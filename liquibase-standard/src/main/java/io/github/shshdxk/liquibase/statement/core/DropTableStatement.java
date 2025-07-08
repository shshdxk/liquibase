package io.github.shshdxk.liquibase.statement.core;

import io.github.shshdxk.liquibase.statement.AbstractSqlStatement;

public class DropTableStatement extends AbstractSqlStatement {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private boolean cascadeConstraints;

    public DropTableStatement(String catalogName, String schemaName, String tableName, boolean cascadeConstraints) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.cascadeConstraints = cascadeConstraints;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public boolean isCascadeConstraints() {
        return cascadeConstraints;
    }
}
