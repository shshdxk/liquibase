package io.github.shshdxk.liquibase.datatype.core;

import io.github.shshdxk.liquibase.change.core.LoadDataChange;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.InformixDatabase;
import io.github.shshdxk.liquibase.database.core.MSSQLDatabase;
import io.github.shshdxk.liquibase.datatype.DataTypeInfo;
import io.github.shshdxk.liquibase.datatype.DatabaseDataType;
import io.github.shshdxk.liquibase.datatype.LiquibaseDataType;

import java.util.Arrays;

@DataTypeInfo(name = "decimal", aliases = { "java.sql.Types.DECIMAL", "java.math.BigDecimal" }, minParameters = 0, maxParameters = 2, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class DecimalType  extends LiquibaseDataType {

    private boolean autoIncrement;

    @Override
    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    @Override
    public LoadDataChange.LOAD_DATA_TYPE getLoadTypeName() {
        return LoadDataChange.LOAD_DATA_TYPE.NUMERIC;
    }

    @Override
  public DatabaseDataType toDatabaseDataType(Database database) {
    if (database instanceof MSSQLDatabase) {
      Object[] parameters = getParameters();
      if (parameters.length == 0) {
        parameters = new Object[] { 18, 0 };
      } else if (parameters.length == 1) {
        parameters = new Object[] { parameters[0], 0 };
      } else if (parameters.length > 2) {
        parameters = Arrays.copyOfRange(parameters, 0, 2);
      }
      return new DatabaseDataType(database.escapeDataTypeName("decimal"), parameters);
    }
    if (database instanceof InformixDatabase) {

      if((getParameters() != null) && (getParameters().length == 2)) {

        // Don't use 255 as a scale because it is invalid, 
        // use only 1 argument in this special case
        if("255".equals(String.valueOf(getParameters()[1]))) {
          return new DatabaseDataType(getName(), getParameters()[0]);
        }
      }
    }

    return super.toDatabaseDataType(database);
  }

}
