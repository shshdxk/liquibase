package io.github.shshdxk.liquibase.sql;

import io.github.shshdxk.liquibase.structure.DatabaseObject;

import java.util.Collection;

public interface Sql {
    String toSql();

    String getEndDelimiter();

    Collection<? extends DatabaseObject> getAffectedDatabaseObjects();

}
