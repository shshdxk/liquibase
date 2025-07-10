package io.github.shshdxk.liquibase.snapshot.jvm;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.H2Database;
import io.github.shshdxk.liquibase.snapshot.CachedRow;
import io.github.shshdxk.liquibase.snapshot.SnapshotGenerator;
import io.github.shshdxk.liquibase.statement.DatabaseFunction;
import io.github.shshdxk.liquibase.structure.DatabaseObject;
import io.github.shshdxk.liquibase.structure.core.Column;

public class ColumnSnapshotGeneratorH2 extends ColumnSnapshotGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (!(database instanceof H2Database)) {
            return PRIORITY_NONE;
        }

        int priority = super.getPriority(objectType, database);
        if (priority == 0) {
            return priority;
        }
        return priority + 5;
    }

    @Override
    public Class<? extends SnapshotGenerator>[] replaces() {
        return new Class[]{ColumnSnapshotGenerator.class};
    }


    @Override
    protected Object readDefaultValue(CachedRow columnMetadataResultSet, Column columnInfo, Database database) {
        Object defaultValue = super.readDefaultValue(columnMetadataResultSet, columnInfo, database);
        if ((defaultValue instanceof DatabaseFunction) && ((DatabaseFunction) defaultValue)
                .getValue().startsWith("NEXT VALUE FOR ")) {
            columnInfo.setAutoIncrementInformation(new Column.AutoIncrementInformation());
            return null;
        }
        return defaultValue;
    }

}
