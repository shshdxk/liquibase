package io.github.shshdxk.liquibase.diff.output;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.diff.ObjectDifferences;
import io.github.shshdxk.liquibase.structure.DatabaseObject;

public interface ObjectChangeFilter {

    boolean includeMissing(DatabaseObject object, Database referenceDatabase, Database comparisionDatabase);

    boolean includeUnexpected(DatabaseObject object, Database referenceDatabase, Database comparisionDatabase);

    boolean includeChanged(DatabaseObject object, ObjectDifferences differences, Database referenceDatabase, Database
            comparisionDatabase);

    boolean include(DatabaseObject object);
}
