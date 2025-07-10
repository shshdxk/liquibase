package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.ObjectQuotingStrategy;
import io.github.shshdxk.liquibase.database.core.MSSQLDatabase;
import io.github.shshdxk.liquibase.datatype.DataTypeFactory;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorFactory;
import io.github.shshdxk.liquibase.statement.NotNullConstraint;
import io.github.shshdxk.liquibase.statement.core.CreateDatabaseChangeLogLockTableStatement;
import io.github.shshdxk.liquibase.statement.core.CreateTableStatement;

public class CreateDatabaseChangeLogLockTableGenerator extends AbstractSqlGenerator<CreateDatabaseChangeLogLockTableStatement> {

    @Override
    public ValidationErrors validate(CreateDatabaseChangeLogLockTableStatement createDatabaseChangeLogLockTableStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    @Override
    public Sql[] generateSql(CreateDatabaseChangeLogLockTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String charTypeName = getCharTypeName(database);
        String dateTimeTypeString = getDateTimeTypeString(database);
        CreateTableStatement createTableStatement = new CreateTableStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName())
                .setTablespace(database.getLiquibaseTablespaceName())
                .addPrimaryKeyColumn("ID", DataTypeFactory.getInstance().fromDescription("int", database), null, null, null, new NotNullConstraint())
                .addColumn("LOCKED", DataTypeFactory.getInstance().fromDescription("boolean", database), null, null, new NotNullConstraint())
                .addColumn("LOCKGRANTED", DataTypeFactory.getInstance().fromDescription(dateTimeTypeString, database))
                .addColumn("LOCKEDBY", DataTypeFactory.getInstance().fromDescription(charTypeName + "(255)", database));

        // use LEGACY quoting since we're dealing with system objects
        ObjectQuotingStrategy currentStrategy = database.getObjectQuotingStrategy();
        database.setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY);
        try {
            return SqlGeneratorFactory.getInstance().generateSql(createTableStatement, database);
        } finally {
            database.setObjectQuotingStrategy(currentStrategy);
        }
    }

    protected String getCharTypeName(Database database) {
        if ((database instanceof MSSQLDatabase) && ((MSSQLDatabase) database).sendsStringParametersAsUnicode()) {
            return "nvarchar";
        }
        return "varchar";
    }

    protected String getDateTimeTypeString(Database database) {
        if (database instanceof MSSQLDatabase) {
            return "datetime2(3)";
        }
        return "datetime";
    }
}
