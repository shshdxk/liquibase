package io.github.shshdxk.liquibase.precondition;

import io.github.shshdxk.liquibase.precondition.core.PreconditionContainer;

public interface Conditional {
    PreconditionContainer getPreconditions();

    void setPreconditions(PreconditionContainer precond);

}
