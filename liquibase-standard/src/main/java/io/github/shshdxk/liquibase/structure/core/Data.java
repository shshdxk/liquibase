package io.github.shshdxk.liquibase.structure.core;

import io.github.shshdxk.liquibase.GlobalConfiguration;
import io.github.shshdxk.liquibase.structure.AbstractDatabaseObject;
import io.github.shshdxk.liquibase.structure.DatabaseObject;

public class Data extends AbstractDatabaseObject {

    @Override
    public boolean snapshotByDefault() {
        return GlobalConfiguration.SHOULD_SNAPSHOT_DATA.getCurrentValue();
    }

    public Table getTable() {
        return getAttribute("table", Table.class);
    }

    public Data setTable(Table table) {
        setAttribute("table", table);

        return this;
    }


    @Override
    public DatabaseObject[] getContainingObjects() {
        return new DatabaseObject[] {
                getTable()
        };
    }

    @Override
    public String getName() {
        Table table = getTable();
        if (table == null) {
            return null;
        }
        return table.getName();
    }

    @Override
    public Data setName(String name) {
        Table table = getTable();
        if (table == null) {
            setTable(new Table().setName(name));
        } else {
            table.setName(name);
        }

        return this;
    }

    @Override
    public Schema getSchema() {
        Table table = getTable();
        if (table == null) {
            return null;
        }
        return table.getSchema();
    }
}
