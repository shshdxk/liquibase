package io.github.shshdxk.liquibase.change.core;

import io.github.shshdxk.liquibase.change.AbstractChange;
import io.github.shshdxk.liquibase.change.ChangeMetaData;
import io.github.shshdxk.liquibase.change.DatabaseChange;
import io.github.shshdxk.liquibase.change.DatabaseChangeProperty;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.statement.SqlStatement;
import io.github.shshdxk.liquibase.statement.core.SetTableRemarksStatement;

@DatabaseChange(name = "setTableRemarks", description = "Set remarks on a table", priority = ChangeMetaData.PRIORITY_DEFAULT)
public class SetTableRemarksChange extends AbstractChange {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String remarks;

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.addAll(super.validate(database));

        return validationErrors;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[] {
                new SetTableRemarksStatement(catalogName, schemaName, tableName, remarks)
        };
    }

    @DatabaseChangeProperty(description = "Name of the database catalog")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(description = "Name of the database schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(description = "Name of the table to set remarks on")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @DatabaseChangeProperty(description = "A brief descriptive comment stored in the table metadata")
    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public String getConfirmationMessage() {
        return "Remarks set on " + tableName;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
