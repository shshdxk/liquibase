package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.AbstractDb2Database;
import io.github.shshdxk.liquibase.database.core.DerbyDatabase;
import io.github.shshdxk.liquibase.database.core.FirebirdDatabase;
import io.github.shshdxk.liquibase.database.core.H2Database;
import io.github.shshdxk.liquibase.database.core.HsqlDatabase;
import io.github.shshdxk.liquibase.database.core.InformixDatabase;
import io.github.shshdxk.liquibase.database.core.OracleDatabase;
import io.github.shshdxk.liquibase.database.core.SybaseASADatabase;
import io.github.shshdxk.liquibase.database.core.SybaseDatabase;
import io.github.shshdxk.liquibase.datatype.DataTypeFactory;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.statement.AutoIncrementConstraint;
import io.github.shshdxk.liquibase.statement.core.AddColumnStatement;
import org.apache.commons.lang3.StringUtils;

public class AddColumnGeneratorDefaultClauseBeforeNotNull extends AddColumnGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddColumnStatement statement, Database database) {
        return (database instanceof OracleDatabase) || (database instanceof HsqlDatabase) || (database instanceof
            H2Database) || (database instanceof DerbyDatabase) || (database instanceof AbstractDb2Database) || (database
            instanceof FirebirdDatabase) || (database instanceof SybaseDatabase) || (database instanceof
            SybaseASADatabase) || (database instanceof InformixDatabase);
    }

    @Override
    public ValidationErrors validate(AddColumnStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = super.validate(statement, database, sqlGeneratorChain);
        if (statement.isMultiple()) {
            for (AddColumnStatement column : statement.getColumns()) {
                validateSingleColumn(column, database, validationErrors);
            }
        } else {
            validateSingleColumn(statement, database, validationErrors);
        }
        return validationErrors;
    }

    private void validateSingleColumn(AddColumnStatement statement,
            Database database, ValidationErrors validationErrors) {
        if ((database instanceof DerbyDatabase) && statement.isAutoIncrement()) {
            validationErrors.addError("Cannot add an identity column to derby");
        }
    }

    @Override
    protected String generateSingleColumnSQL(AddColumnStatement statement,
            Database database) {
        String alterTable = " ADD " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " " + DataTypeFactory.getInstance().fromDescription(statement.getColumnType() + (statement.isAutoIncrement() ? "{autoIncrement:true}" : ""), database).toDatabaseDataType(database);

        alterTable += getDefaultClauseBeforeNotNull(statement, database);

        if (primaryKeyBeforeNotNull(database)) {
            if (statement.isPrimaryKey()) {
                alterTable += " PRIMARY KEY";
            }
        }

        if (statement.isAutoIncrement()) {
            AutoIncrementConstraint autoIncrementConstraint = statement.getAutoIncrementConstraint();
            alterTable += " " + database.getAutoIncrementClause(autoIncrementConstraint.getStartWith(), autoIncrementConstraint.getIncrementBy(), autoIncrementConstraint.getGenerationType(), autoIncrementConstraint.getDefaultOnNull());
        }

        if (!statement.isNullable()) {
            alterTable += " NOT NULL";
        } else if ((database instanceof SybaseDatabase) || (database instanceof SybaseASADatabase)) {
            alterTable += " NULL";
        }

        if (!primaryKeyBeforeNotNull(database)) {
            if (statement.isPrimaryKey()) {
                alterTable += " PRIMARY KEY";
            }
        }

        if (!StringUtils.isEmpty(statement.getAddBeforeColumn())) {
            alterTable += " BEFORE " + database.escapeColumnName(statement.getSchemaName(), statement.getSchemaName(), statement.getTableName(), statement.getAddBeforeColumn()) + " ";
        }

        if (!StringUtils.isEmpty(statement.getAddAfterColumn())) {
            alterTable += " AFTER " + database.escapeColumnName(statement.getSchemaName(), statement.getSchemaName(), statement.getTableName(), statement.getAddAfterColumn());
        }

        return alterTable;
    }

    private String getDefaultClauseBeforeNotNull(AddColumnStatement statement, Database database) {
        String clause = "";
        Object defaultValue = statement.getDefaultValue();
        if (defaultValue != null) {
            clause += " DEFAULT " + DataTypeFactory.getInstance().fromObject(defaultValue, database).objectToSql(defaultValue, database);
        }
        return clause;
    }

    private boolean primaryKeyBeforeNotNull(Database database) {
        return !((database instanceof HsqlDatabase) || (database instanceof H2Database));
    }


}
