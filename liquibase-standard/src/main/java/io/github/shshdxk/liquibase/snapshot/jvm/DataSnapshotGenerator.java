package io.github.shshdxk.liquibase.snapshot.jvm;

import io.github.shshdxk.liquibase.exception.DatabaseException;
import io.github.shshdxk.liquibase.snapshot.DatabaseSnapshot;
import io.github.shshdxk.liquibase.snapshot.InvalidExampleException;
import io.github.shshdxk.liquibase.structure.DatabaseObject;
import io.github.shshdxk.liquibase.structure.core.Data;
import io.github.shshdxk.liquibase.structure.core.Table;

public class DataSnapshotGenerator extends JdbcSnapshotGenerator {

    public DataSnapshotGenerator() {
        super(Data.class, new Class[]{Table.class});
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        return example;
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        if (!snapshot.getSnapshotControl().shouldInclude(Data.class)) {
            return;
        }
        if (foundObject instanceof Table) {
            Table table = (Table) foundObject;
            try {

                Data exampleData = new Data().setTable(table);
                table.setAttribute("data", exampleData);
            } catch (Exception e) {
                throw new DatabaseException(e);
            }
        }
    }
}
