package io.github.shshdxk.liquibase.precondition.core;

import io.github.shshdxk.liquibase.changelog.ChangeSet;
import io.github.shshdxk.liquibase.changelog.visitor.ChangeExecListener;
import io.github.shshdxk.liquibase.changelog.DatabaseChangeLog;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.exception.*;
import io.github.shshdxk.liquibase.precondition.AbstractPrecondition;
import io.github.shshdxk.liquibase.snapshot.DatabaseSnapshot;
import io.github.shshdxk.liquibase.snapshot.SnapshotGeneratorFactory;
import io.github.shshdxk.liquibase.structure.core.Schema;
import io.github.shshdxk.liquibase.structure.core.Sequence;

public class SequenceExistsPrecondition extends AbstractPrecondition {
    private String catalogName;
    private String schemaName;
    private String sequenceName;

    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getSequenceName() {
        return sequenceName;
    }

    public void setSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
    }

        @Override
        public Warnings warn(Database database) {
        return new Warnings();
    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }

    @Override
    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet, ChangeExecListener changeExecListener)
            throws PreconditionFailedException, PreconditionErrorException {
        DatabaseSnapshot snapshot;
        Schema schema = new Schema(getCatalogName(), getSchemaName());
        try {
            if (!SnapshotGeneratorFactory.getInstance().has(new Sequence().setName(getSequenceName()).setSchema(schema), database)) {
                throw new PreconditionFailedException("Sequence "+database.escapeSequenceName(getCatalogName(), getSchemaName(), getSequenceName())+" does not exist", changeLog, this);
            }
        } catch (LiquibaseException e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public String getName() {
        return "sequenceExists";
    }
}