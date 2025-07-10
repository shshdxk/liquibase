package io.github.shshdxk.liquibase.exception;

import io.github.shshdxk.liquibase.Scope;
import io.github.shshdxk.liquibase.change.Change;
import io.github.shshdxk.liquibase.change.ChangeFactory;

public class InvalidChangeDefinitionException extends LiquibaseException {
    
    private static final long serialVersionUID = -429400803681987065L;
    
    public InvalidChangeDefinitionException(String message, Change change) {
        super(Scope.getCurrentScope().getSingleton(ChangeFactory.class).getChangeMetaData(change).getName()+" in '"+change.getChangeSet().toString(false)+"' is invalid: "+message);
    }
}
