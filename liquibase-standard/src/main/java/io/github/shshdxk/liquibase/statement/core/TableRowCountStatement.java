package io.github.shshdxk.liquibase.statement.core;

import io.github.shshdxk.liquibase.statement.AbstractSqlStatement;

public class TableRowCountStatement extends AbstractSqlStatement {

    private DatabaseTableIdentifier databaseTableIdentifier = new DatabaseTableIdentifier(null, null, null);

    public TableRowCountStatement(String catalogName, String schemaName, String tableName) {
        this.databaseTableIdentifier.setCatalogName(catalogName);
        this.databaseTableIdentifier.setSchemaName(schemaName);
        this.databaseTableIdentifier.setTableName(tableName);
    }

    public String getCatalogName() {
        return databaseTableIdentifier.getCatalogName();
    }

    public String getSchemaName() {
        return databaseTableIdentifier.getSchemaName();
    }

    public String getTableName() {
        return databaseTableIdentifier.getTableName();
    }
}
