package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.*;
import io.github.shshdxk.liquibase.datatype.DataTypeFactory;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sql.UnparsedSql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.statement.core.AddAutoIncrementStatement;
import io.github.shshdxk.liquibase.structure.core.Column;
import io.github.shshdxk.liquibase.structure.core.Schema;
import io.github.shshdxk.liquibase.structure.core.Table;

public class AddAutoIncrementGenerator extends AbstractSqlGenerator<AddAutoIncrementStatement> {

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public boolean supports(AddAutoIncrementStatement statement, Database database) {
        return (database.supportsAutoIncrement()
                && !(database instanceof Db2zDatabase)
                && !(database instanceof DerbyDatabase)
                && !(database instanceof MSSQLDatabase)
                && !(database instanceof HsqlDatabase)
                && !(database instanceof H2Database)
                && !(database instanceof OracleDatabase));
    }

    @Override
    public ValidationErrors validate(
            AddAutoIncrementStatement statement,
            Database database,
            SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();

        validationErrors.checkRequiredField("columnName", statement.getColumnName());
        validationErrors.checkRequiredField("tableName", statement.getTableName());
        validationErrors.checkRequiredField("columnDataType", statement.getColumnDataType());

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(
            AddAutoIncrementStatement statement,
            Database database,
            SqlGeneratorChain sqlGeneratorChain) {
    	String sql;
    	if (database instanceof SybaseASADatabase) {
            sql = "ALTER TABLE " +
                database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(),
                    statement.getTableName()) +
                " ALTER " +
                database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement
                    .getTableName(), statement.getColumnName()) +
                " SET " +
                database.getAutoIncrementClause(statement.getStartWith(), statement.getIncrementBy(), null, null);
    	} else {
            sql = "ALTER TABLE " +
                database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement
                    .getTableName()) +
                " MODIFY " +
                database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement
                    .getTableName(), statement.getColumnName()) +
                " " +
                DataTypeFactory.getInstance().fromDescription(statement.getColumnDataType() +
                    "{autoIncrement:true}", database).toDatabaseDataType(database) +
                " " +
                database.getAutoIncrementClause(statement.getStartWith(), statement.getIncrementBy(), statement.getGenerationType(), statement.getDefaultOnNull());
    	}
        return new Sql[]{
            new UnparsedSql(sql, getAffectedColumn(statement))
        };
    }

    protected Column getAffectedColumn(AddAutoIncrementStatement statement) {
        return new Column()
            .setRelation(new Table().setName(statement.getTableName()).setSchema(
                new Schema(statement.getCatalogName(), statement.getSchemaName())))
            .setName(statement.getColumnName());
    }
}
