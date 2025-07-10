package io.github.shshdxk.liquibase.diff.output.changelog.core;

import io.github.shshdxk.liquibase.change.Change;
import io.github.shshdxk.liquibase.change.core.CreateSequenceChange;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.SnowflakeDatabase;
import io.github.shshdxk.liquibase.diff.output.DiffOutputControl;
import io.github.shshdxk.liquibase.diff.output.changelog.ChangeGeneratorChain;
import io.github.shshdxk.liquibase.structure.DatabaseObject;
import io.github.shshdxk.liquibase.structure.core.Sequence;

public class MissingSequenceChangeGeneratorSnowflake extends MissingSequenceChangeGenerator{

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        int priority = super.getPriority(objectType, database);
        if ((Sequence.class.isAssignableFrom(objectType)) && (database instanceof SnowflakeDatabase)) {
            priority += PRIORITY_DATABASE;
        }
        return priority;
    }

    @Override
    public Change[] fixMissing(DatabaseObject missingObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        Sequence sequence = (Sequence) missingObject;

        CreateSequenceChange change = new CreateSequenceChange();
        change.setSequenceName(sequence.getName());
        if (control.getIncludeCatalog()) {
            change.setCatalogName(sequence.getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            change.setSchemaName(sequence.getSchema().getName());
        }
        change.setStartValue(sequence.getStartValue());
        change.setIncrementBy(sequence.getIncrementBy());

        return new Change[] { change };

    }
}
