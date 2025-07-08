package io.github.shshdxk.liquibase.diff.output.changelog;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.structure.DatabaseObject;

import java.util.Comparator;

public class ChangeGeneratorComparator implements Comparator<ChangeGenerator> {

    private Class<? extends DatabaseObject> objectType;
    private Database database;

    public ChangeGeneratorComparator(Class<? extends DatabaseObject> objectType, Database database) {
        this.objectType = objectType;
        this.database = database;
    }

    @Override
    public int compare(ChangeGenerator o1, ChangeGenerator o2) {
        int result = -1 * Integer.compare(o1.getPriority(objectType, database), o2.getPriority(objectType, database));
        if (result == 0) {
            return o1.getClass().getName().compareTo(o2.getClass().getName());
        }
        return result;
    }
}
