package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeFactory;
import liquibase.datatype.DatabaseDataType;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.ColumnConstraint;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.NotNullConstraint;
import liquibase.statement.core.ModifyColumnStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import liquibase.util.ObjectUtil;
import liquibase.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class ModifyColumnGenerator extends AbstractSqlGenerator<ModifyColumnStatement> {

    private static final String REFERENCE_REGEX = "([\\w\\._]+)\\(([\\w_]+)\\)";
    public static final Pattern REFERENCE_PATTERN = Pattern.compile(REFERENCE_REGEX);

    @Override
    public ValidationErrors validate(ModifyColumnStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        if (statement.isMultiple()) {
            ValidationErrors validationErrors = new ValidationErrors();
            ModifyColumnStatement firstColumn = statement.getColumns().get(0);

            for (ModifyColumnStatement column : statement.getColumns()) {
                validationErrors.addAll(validateSingleColumn(column, database));
                if ((firstColumn.getTableName() != null) && !firstColumn.getTableName().equals(column.getTableName())) {
                    validationErrors.addError("All columns must be targeted at the same table");
                }
                if (column.isMultiple()) {
                    validationErrors.addError("Nested multiple add column statements are not supported");
                }
            }
            return validationErrors;
        } else {
            return validateSingleColumn(statement, database);
        }
    }

    private ValidationErrors validateSingleColumn(ModifyColumnStatement statement, Database database) {
        ValidationErrors validationErrors = new ValidationErrors();

        validationErrors.checkRequiredField("columnName", statement.getColumnName());
        if (!ObjectUtil.defaultIfNull(statement.getComputed(), false)) {
            validationErrors.checkRequiredField("columnType", statement.getColumnType());
        }
        validationErrors.checkRequiredField("tableName", statement.getTableName());


        if (!(database instanceof MySQLDatabase)) {
            validationErrors.checkDisallowedField("addAfterColumn", statement.getAddAfterColumn(), database, database.getClass());
        }

        //no databases liquibase supports currently supports adding columns at a given position. Firebird only allows position on alters
        validationErrors.checkDisallowedField("addAtPosition", statement.getAddAtPosition(), database, database.getClass());

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(ModifyColumnStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        if (statement.isMultiple()) {
            return generateMultipleColumns(statement.getColumns(), database);
        } else {
            return generateSingleColumn(statement, database);
        }
    }

    private Sql[] generateMultipleColumns(List<ModifyColumnStatement> columns, Database database) {
        List<Sql> result = new ArrayList<>();
        if (database instanceof MySQLDatabase) {
            final StringBuilder alterTable = new StringBuilder(generateSingleColumBaseSQL(columns.get(0), database));
            for (int i = 0; i < columns.size(); i++) {
                alterTable.append(generateSingleColumnSQL(columns.get(i), database));
                if (i < (columns.size() - 1)) {
                    alterTable.append(",");
                }
            }
            result.add(new UnparsedSql(alterTable.toString(), getAffectedColumns(columns)));

        } else {
            for (ModifyColumnStatement column : columns) {
                result.addAll(Arrays.asList(generateSingleColumn(column, database)));
            }
        }
        return result.toArray(EMPTY_SQL);
    }

    protected Sql[] generateSingleColumn(ModifyColumnStatement statement, Database database) {
        String alterTable = generateSingleColumBaseSQL(statement, database);
        alterTable += generateSingleColumnSQL(statement, database);

        List<Sql> returnSql = new ArrayList<>();
        returnSql.add(new UnparsedSql(alterTable, getAffectedColumn(statement)));

        return returnSql.toArray(EMPTY_SQL);
    }

    protected String generateSingleColumBaseSQL(ModifyColumnStatement statement, Database database) {
        return "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName());
    }

    protected String generateSingleColumnSQL(ModifyColumnStatement statement, Database database) {
        DatabaseDataType columnType = null;

        if (statement.getColumnType() != null) {
            columnType = DataTypeFactory.getInstance().fromDescription(statement.getColumnType() + (statement.isAutoIncrement() ? "{autoIncrement:true}" : ""), database).toDatabaseDataType(database);
        }

        String alterTable = " MODIFY " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName());

        if (columnType != null) {
            alterTable += " " + columnType;
        }

        alterTable += getDefaultClause(statement, database);

        if (!statement.isNullable()) {
            for (ColumnConstraint constraint : statement.getConstraints()) {
                if (constraint instanceof NotNullConstraint) {
                    NotNullConstraint notNullConstraint = (NotNullConstraint) constraint;
                    if (StringUtil.isNotEmpty(notNullConstraint.getConstraintName())) {
                        alterTable += " CONSTRAINT " + database.escapeConstraintName(notNullConstraint.getConstraintName());
                        break;
                    }
                }
            }
            alterTable += " NOT NULL";
            if (database instanceof OracleDatabase) {
                alterTable += !statement.shouldValidateNullable() ? " ENABLE NOVALIDATE " : "";
            }
        } else {
            if ((database instanceof SybaseDatabase) || (database instanceof SybaseASADatabase) || (database
                    instanceof MySQLDatabase) || ((database instanceof MSSQLDatabase) && columnType != null && "timestamp".equalsIgnoreCase(columnType.toString()))) {
                alterTable += " NULL";
            }
        }

        if ((database instanceof MySQLDatabase) && (statement.getRemarks() != null)) {
            alterTable += " COMMENT '" + database.escapeStringForDatabase(StringUtil.trimToEmpty(statement.getRemarks())) + "' ";
        }

        if ((statement.getAddBeforeColumn() != null) && !statement.getAddBeforeColumn().isEmpty()) {
            alterTable += " BEFORE " + database.escapeColumnName(statement.getSchemaName(), statement.getSchemaName(), statement.getTableName(), statement.getAddBeforeColumn()) + " ";
        }

        if ((statement.getAddAfterColumn() != null) && !statement.getAddAfterColumn().isEmpty()) {
            alterTable += " AFTER " + database.escapeColumnName(statement.getSchemaName(), statement.getSchemaName(), statement.getTableName(), statement.getAddAfterColumn());
        }

        return alterTable;
    }

    protected Column[] getAffectedColumns(List<ModifyColumnStatement> columns) {
        List<Column> cols = new ArrayList<>();
        for (ModifyColumnStatement c : columns) {
            cols.add(getAffectedColumn(c));
        }
        return cols.toArray(new Column[0]);
    }

    protected Column getAffectedColumn(ModifyColumnStatement statement) {
        return new Column()
                .setRelation(new Table().setName(statement.getTableName()).setSchema(new Schema(statement.getCatalogName(), statement.getSchemaName())))
                .setName(statement.getColumnName());
    }

    private String getDefaultClause(ModifyColumnStatement statement, Database database) {
        String clause = "";
        Object defaultValue = statement.getDefaultValue();
        if (defaultValue != null) {
            if ((database instanceof OracleDatabase) && defaultValue.toString().startsWith("GENERATED ALWAYS ")) {
                clause += " " + DataTypeFactory.getInstance().fromObject(defaultValue, database).objectToSql(defaultValue, database);
            } else {
                if (database instanceof MSSQLDatabase) {
                    String constraintName = statement.getDefaultValueConstraintName();
                    if (constraintName == null) {
                        constraintName = ((MSSQLDatabase) database).generateDefaultConstraintName(statement.getTableName(), statement.getColumnName());
                    }
                    clause += " CONSTRAINT " + constraintName;
                }
                if (defaultValue instanceof DatabaseFunction) {
                    clause += " DEFAULT " + DataTypeFactory.getInstance().fromObject(defaultValue, database).objectToSql(defaultValue, database);
                } else {
                    clause += " DEFAULT " + DataTypeFactory.getInstance().fromDescription(statement.getColumnType(), database).objectToSql(defaultValue, database);
                }
            }
        }
        return clause;
    }
}
