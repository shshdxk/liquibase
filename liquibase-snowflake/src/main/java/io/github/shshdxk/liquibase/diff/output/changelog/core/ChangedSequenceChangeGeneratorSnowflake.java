package io.github.shshdxk.liquibase.diff.output.changelog.core;

import io.github.shshdxk.liquibase.change.Change;
import io.github.shshdxk.liquibase.change.core.AlterSequenceChange;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.SnowflakeDatabase;
import io.github.shshdxk.liquibase.diff.ObjectDifferences;
import io.github.shshdxk.liquibase.diff.output.DiffOutputControl;
import io.github.shshdxk.liquibase.diff.output.changelog.ChangeGeneratorChain;
import io.github.shshdxk.liquibase.structure.DatabaseObject;
import io.github.shshdxk.liquibase.structure.core.Sequence;

import java.util.ArrayList;
import java.util.List;

public class ChangedSequenceChangeGeneratorSnowflake extends ChangedSequenceChangeGenerator{

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        int priority = super.getPriority(objectType, database);
        if ((Sequence.class.isAssignableFrom(objectType)) && (database instanceof SnowflakeDatabase)) {
            priority += PRIORITY_DATABASE;
        }
        return priority;
    }

    @Override
    public Change[] fixChanged(DatabaseObject changedObject, ObjectDifferences differences, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        Sequence sequence = (Sequence) changedObject;

        List<Change> changes = new ArrayList<>();
        AlterSequenceChange accumulatedChange = createAlterSequenceChange(sequence, control);

        if (differences.isDifferent("incrementBy")) {
            AlterSequenceChange change = createAlterSequenceChange(sequence, control);
            change.setIncrementBy(sequence.getIncrementBy());
            accumulatedChange.setIncrementBy(sequence.getIncrementBy());
            changes.add(change);
        }

        if (changes.isEmpty()) {
            return null;
        } else {
            return changes.toArray(EMPTY_CHANGE);
        }
    }
}
