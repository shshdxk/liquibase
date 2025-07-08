package io.github.shshdxk.liquibase.snapshot.jvm;

import io.github.shshdxk.liquibase.Scope;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.SnowflakeDatabase;
import io.github.shshdxk.liquibase.exception.DatabaseException;
import io.github.shshdxk.liquibase.executor.ExecutorService;
import io.github.shshdxk.liquibase.snapshot.CachedRow;
import io.github.shshdxk.liquibase.snapshot.DatabaseSnapshot;
import io.github.shshdxk.liquibase.snapshot.SnapshotGenerator;
import io.github.shshdxk.liquibase.statement.core.RawSqlStatement;
import io.github.shshdxk.liquibase.structure.DatabaseObject;
import io.github.shshdxk.liquibase.structure.core.Relation;
import io.github.shshdxk.liquibase.structure.core.Schema;
import io.github.shshdxk.liquibase.structure.core.Table;
import io.github.shshdxk.liquibase.structure.core.UniqueConstraint;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class UniqueConstraintSnapshotGeneratorSnowflake extends UniqueConstraintSnapshotGenerator {


    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (database instanceof SnowflakeDatabase) {
            return PRIORITY_DATABASE;
        } else {
            return PRIORITY_NONE;
        }
    }

    @Override
    public Class<? extends SnapshotGenerator>[] replaces() {
        return new Class[] { UniqueConstraintSnapshotGenerator.class };
    }

    @Override
    protected List<CachedRow> listConstraints(Table table, DatabaseSnapshot snapshot, Schema schema)
            throws DatabaseException, SQLException {
        return new SnowflakeResultSetConstraintsExtractor(snapshot, schema.getCatalogName(), schema.getName(), table.getName())
                .fastFetch();
    }

    @Override
    protected List<Map<String, ?>> listColumns(UniqueConstraint example, Database database, DatabaseSnapshot snapshot)
            throws DatabaseException {
        Relation table = example.getRelation();
        String name = example.getName();
        String tableName = database.correctObjectName(table.getName(), Table.class);
        String constraintName = database.correctObjectName(name, UniqueConstraint.class);

        String showSql = "SHOW UNIQUE KEYS IN " + tableName;
        String sql = "SELECT \"column_name\" AS COLUMN_NAME FROM TABLE(result_scan(last_query_id())) WHERE \"constraint_name\"= '" + constraintName +"'";

        Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database)
                .queryForList(new RawSqlStatement(showSql));

        return Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database)
                .queryForList(new RawSqlStatement(sql));
    }
}