package io.github.shshdxk.liquibase.diff;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.diff.compare.CompareControl;
import io.github.shshdxk.liquibase.exception.DatabaseException;
import io.github.shshdxk.liquibase.servicelocator.PrioritizedService;
import io.github.shshdxk.liquibase.snapshot.DatabaseSnapshot;

public interface DiffGenerator extends PrioritizedService {
    DiffResult compare(DatabaseSnapshot referenceSnapshot, DatabaseSnapshot comparisonSnapshot, CompareControl compareControl) throws DatabaseException;

    boolean supports(Database referenceDatabase, Database comparisonDatabase);

}
