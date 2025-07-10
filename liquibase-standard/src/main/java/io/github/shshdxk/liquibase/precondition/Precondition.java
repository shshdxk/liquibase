package io.github.shshdxk.liquibase.precondition;

import io.github.shshdxk.liquibase.changelog.ChangeSet;
import io.github.shshdxk.liquibase.changelog.DatabaseChangeLog;
import io.github.shshdxk.liquibase.changelog.visitor.ChangeExecListener;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.exception.PreconditionErrorException;
import io.github.shshdxk.liquibase.exception.PreconditionFailedException;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.exception.Warnings;
import io.github.shshdxk.liquibase.parser.core.ParsedNode;
import io.github.shshdxk.liquibase.parser.core.ParsedNodeException;
import io.github.shshdxk.liquibase.resource.ResourceAccessor;
import io.github.shshdxk.liquibase.serializer.LiquibaseSerializable;

/**
 * Marker interface for preconditions.  May become an annotation in the future.
 */
public interface Precondition extends LiquibaseSerializable {
    String getName();

    Warnings warn(Database database);

    ValidationErrors validate(Database database);

    void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet, ChangeExecListener changeExecListener)
            throws PreconditionFailedException, PreconditionErrorException;

    void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException;
}
