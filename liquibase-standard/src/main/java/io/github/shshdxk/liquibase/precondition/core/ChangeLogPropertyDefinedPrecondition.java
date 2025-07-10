package io.github.shshdxk.liquibase.precondition.core;

import io.github.shshdxk.liquibase.changelog.visitor.ChangeExecListener;
import io.github.shshdxk.liquibase.changelog.ChangeLogParameters;
import io.github.shshdxk.liquibase.changelog.ChangeSet;
import io.github.shshdxk.liquibase.changelog.DatabaseChangeLog;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.exception.PreconditionErrorException;
import io.github.shshdxk.liquibase.exception.PreconditionFailedException;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.exception.Warnings;
import io.github.shshdxk.liquibase.precondition.AbstractPrecondition;
import lombok.Getter;

@Getter
public class ChangeLogPropertyDefinedPrecondition extends AbstractPrecondition {

    private String property;
    private String value;

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public String getName() {
        return "changeLogPropertyDefined";
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public void setValue(String value) {
        this.value = value;
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
        ChangeLogParameters changeLogParameters = changeLog.getChangeLogParameters();
        if (changeLogParameters == null) {
            throw new PreconditionFailedException("No Changelog properties were set", changeLog, this);
        }
        Object propertyValue = changeLogParameters.getValue(property, changeLog);
        if (propertyValue == null) {
            propertyValue = changeLogParameters.getLocalValue(property, changeSet);
            if (null == propertyValue) {
                throw new PreconditionFailedException("Changelog property '"+ property +"' was not set", changeLog, this);
            }
        }
        if ((value != null) && !propertyValue.toString().equals(value)) {
            throw new PreconditionFailedException("Expected changelog property '"+ property +"' to have a value of '"+value+"'.  Got '"+propertyValue+"'", changeLog, this);
        }
    }
}
