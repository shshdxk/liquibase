package io.github.shshdxk.liquibase.parser;

import io.github.shshdxk.liquibase.exception.LiquibaseParseException;
import io.github.shshdxk.liquibase.resource.ResourceAccessor;
import io.github.shshdxk.liquibase.snapshot.DatabaseSnapshot;

public interface SnapshotParser extends LiquibaseParser {

    DatabaseSnapshot parse(String path, ResourceAccessor resourceAccessor) throws LiquibaseParseException;

    boolean supports(String path, ResourceAccessor resourceAccessor);

}
