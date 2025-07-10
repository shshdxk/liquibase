package io.github.shshdxk.liquibase.sdk.executor;

import io.github.shshdxk.liquibase.executor.LoggingExecutor;
import io.github.shshdxk.liquibase.database.core.MockDatabase;
import io.github.shshdxk.liquibase.servicelocator.LiquibaseService;

import java.io.StringWriter;

@LiquibaseService(skip=true)
public class MockExecutor extends LoggingExecutor {

    public MockExecutor() {
        super(null, new StringWriter(), new MockDatabase());
    }

    public String getRanSql() {
        return getOutput().toString();
    }
}
