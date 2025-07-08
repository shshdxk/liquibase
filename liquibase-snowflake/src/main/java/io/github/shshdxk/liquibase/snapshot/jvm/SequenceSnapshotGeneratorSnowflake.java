package io.github.shshdxk.liquibase.snapshot.jvm;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.SnowflakeDatabase;
import io.github.shshdxk.liquibase.snapshot.SnapshotGenerator;
import io.github.shshdxk.liquibase.structure.DatabaseObject;
import io.github.shshdxk.liquibase.structure.core.Column;
import io.github.shshdxk.liquibase.structure.core.Schema;

public class SequenceSnapshotGeneratorSnowflake extends SequenceSnapshotGenerator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (database instanceof SnowflakeDatabase) {
            return super.getPriority(objectType, database) + PRIORITY_DATABASE;
        } else {
            return PRIORITY_NONE;
        }
    }

    @Override
    public Class<? extends SnapshotGenerator>[] replaces() {
        return new Class[]{SequenceSnapshotGenerator.class};
    }

    @Override
    protected String getSelectSequenceSql(Schema schema, Database database) {
        if (database instanceof SnowflakeDatabase) {
            return "SELECT SEQUENCE_NAME, START_VALUE, MINIMUM_VALUE AS MIN_VALUE, MAXIMUM_VALUE AS MAX_VALUE, " +
                    database.escapeObjectName("INCREMENT", Column.class) + " AS INCREMENT_BY, " +
                    "CYCLE_OPTION AS WILL_CYCLE FROM information_schema.sequences " +
                    "WHERE SEQUENCE_CATALOG='" + database.getDefaultCatalogName() + "' AND " +
                    "SEQUENCE_SCHEMA='" + database.getDefaultSchemaName() + "'";
        }
        return super.getSelectSequenceSql(schema, database);
    }
}