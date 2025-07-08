package io.github.shshdxk.liquibase.precondition.core;

import io.github.shshdxk.liquibase.changelog.ChangeSet;
import io.github.shshdxk.liquibase.changelog.DatabaseChangeLog;
import io.github.shshdxk.liquibase.changelog.visitor.ChangeExecListener;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.DatabaseList;
import io.github.shshdxk.liquibase.exception.PreconditionErrorException;
import io.github.shshdxk.liquibase.exception.PreconditionFailedException;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.exception.Warnings;
import io.github.shshdxk.liquibase.precondition.AbstractPrecondition;

/**
 * Precondition for specifying the type of database (oracle, mysql, etc.).
 */

public class DBMSPrecondition extends AbstractPrecondition {
    private String type;


    public DBMSPrecondition() {
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    public String getType() {
        return type;
    }

    public void setType(String atype) {
        if (atype == null) {
            this.type = null;
        } else {
            this.type = atype.toLowerCase();
        }
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
        try {
            String dbType = database.getShortName();
            if (!DatabaseList.definitionMatches(this.type, database, false)) {
                throw new PreconditionFailedException("DBMS Precondition failed: expected "+type+", got "+dbType, changeLog, this);
            }
        } catch (PreconditionFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
    }

    @Override
    public String getName() {
        return "dbms";
    }

}
