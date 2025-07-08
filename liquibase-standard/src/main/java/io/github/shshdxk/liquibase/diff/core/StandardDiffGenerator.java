package io.github.shshdxk.liquibase.diff.core;

import io.github.shshdxk.liquibase.Scope;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.diff.DiffGenerator;
import io.github.shshdxk.liquibase.diff.DiffResult;
import io.github.shshdxk.liquibase.diff.ObjectDifferences;
import io.github.shshdxk.liquibase.diff.StringDiff;
import io.github.shshdxk.liquibase.diff.compare.CompareControl;
import io.github.shshdxk.liquibase.diff.compare.DatabaseObjectComparatorFactory;
import io.github.shshdxk.liquibase.exception.DatabaseException;
import io.github.shshdxk.liquibase.exception.UnexpectedLiquibaseException;
import io.github.shshdxk.liquibase.logging.mdc.MdcKey;
import io.github.shshdxk.liquibase.logging.mdc.customobjects.DiffResultsSummary;
import io.github.shshdxk.liquibase.snapshot.DatabaseSnapshot;
import io.github.shshdxk.liquibase.snapshot.EmptyDatabaseSnapshot;
import io.github.shshdxk.liquibase.snapshot.InvalidExampleException;
import io.github.shshdxk.liquibase.structure.DatabaseObject;
import io.github.shshdxk.liquibase.structure.core.Schema;
import io.github.shshdxk.liquibase.util.StringUtil;

import java.util.Set;

public class StandardDiffGenerator implements DiffGenerator {

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public boolean supports(Database referenceDatabase, Database comparisonDatabase) {
        return true;
    }

    @Override
    public DiffResult compare(DatabaseSnapshot referenceSnapshot, DatabaseSnapshot comparisonSnapshot, CompareControl compareControl) throws DatabaseException {

        if (comparisonSnapshot == null) {
            try {
                comparisonSnapshot = new EmptyDatabaseSnapshot(referenceSnapshot.getDatabase()); //, compareControl.toSnapshotControl(CompareControl.DatabaseRole.REFERENCE));
            } catch (InvalidExampleException e) {
                throw new UnexpectedLiquibaseException(e);
            }
        }

        DiffResult diffResult = new DiffResult(referenceSnapshot, comparisonSnapshot, compareControl);
        checkVersionInfo(referenceSnapshot, comparisonSnapshot, diffResult);

        Set<Class<? extends DatabaseObject>> typesToCompare = compareControl.getComparedTypes();
        typesToCompare.retainAll(referenceSnapshot.getSnapshotControl().getTypesToInclude());
        typesToCompare.retainAll(comparisonSnapshot.getSnapshotControl().getTypesToInclude());

        for (Class<? extends DatabaseObject> typeToCompare : typesToCompare) {
            compareObjectType(typeToCompare, referenceSnapshot, comparisonSnapshot, diffResult);
        }

        Scope.getCurrentScope().addMdcValue(MdcKey.DIFF_RESULTS_SUMMARY, new DiffResultsSummary(diffResult));

        return diffResult;
    }

    protected void checkVersionInfo(DatabaseSnapshot referenceSnapshot, DatabaseSnapshot comparisonSnapshot, DiffResult diffResult) throws DatabaseException {

        if ((comparisonSnapshot != null) && (comparisonSnapshot.getDatabase() != null)) {
            diffResult.setProductNameDiff(new StringDiff(referenceSnapshot.getDatabase().getDatabaseProductName(), comparisonSnapshot.getDatabase().getDatabaseProductName()));
            diffResult.setProductVersionDiff(new StringDiff(referenceSnapshot.getDatabase().getDatabaseProductVersion(), comparisonSnapshot.getDatabase().getDatabaseProductVersion()));
        }

    }

    protected <T extends DatabaseObject> void compareObjectType(Class<T> type, DatabaseSnapshot referenceSnapshot, DatabaseSnapshot comparisonSnapshot, DiffResult diffResult) {

        Database comparisonDatabase = comparisonSnapshot.getDatabase();
        Database referenceDatabase = referenceSnapshot.getDatabase();

        CompareControl.SchemaComparison[] schemaComparisons = diffResult.getCompareControl().getSchemaComparisons();
        if (schemaComparisons != null) {
            for (CompareControl.SchemaComparison schemaComparison : schemaComparisons) {
                for (T referenceObject : referenceSnapshot.get(type)) {
                    Schema referenceObjectSchema = referenceObject.getSchema();
                    if ((referenceObjectSchema != null) && (referenceObjectSchema.getName() != null)) { //don't filter out null-named schemas. May actually be catalog-level objects that should be included
                        if (!StringUtil.trimToEmpty(
                            referenceObjectSchema.toCatalogAndSchema().standardize(referenceDatabase).getSchemaName())
                            .equalsIgnoreCase(
                                StringUtil.trimToEmpty(schemaComparison.getReferenceSchema()
                                .standardize(referenceDatabase).getSchemaName()))) {
                            continue;
                        }
                    }
                    T comparisonObject = comparisonSnapshot.get(referenceObject);
                    if (comparisonObject == null) {
                        diffResult.addMissingObject(referenceObject);
                    } else {
                        ObjectDifferences differences = DatabaseObjectComparatorFactory.getInstance().findDifferences(referenceObject, comparisonObject, comparisonDatabase, diffResult.getCompareControl());
                        if (differences.hasDifferences()) {
                            diffResult.addChangedObject(referenceObject, differences);
                        }
                    }
                }
                //
                for (T comparisonObject : comparisonSnapshot.get(type)) {
                    Schema comparisonObjectSchema = comparisonObject.getSchema();
                    if (comparisonObjectSchema != null) {
                        String comparisonObjectSchemaName = StringUtil.trimToEmpty(comparisonObjectSchema.toCatalogAndSchema().standardize(comparisonDatabase).getSchemaName());
                        String schemaComparisonName1 = StringUtil.trimToEmpty(schemaComparison.getComparisonSchema().standardize(comparisonDatabase).getSchemaName());
                        String schemaComparisonName2 = StringUtil.trimToEmpty(schemaComparison.getReferenceSchema().standardize(comparisonDatabase).getSchemaName());

                        if ("".equals(comparisonObjectSchemaName) && !"".equals(schemaComparisonName1) && !"".equals
                            (schemaComparisonName2)) {
                            comparisonObjectSchemaName = StringUtil.trimToEmpty(comparisonObjectSchema.getName());
                        }
                        if (!(comparisonObjectSchemaName.equalsIgnoreCase(schemaComparisonName1) || comparisonObjectSchemaName.equals(schemaComparisonName2))) {
                            continue;
                        }
                    }

                    if (referenceSnapshot.get(comparisonObject) == null) {
                        diffResult.addUnexpectedObject(comparisonObject);
                    }
                    //            }
                }
            }

            //todo: add logic for when container is missing or unexpected also
        }

    }
}
