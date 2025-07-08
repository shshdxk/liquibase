package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.DB2Database;
import io.github.shshdxk.liquibase.database.core.MSSQLDatabase;
import io.github.shshdxk.liquibase.database.core.OracleDatabase;
import io.github.shshdxk.liquibase.database.core.PostgresDatabase;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sql.UnparsedSql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.statement.core.SetViewRemarksStatement;
import io.github.shshdxk.liquibase.structure.core.Relation;
import io.github.shshdxk.liquibase.structure.core.View;
import io.github.shshdxk.liquibase.util.StringUtil;

public class SetViewRemarksGenerator extends AbstractSqlGenerator<SetViewRemarksStatement> {

    @Override
    public boolean supports(SetViewRemarksStatement statement, Database database) {
        return (database instanceof OracleDatabase) || (database instanceof PostgresDatabase) || (database instanceof MSSQLDatabase) || (database instanceof DB2Database);
    }

    @Override
    public ValidationErrors validate(SetViewRemarksStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("viewName", statement.getViewName());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(SetViewRemarksStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String sql = "";
        String remarksEscaped = database.escapeStringForDatabase(StringUtil.trimToEmpty(statement.getRemarks()));
        if (database instanceof OracleDatabase || database instanceof PostgresDatabase || database instanceof DB2Database) {
            String sqlPlaceholder = "COMMENT ON %s %s IS '%s'";
            String targetNameEscaped = database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getViewName());
            String targetObject;
            if (database instanceof OracleDatabase || database instanceof DB2Database) {
                //Oracle and DB2 consider views as tables for their comment syntax
                targetObject = "TABLE";
            } else {
                targetObject = "VIEW";
            }
            sql = String.format(sqlPlaceholder, targetObject, targetNameEscaped, remarksEscaped);
        } else if (database instanceof MSSQLDatabase) {
            String schemaName = statement.getSchemaName();
            if (schemaName == null) {
                schemaName = database.getDefaultSchemaName() != null ? database.getDefaultSchemaName() : "dbo";
            }
            String viewName = statement.getViewName();
            String qualifiedTableName = String.format("%s.%s", schemaName, statement.getViewName());

            sql = "IF EXISTS( " +
                    " SELECT extended_properties.value" +
                    " FROM sys.extended_properties" +
                    " WHERE major_id = OBJECT_ID('" + qualifiedTableName + "')" +
                    " AND name = N'MS_DESCRIPTION'" +
                    " AND minor_id = 0" +
                    " )" +
                    " BEGIN " +
                    " EXEC sys.sp_updateextendedproperty @name = N'MS_Description'" +
                    " , @value = N'" + remarksEscaped + "'" +
                    " , @level0type = N'SCHEMA'" +
                    " , @level0name = N'" + schemaName + "'" +
                    " , @level1type = N'VIEW'" +
                    " , @level1name = N'" + viewName + "'" +
                    " END " +
                    " ELSE " +
                    " BEGIN " +
                    " EXEC sys.sp_addextendedproperty @name = N'MS_Description'" +
                    " , @value = N'" + remarksEscaped + "'" +
                    " , @level0type = N'SCHEMA'" +
                    " , @level0name = N'" + schemaName + "'" +
                    " , @level1type = N'VIEW'" +
                    " , @level1name = N'" + viewName + "'" +
                    " END";
        }
        return new Sql[]{new UnparsedSql(sql, getAffectedTable(statement))};
    }

    protected Relation getAffectedTable(SetViewRemarksStatement statement) {
        return new View().setName(statement.getViewName()).setSchema(statement.getCatalogName(), statement.getSchemaName());
    }
}
