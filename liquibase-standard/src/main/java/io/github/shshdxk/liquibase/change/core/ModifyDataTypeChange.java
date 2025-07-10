package io.github.shshdxk.liquibase.change.core;

import io.github.shshdxk.liquibase.change.AbstractChange;
import io.github.shshdxk.liquibase.change.ChangeMetaData;
import io.github.shshdxk.liquibase.change.DatabaseChange;
import io.github.shshdxk.liquibase.change.DatabaseChangeProperty;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.DB2Database;
import io.github.shshdxk.liquibase.database.core.Db2zDatabase;
import io.github.shshdxk.liquibase.statement.SqlStatement;
import io.github.shshdxk.liquibase.statement.core.ModifyDataTypeStatement;
import io.github.shshdxk.liquibase.statement.core.ReorganizeTableStatement;
import lombok.Setter;

@DatabaseChange(name = "modifyDataType", description = "Modify the data type of a column", priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "column")
@Setter
public class ModifyDataTypeChange extends AbstractChange {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String columnName;
    private String newDataType;

    @Override
    public boolean supports(Database database) {
        return !(database instanceof Db2zDatabase) && super.supports(database);
    }

    @Override
    public String getConfirmationMessage() {
        return tableName+"."+columnName+" datatype was changed to "+newDataType;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        ModifyDataTypeStatement modifyDataTypeStatement = new ModifyDataTypeStatement(getCatalogName(), getSchemaName(), getTableName(), getColumnName(), getNewDataType());
        if (database instanceof DB2Database) {
            return new SqlStatement[] {
                    modifyDataTypeStatement,
                    new ReorganizeTableStatement(getCatalogName(), getSchemaName(), getTableName())
            };
        } else {
            return new SqlStatement[] {
                    modifyDataTypeStatement
            };
        }
    }

    @DatabaseChangeProperty(mustEqualExisting ="column.relation.catalog", since = "3.0", description = "Name of the database catalog")
    public String getCatalogName() {
        return catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="column.relation.schema", description = "Name of the database schema")
    public String getSchemaName() {
        return schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "column.relation",
        description = "Name of the table containing the column whose data type you want to change")
    public String getTableName() {
        return tableName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "column")
    public String getColumnName() {
        return columnName;
    }

    @DatabaseChangeProperty(
        description = "Data type to convert the column to. Only modifies the data type itself and cannot define constraints")
    public String getNewDataType() {
        return newDataType;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
