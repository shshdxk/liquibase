package io.github.shshdxk.liquibase.datatype.core;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.SnowflakeDatabase;
import io.github.shshdxk.liquibase.datatype.DataTypeInfo;
import io.github.shshdxk.liquibase.datatype.DatabaseDataType;
import io.github.shshdxk.liquibase.datatype.LiquibaseDataType;

@DataTypeInfo(
    name = "double",
    aliases = {"java.sql.Types.DOUBLE", "java.lang.Double"},
    minParameters = 0,
    maxParameters = 2,
    priority = LiquibaseDataType.PRIORITY_DATABASE
)
public class DoubleDataTypeSnowflake extends DoubleType {

    public DoubleDataTypeSnowflake() {

    }

    public int getPriority() {
        return LiquibaseDataType.PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        // Double is an alias for the FLOAT data type in Snowflake.
        return new DatabaseDataType("FLOAT");
    }
}
