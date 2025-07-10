package io.github.shshdxk.liquibase.precondition;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.exception.CustomPreconditionErrorException;
import io.github.shshdxk.liquibase.exception.CustomPreconditionFailedException;

public interface CustomPrecondition {
    void check(Database database) throws CustomPreconditionFailedException, CustomPreconditionErrorException;
}
