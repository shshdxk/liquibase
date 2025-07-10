package io.github.shshdxk.liquibase.snapshot.jvm;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.SnowflakeDatabase;
import io.github.shshdxk.liquibase.snapshot.SnapshotGenerator;
import io.github.shshdxk.liquibase.statement.SqlStatement;
import io.github.shshdxk.liquibase.statement.core.RawParameterizedSqlStatement;
import io.github.shshdxk.liquibase.structure.DatabaseObject;
import io.github.shshdxk.liquibase.structure.core.Column;
import io.github.shshdxk.liquibase.structure.core.Schema;

import java.util.ArrayList;
import java.util.List;

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
    protected SqlStatement getSelectSequenceStatement(Schema schema, Database database) {
        if (database instanceof SnowflakeDatabase) {
            List<String> parameter = new ArrayList<>(2);
            parameter.add(database.getDefaultCatalogName());
            parameter.add(database.getDefaultSchemaName());

            StringBuilder sql = new StringBuilder(String.format("SELECT SEQUENCE_NAME, START_VALUE, MINIMUM_VALUE AS MIN_VALUE, MAXIMUM_VALUE AS MAX_VALUE, %s AS INCREMENT_BY, ",
                    database.escapeObjectName("INCREMENT", Column.class)))
                    .append("CYCLE_OPTION AS WILL_CYCLE FROM information_schema.sequences ")
                    .append("WHERE SEQUENCE_CATALOG=? AND SEQUENCE_SCHEMA=?");

            return new RawParameterizedSqlStatement(sql.toString(), parameter.toArray());
        }
        return super.getSelectSequenceStatement(schema, database);
    }
}
