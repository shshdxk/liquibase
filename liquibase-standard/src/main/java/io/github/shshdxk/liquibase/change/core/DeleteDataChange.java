package io.github.shshdxk.liquibase.change.core;

import io.github.shshdxk.liquibase.change.ChangeMetaData;
import io.github.shshdxk.liquibase.change.ColumnConfig;
import io.github.shshdxk.liquibase.change.DatabaseChange;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.parser.core.ParsedNode;
import io.github.shshdxk.liquibase.parser.core.ParsedNodeException;
import io.github.shshdxk.liquibase.resource.ResourceAccessor;
import io.github.shshdxk.liquibase.statement.SqlStatement;
import io.github.shshdxk.liquibase.statement.core.DeleteStatement;

@DatabaseChange(name = "delete", description = "Deletes data from an existing table", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table")
public class DeleteDataChange extends AbstractModifyDataChange {


    @Override
    public SqlStatement[] generateStatements(Database database) {

        DeleteStatement statement = new DeleteStatement(getCatalogName(), getSchemaName(), getTableName());

        statement.setWhere(where);

        for (ColumnConfig whereParam : whereParams) {
            if (whereParam.getName() != null) {
                statement.addWhereColumnName(whereParam.getName());
            }
            statement.addWhereParameter(whereParam.getValueObject());
        }

        return new SqlStatement[]{
                statement
        };
    }

    @Override
    public String getConfirmationMessage() {
        return "Data deleted from " + getTableName();
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    protected void customLoadLogic(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        ParsedNode whereParams = parsedNode.getChild(null, "whereParams");
        if (whereParams != null) {
            for (ParsedNode param : whereParams.getChildren(null, "param")) {
                ColumnConfig columnConfig = new ColumnConfig();
                try {
                    columnConfig.load(param, resourceAccessor);
                } catch (ParsedNodeException e) {
                    e.printStackTrace();
                }
                addWhereParam(columnConfig);
            }
        }
    }

}
