package io.github.shshdxk.liquibase.database.core;

import io.github.shshdxk.liquibase.database.DatabaseConnection;
import io.github.shshdxk.liquibase.exception.DatabaseException;
import io.github.shshdxk.liquibase.structure.DatabaseObject;
import io.github.shshdxk.liquibase.structure.core.Column;
import io.github.shshdxk.liquibase.structure.core.Index;
import io.github.shshdxk.liquibase.util.StringUtil;

public class Db2zDatabase extends AbstractDb2Database {

    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return conn.getDatabaseProductName().startsWith("DB2") && StringUtil.startsWith(conn.getDatabaseProductVersion(), "DSN");
    }

    @Override
    public String getShortName() {
        return "db2z";
    }

    @Override
    public String correctObjectName(final String objectName, final Class<? extends DatabaseObject> objectType) {
        return objectName;
    }

    @Override
    public boolean isSystemObject(DatabaseObject example) {
        boolean isSystemIndex = example instanceof Index && example.getName() != null && example.getName().contains("_#_");
        boolean isSystemColumn = example instanceof Column && StringUtil.startsWith(example.getName(), "DB2_GENERATED");
        return isSystemIndex || isSystemColumn || super.isSystemObject(example);
    }

    @Override
    protected String getDefaultDatabaseProductName() {
        return "DB2/z";
    }
}
