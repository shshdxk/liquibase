package io.github.shshdxk.liquibase.change.core;

import io.github.shshdxk.liquibase.change.AbstractChange;
import io.github.shshdxk.liquibase.change.ChangeMetaData;
import io.github.shshdxk.liquibase.change.DatabaseChange;
import io.github.shshdxk.liquibase.change.DatabaseChangeProperty;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.serializer.LiquibaseSerializable;
import io.github.shshdxk.liquibase.statement.SqlStatement;
import io.github.shshdxk.liquibase.statement.core.DropProcedureStatement;

@DatabaseChange(name = "dropProcedure", description = "Drops an existing procedure", priority = ChangeMetaData.PRIORITY_DEFAULT+100,
    appliesTo = "storedProcedure")
public class DropProcedureChange extends AbstractChange {

    private String catalogName;
    private String schemaName;
    private String procedureName;

    @DatabaseChangeProperty(mustEqualExisting ="storedProcedure.catalog", description = "Name of the database catalog")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="storedProcedure.schema", description = "Name of the database schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "storedProcedure", description = "Name of the stored procedure to drop",
        exampleValue = "new_customer")
    public String getProcedureName() {
        return procedureName;
    }

    public void setProcedureName(String procedureName) {
        this.procedureName = procedureName;
    }

    @Override
    public String getConfirmationMessage() {
        return "Stored Procedure "+getProcedureName()+" dropped";
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[]{
                new DropProcedureStatement(getCatalogName(), getSchemaName(), getProcedureName())
        };
    }

    @Override
    public String getSerializedObjectNamespace() {
        return LiquibaseSerializable.STANDARD_CHANGELOG_NAMESPACE;
    }
}
