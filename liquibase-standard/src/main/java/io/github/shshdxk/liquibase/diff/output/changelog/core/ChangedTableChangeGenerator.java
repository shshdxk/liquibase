package io.github.shshdxk.liquibase.diff.output.changelog.core;

import io.github.shshdxk.liquibase.Scope;
import io.github.shshdxk.liquibase.change.Change;
import io.github.shshdxk.liquibase.change.core.SetTableRemarksChange;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.diff.Difference;
import io.github.shshdxk.liquibase.diff.ObjectDifferences;
import io.github.shshdxk.liquibase.diff.output.DiffOutputControl;
import io.github.shshdxk.liquibase.diff.output.changelog.AbstractChangeGenerator;
import io.github.shshdxk.liquibase.diff.output.changelog.ChangeGeneratorChain;
import io.github.shshdxk.liquibase.diff.output.changelog.ChangedObjectChangeGenerator;
import io.github.shshdxk.liquibase.structure.DatabaseObject;
import io.github.shshdxk.liquibase.structure.core.Table;

public class ChangedTableChangeGenerator extends AbstractChangeGenerator implements ChangedObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Table.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return null;
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return null;
    }

    @Override
    public Change[] fixChanged(DatabaseObject changedObject, ObjectDifferences differences, DiffOutputControl control, Database referenceDatabase, final Database comparisonDatabase, ChangeGeneratorChain chain) {
        Table table = (Table) changedObject;

        Difference changedRemarks = differences.getDifference("remarks");
        if (changedRemarks != null) {
            SetTableRemarksChange change = new SetTableRemarksChange();
            if (control.getIncludeCatalog()) {
                change.setCatalogName(table.getSchema().getCatalogName());
            }
            if (control.getIncludeSchema()) {
                change.setSchemaName(table.getSchema().getName());
            }

            change.setTableName(table.getName());
            change.setRemarks(table.getRemarks());
            
            return new Change[] {
                    change
            };
        }

        Difference changedTablespace = differences.getDifference("tablespace");
        
        if (changedTablespace != null) {
            // TODO: Implement moveTableToDifferentTablespace change type!
            Scope.getCurrentScope().getLog(getClass()).warning("A change of the tablespace was detected, however, Liquibase does not currently generate statements to move a table between tablespaces.");
        }

        return null;
    }
}
