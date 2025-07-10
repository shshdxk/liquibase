package io.github.shshdxk.liquibase.snapshot.jvm;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.SnowflakeDatabase;
import io.github.shshdxk.liquibase.database.jvm.JdbcConnection;
import io.github.shshdxk.liquibase.exception.DatabaseException;
import io.github.shshdxk.liquibase.snapshot.SnapshotGenerator;
import io.github.shshdxk.liquibase.structure.DatabaseObject;
import io.github.shshdxk.liquibase.util.JdbcUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SchemaSnapshotGeneratorSnowflake extends SchemaSnapshotGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (database instanceof SnowflakeDatabase) {
            return super.getPriority(objectType, database) + PRIORITY_DATABASE;
        } else {
            return PRIORITY_NONE;
        }
    }

    @Override
    protected String[] getDatabaseSchemaNames(Database database) throws SQLException, DatabaseException {
        List<String> returnList = new ArrayList<>();

        try (ResultSet schemas = ((JdbcConnection) database.getConnection()).getMetaData().getSchemas(database
                .getDefaultCatalogName(), null)) {
            while (schemas.next()) {
                returnList.add(JdbcUtil.getValueForColumn(schemas, "TABLE_SCHEM", database));
            }
        }

        return returnList.toArray(new String[0]);
    }

    @Override
    public Class<? extends SnapshotGenerator>[] replaces() {
        return new Class[]{ SchemaSnapshotGenerator.class };
    }
}
