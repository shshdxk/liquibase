package io.github.shshdxk.liquibase.database;

import io.github.shshdxk.liquibase.resource.ResourceAccessor;

public interface LiquibaseExtDriver {
    void setResourceAccessor(ResourceAccessor accessor);
}
