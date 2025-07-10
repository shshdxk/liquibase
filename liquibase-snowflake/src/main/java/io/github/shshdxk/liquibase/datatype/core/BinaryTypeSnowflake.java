package io.github.shshdxk.liquibase.datatype.core;

import io.github.shshdxk.liquibase.change.core.LoadDataChange;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.SnowflakeDatabase;
import io.github.shshdxk.liquibase.datatype.DataTypeInfo;
import io.github.shshdxk.liquibase.datatype.DatabaseDataType;
import io.github.shshdxk.liquibase.datatype.LiquibaseDataType;
import io.github.shshdxk.liquibase.servicelocator.PrioritizedService;
import io.github.shshdxk.liquibase.util.StringUtil;

import java.util.Locale;

@DataTypeInfo(name = "binary", aliases = {"longblob", "longvarbinary", "java.sql.Types.BLOB", "java.sql.Types.LONGBLOB", "java.sql.Types.LONGVARBINARY", "java.sql.Types.VARBINARY", "java.sql.Types.BINARY", "varbinary", "binary", "image", "tinyblob", "mediumblob"}, minParameters = 0, maxParameters = 12, priority = PrioritizedService.PRIORITY_DATABASE)
public class BinaryTypeSnowflake extends LiquibaseDataType {
    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        String originalDefinition = StringUtil.trimToEmpty(getRawDefinition());
        if (database instanceof SnowflakeDatabase) {
            if (originalDefinition.toLowerCase(Locale.US).startsWith("varbinary") || originalDefinition.startsWith("java.sql.Types.VARBINARY")) {
                return new DatabaseDataType("VARBINARY", getParameters());
            } else {
                return new DatabaseDataType("BINARY", getParameters());
            }
        }
        return super.toDatabaseDataType(database);
    }

    @Override
    public LoadDataChange.LOAD_DATA_TYPE getLoadTypeName() {
        return LoadDataChange.LOAD_DATA_TYPE.OTHER;
    }
    @Override
    public boolean supports(Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }
}
