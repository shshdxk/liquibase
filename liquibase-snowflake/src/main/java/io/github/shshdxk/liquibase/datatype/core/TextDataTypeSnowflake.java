package io.github.shshdxk.liquibase.datatype.core;

import io.github.shshdxk.liquibase.change.core.LoadDataChange;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.SnowflakeDatabase;
import io.github.shshdxk.liquibase.datatype.DataTypeInfo;
import io.github.shshdxk.liquibase.datatype.DatabaseDataType;
import io.github.shshdxk.liquibase.datatype.LiquibaseDataType;
import io.github.shshdxk.liquibase.servicelocator.PrioritizedService;

@DataTypeInfo(name = "text", minParameters = 0, maxParameters = 0, priority = PrioritizedService.PRIORITY_DATABASE)
public class TextDataTypeSnowflake extends LiquibaseDataType {

    @Override
    public boolean supports(Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        if (database instanceof SnowflakeDatabase) {
            return new DatabaseDataType("TEXT", getParameters());
        }
        return super.toDatabaseDataType(database);
    }

    @Override
    public LoadDataChange.LOAD_DATA_TYPE getLoadTypeName() {
        return LoadDataChange.LOAD_DATA_TYPE.STRING;
    }
}
