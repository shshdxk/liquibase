package io.github.shshdxk.liquibase.change.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.shshdxk.liquibase.CatalogAndSchema;
import io.github.shshdxk.liquibase.change.AbstractChange;
import io.github.shshdxk.liquibase.change.ChangeMetaData;
import io.github.shshdxk.liquibase.change.DatabaseChange;
import io.github.shshdxk.liquibase.change.DatabaseChangeProperty;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.diff.compare.DatabaseObjectComparatorFactory;
import io.github.shshdxk.liquibase.exception.DatabaseException;
import io.github.shshdxk.liquibase.exception.UnexpectedLiquibaseException;
import io.github.shshdxk.liquibase.snapshot.InvalidExampleException;
import io.github.shshdxk.liquibase.snapshot.SnapshotControl;
import io.github.shshdxk.liquibase.snapshot.SnapshotGeneratorFactory;
import io.github.shshdxk.liquibase.statement.SqlStatement;
import io.github.shshdxk.liquibase.structure.core.ForeignKey;
import io.github.shshdxk.liquibase.structure.core.Table;
import lombok.Setter;

@DatabaseChange(name = "dropAllForeignKeyConstraints", description = "Drops all foreign key constraints on a table", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table")
@Setter
public class DropAllForeignKeyConstraintsChange extends AbstractChange {

    private String baseTableCatalogName;
    private String baseTableSchemaName;
    private String baseTableName;

    @DatabaseChangeProperty(mustEqualExisting ="table.catalog", description = "Name of the database catalog of the base table", since = "3.0")
    public String getBaseTableCatalogName() {
        return baseTableCatalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="table.schema", description = "Name of the database schema of the base table")
    public String getBaseTableSchemaName() {
        return baseTableSchemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "table", requiredForDatabase = "all",
        description = "Name of the table containing columns constrained by foreign keys")
    public String getBaseTableName() {
        return baseTableName;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        List<SqlStatement> sqlStatements = new ArrayList<>();

        List<DropForeignKeyConstraintChange> childDropChanges = generateChildren(database);

        for (DropForeignKeyConstraintChange change : childDropChanges) {
            sqlStatements.addAll(Arrays.asList(change.generateStatements(database)));
        }

        return sqlStatements.toArray(SqlStatement.EMPTY_SQL_STATEMENT);
    }

    @Override
    public String getConfirmationMessage() {
        return "Foreign keys on base table " + getBaseTableName() + " dropped";
    }

    /**
     * Iterates through all the FOREIGN KEYs of the target table and outputs a list of DropForeignKeyConstraintChanges
     * for a given database type.
     *
     * @param database the database type for which subchanges need to be generated
     * @return the list of generated DropForeignKeyConstraintChanges
     */
    private List<DropForeignKeyConstraintChange> generateChildren(Database database) {
        // Make a new list
        List<DropForeignKeyConstraintChange> childDropChanges = new ArrayList<>();

        try {
            CatalogAndSchema catalogAndSchema =
                    new CatalogAndSchema(getBaseTableCatalogName(), getBaseTableSchemaName());
            catalogAndSchema = catalogAndSchema.standardize(database);
            Table target = SnapshotGeneratorFactory.getInstance().createSnapshot(
                    new Table(catalogAndSchema.getCatalogName(), catalogAndSchema.getSchemaName(),
                            database.correctObjectName(getBaseTableName(), Table.class))
                    , database, new SnapshotControl(database, Table.class, ForeignKey.class));

            List<ForeignKey> results = ((target == null) ? null : target.getOutgoingForeignKeys());
            Set<String> handledConstraints = new HashSet<>();

            if ((results != null) && (!results.isEmpty())) {
                for (ForeignKey fk : results) {
                    Table baseTable = fk.getForeignKeyTable();
                    String constraintName = fk.getName();
                    if (DatabaseObjectComparatorFactory.getInstance().isSameObject(
                            baseTable,
                            target,
                            null,
                            database
                    )) {
                        if( !handledConstraints.contains(constraintName)) {
                            DropForeignKeyConstraintChange dropForeignKeyConstraintChange =
                                    new DropForeignKeyConstraintChange();

                            dropForeignKeyConstraintChange.setBaseTableSchemaName(getBaseTableSchemaName());
                            dropForeignKeyConstraintChange.setBaseTableName(baseTableName);
                            dropForeignKeyConstraintChange.setConstraintName(constraintName);

                            childDropChanges.add(dropForeignKeyConstraintChange);
                            handledConstraints.add(constraintName);
                        }
                    } else {
                        throw new UnexpectedLiquibaseException(
                                "Expected to return only foreign keys for base table name: " +
                                        getBaseTableName() + " and got results for table: " + baseTableName);
                    }
                }
            }

            return childDropChanges;

        } catch (DatabaseException | InvalidExampleException e) {
            throw new UnexpectedLiquibaseException("Failed to find foreign keys for table: " + getBaseTableName(), e);
        }
    }

    @Override
    public boolean generateStatementsVolatile(Database database) {
        return true;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
