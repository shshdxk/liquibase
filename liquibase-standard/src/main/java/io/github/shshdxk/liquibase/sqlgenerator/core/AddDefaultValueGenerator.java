package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.HsqlDatabase;
import io.github.shshdxk.liquibase.database.core.MySQLDatabase;
import io.github.shshdxk.liquibase.datatype.DataTypeFactory;
import io.github.shshdxk.liquibase.datatype.LiquibaseDataType;
import io.github.shshdxk.liquibase.datatype.core.BooleanType;
import io.github.shshdxk.liquibase.datatype.core.CharType;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.exception.Warnings;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sql.UnparsedSql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.statement.DatabaseFunction;
import io.github.shshdxk.liquibase.statement.SequenceNextValueFunction;
import io.github.shshdxk.liquibase.statement.core.AddDefaultValueStatement;
import io.github.shshdxk.liquibase.structure.core.Column;
import io.github.shshdxk.liquibase.structure.core.Schema;
import io.github.shshdxk.liquibase.structure.core.Table;

public class AddDefaultValueGenerator extends AbstractSqlGenerator<AddDefaultValueStatement> {

    @Override
    public ValidationErrors validate(AddDefaultValueStatement addDefaultValueStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        Object defaultValue = addDefaultValueStatement.getDefaultValue();

        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("defaultValue", defaultValue, true);
        validationErrors.checkRequiredField("columnName", addDefaultValueStatement.getColumnName());
        validationErrors.checkRequiredField("tableName", addDefaultValueStatement.getTableName());
        if (!database.supportsSequences() && (defaultValue instanceof SequenceNextValueFunction)) {
            validationErrors.addError("Database "+database.getShortName()+" does not support sequences");
        }
        if (database instanceof HsqlDatabase) {
            if (defaultValue instanceof SequenceNextValueFunction) {
                validationErrors.addError("Database " + database.getShortName() + " does not support adding sequence-based default values");
            } else if ((defaultValue instanceof DatabaseFunction) && !HsqlDatabase.supportsDefaultValueComputed
                (addDefaultValueStatement.getColumnDataType(), defaultValue.toString())) {
                validationErrors.addError("Database " + database.getShortName() + " does not support adding function-based default values");
            }
        }

        String columnDataType = addDefaultValueStatement.getColumnDataType();
        if (columnDataType != null) {
            LiquibaseDataType dataType = DataTypeFactory.getInstance().fromDescription(columnDataType, database);
            boolean typeMismatch = false;
            if (dataType instanceof BooleanType) {
                if (!(defaultValue instanceof Boolean) && !(defaultValue instanceof DatabaseFunction)) {
                    typeMismatch = true;
                }
            } else if (dataType instanceof CharType) {
                if (!(defaultValue instanceof String) && !(defaultValue instanceof DatabaseFunction)) {
                    typeMismatch = true;
                }
            }

            if (typeMismatch) {
                validationErrors.addError("Default value of "+defaultValue+" does not match defined type of "+columnDataType);
            }
        }

        return validationErrors;
    }

    @Override
    public Warnings warn(AddDefaultValueStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        Warnings warnings = super.warn(statement, database, sqlGeneratorChain);

        if (database instanceof MySQLDatabase) {
            ((MySQLDatabase) database).warnAboutAlterColumn("addDefaultValue", warnings);
        }

        return warnings;
    }

    @Override
    public Sql[] generateSql(AddDefaultValueStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        Object defaultValue = statement.getDefaultValue();
        return new Sql[] {
                new UnparsedSql("ALTER TABLE "
                        + database.escapeTableName(
                                statement.getCatalogName(),
                                statement.getSchemaName(),
                                statement.getTableName() )
                        + " ALTER COLUMN  "
                        + database.escapeColumnName(
                                statement.getCatalogName(),
                                statement.getSchemaName(),
                                statement.getTableName(),
                                statement.getColumnName()
                        ) + " SET DEFAULT "
                        + DataTypeFactory.getInstance()
                            .fromObject(defaultValue, database)
                            .objectToSql(defaultValue, database),
                    getAffectedColumn(statement) )
        };
    }

    protected Column getAffectedColumn(AddDefaultValueStatement statement) {
        return new Column()
                .setRelation(new Table().setName(statement.getTableName()).setSchema(new Schema(statement.getCatalogName(), statement.getSchemaName())))
                .setName(statement.getColumnName());
    }
}
