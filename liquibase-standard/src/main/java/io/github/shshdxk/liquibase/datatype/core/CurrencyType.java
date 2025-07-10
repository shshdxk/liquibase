package io.github.shshdxk.liquibase.datatype.core;

import io.github.shshdxk.liquibase.change.core.LoadDataChange;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.*;
import io.github.shshdxk.liquibase.datatype.DataTypeInfo;
import io.github.shshdxk.liquibase.datatype.DatabaseDataType;
import io.github.shshdxk.liquibase.datatype.LiquibaseDataType;
import io.github.shshdxk.liquibase.util.StringUtil;

import java.util.Locale;


@DataTypeInfo(name="currency", aliases = {"money", "smallmoney"}, minParameters = 0, maxParameters = 0, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class CurrencyType  extends LiquibaseDataType {

    @Override
    public LoadDataChange.LOAD_DATA_TYPE getLoadTypeName() {
        return LoadDataChange.LOAD_DATA_TYPE.NUMERIC;
    }

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        String originalDefinition = StringUtil.trimToEmpty(getRawDefinition());
        if (database instanceof MSSQLDatabase) {
            if (originalDefinition.toLowerCase(Locale.US).startsWith("smallmoney")
                    || originalDefinition.toLowerCase(Locale.US).startsWith("[smallmoney]")) {

                return new DatabaseDataType(database.escapeDataTypeName("smallmoney"));
            }
            return new DatabaseDataType(database.escapeDataTypeName("money"));
        }
        if ((database instanceof InformixDatabase) || (database instanceof SybaseASADatabase) || (database instanceof
                SybaseDatabase)) {
            return new DatabaseDataType("MONEY");
        }
        if (database instanceof OracleDatabase) {
            return new DatabaseDataType("NUMBER", 15, 2);
        }

        if (database instanceof AbstractDb2Database) {
            return new DatabaseDataType("DECIMAL", 19,4);
        }
        if (database instanceof FirebirdDatabase || database instanceof H2Database) {
            return new DatabaseDataType("DECIMAL", 18, 4);
        }
        if (database instanceof SQLiteDatabase) {
            return new DatabaseDataType("REAL");
        }
        return new DatabaseDataType("DECIMAL");
    }
}
