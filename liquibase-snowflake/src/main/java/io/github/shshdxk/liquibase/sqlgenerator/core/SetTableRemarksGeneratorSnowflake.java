package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.SnowflakeDatabase;
import io.github.shshdxk.liquibase.statement.core.SetTableRemarksStatement;

public class SetTableRemarksGeneratorSnowflake extends SetTableRemarksGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(SetTableRemarksStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }
}
