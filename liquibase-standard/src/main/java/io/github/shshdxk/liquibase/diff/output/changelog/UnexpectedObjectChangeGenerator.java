package io.github.shshdxk.liquibase.diff.output.changelog;

import io.github.shshdxk.liquibase.change.Change;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.diff.output.DiffOutputControl;
import io.github.shshdxk.liquibase.structure.DatabaseObject;

public interface UnexpectedObjectChangeGenerator extends ChangeGenerator {

    Change[] fixUnexpected(DatabaseObject unexpectedObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain);
}
