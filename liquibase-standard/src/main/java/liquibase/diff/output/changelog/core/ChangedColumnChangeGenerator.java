package liquibase.diff.output.changelog.core;

import liquibase.Scope;
import liquibase.change.AddColumnConfig;
import liquibase.change.Change;
import liquibase.change.core.*;
import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.datatype.LiquibaseDataType;
import liquibase.datatype.core.BooleanType;
import liquibase.datatype.core.VarcharType;
import liquibase.diff.Difference;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.AbstractChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.ChangedObjectChangeGenerator;
import liquibase.statement.DatabaseFunction;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import liquibase.util.ISODateFormat;

import java.util.*;

public class ChangedColumnChangeGenerator extends AbstractChangeGenerator implements ChangedObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Column.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return new Class[] {
                Table.class
        };
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return new Class[] {
                PrimaryKey.class
        };
    }

    @Override
    public Change[] fixChanged(DatabaseObject changedObject, ObjectDifferences differences, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        Column column = (Column) changedObject;
        if (column.getRelation() instanceof View) {
            return null;
        }

        if (column.getRelation().getSnapshotId() == null) { //not an actual table, maybe an alias, maybe in a different schema. Don't fix it.
            return null;
        }

        List<Change> changes = new ArrayList<>();

        if (comparisonDatabase instanceof MySQLDatabase) {
            String catalogName = null;
            String schemaName = null;
            if (control.getIncludeCatalog()) {
                catalogName = column.getRelation().getSchema().getCatalog().getName();
            }
            if (control.getIncludeSchema()) {
                schemaName = column.getRelation().getSchema().getName();
            }

            String tableName = column.getRelation().getName();

            boolean changed = false;
            Difference typeDifference = differences.getDifference("type");
            if (typeDifference != null) {
                LiquibaseDataType type1 = DataTypeFactory.getInstance().from((DataType) differences.getDifference("type")
                        .getReferenceValue(), comparisonDatabase);
                LiquibaseDataType type2 = DataTypeFactory.getInstance().from((DataType) differences.getDifference("type")
                        .getComparedValue(), comparisonDatabase);
                if (!type1.getName().equals(type2.getName())) {
                    changed = true;
                } else if ("varchar".equalsIgnoreCase(type1.getName())) {
                    Object size1 = type1.getParameters().length > 0 ? type1.getParameters()[0] : null;
                    Object size2 = type2.getParameters().length > 0 ? type2.getParameters()[0] : null;
                    if (size1 != null && size2 != null && !Objects.equals(size1, size2)) {
                        changed = true;
                    }
                }
            }
            Difference nullableDifference = differences.getDifference("nullable");
            if (!changed && nullableDifference != null && nullableDifference.getReferenceValue() != null) {
                changed = true;
            }
            Difference difference = differences.getDifference("defaultValue");
            if (!changed && difference != null) {
                changed = true;
            }
            Difference remarksDiff = differences.getDifference("remarks");
            if (!changed && remarksDiff != null) {
                changed = true;
            }
            if (!changed) {
                return changes.toArray(EMPTY_CHANGE);
            }
            column.setRelation(null);
            ModifyColumnChange modifyColumn = new ModifyColumnChange();
            modifyColumn.setCatalogName(catalogName);
            modifyColumn.setSchemaName(schemaName);
            modifyColumn.setTableName(tableName);
            AddColumnConfig addColumnConfig = new AddColumnConfig(column);
//            addColumnConfig.setName(column.getName());
//            addColumnConfig.setType(column.getType().toString());
//            addColumnConfig.setConstraints(null);
            modifyColumn.setColumns(Collections.singletonList(addColumnConfig));
            changes.add(modifyColumn);
        } else {
            handleTypeDifferences(column, differences, control, changes, referenceDatabase, comparisonDatabase);
            handleNullableDifferences(column, differences, control, changes, referenceDatabase, comparisonDatabase);
            handleDefaultValueDifferences(column, differences, control, changes, referenceDatabase, comparisonDatabase);
            handleAutoIncrementDifferences(column, differences, control, changes, referenceDatabase, comparisonDatabase);

            Difference remarksDiff = differences.getDifference("remarks");
            if (remarksDiff != null) {
                SetColumnRemarksChange change = new SetColumnRemarksChange();
                if (control.getIncludeCatalog()) {
                    change.setCatalogName(column.getSchema().getCatalogName());
                }
                if (control.getIncludeSchema()) {
                    change.setSchemaName(column.getSchema().getName());
                }
                change.setTableName(column.getRelation().getName());
                change.setColumnName(column.getName());
                change.setRemarks(column.getRemarks());

                LiquibaseDataType columnDataType = DataTypeFactory.getInstance().from(column.getType(), comparisonDatabase);
                if (columnDataType != null) {
                    change.setColumnDataType(columnDataType.toDatabaseDataType(comparisonDatabase).getType());
                }

                changes.add(change);
            }
        }

        return changes.toArray(EMPTY_CHANGE);
    }

    protected void handleNullableDifferences(Column column, ObjectDifferences differences, DiffOutputControl control, List<Change> changes, Database referenceDatabase, Database comparisonDatabase) {
        Difference nullableDifference = differences.getDifference("nullable");
        if ((nullableDifference != null) && (nullableDifference.getReferenceValue() != null)) {
            boolean nullable = (Boolean) nullableDifference.getReferenceValue();
            if (nullable) {
                DropNotNullConstraintChange change = new DropNotNullConstraintChange();
                if (control.getIncludeCatalog()) {
                    change.setCatalogName(column.getRelation().getSchema().getCatalog().getName());
                }
                if (control.getIncludeSchema()) {
                    change.setSchemaName(column.getRelation().getSchema().getName());
                }
                change.setTableName(column.getRelation().getName());
                change.setColumnName(column.getName());
                change.setColumnDataType(DataTypeFactory.getInstance().from(column.getType(), comparisonDatabase).toDatabaseDataType(comparisonDatabase).getType());
                changes.add(change);
            } else {
                AddNotNullConstraintChange change = new AddNotNullConstraintChange();
                if (control.getIncludeCatalog()) {
                    change.setCatalogName(column.getRelation().getSchema().getCatalog().getName());
                }
                if (control.getIncludeSchema()) {
                    change.setSchemaName(column.getRelation().getSchema().getName());
                }
                change.setTableName(column.getRelation().getName());
                change.setColumnName(column.getName());
                change.setColumnDataType(DataTypeFactory.getInstance().from(column.getType(), comparisonDatabase).toDatabaseDataType(comparisonDatabase).getType());
                change.setValidate(column.getValidate());
                change.setConstraintName(column.getAttribute("notNullConstraintName", String.class));
                changes.add(change);
            }
        }
    }

    protected void handleAutoIncrementDifferences(Column column, ObjectDifferences differences, DiffOutputControl control, List<Change> changes, Database referenceDatabase, Database comparisonDatabase) {
        Difference difference = differences.getDifference("autoIncrementInformation");
        if (difference != null) {
            if (difference.getReferenceValue() == null) {
                Scope.getCurrentScope().getLog(getClass()).info("ChangedColumnChangeGenerator cannot fix dropped auto increment values");
                //todo: Support dropping auto increments
            } else {
                AddAutoIncrementChange change = new AddAutoIncrementChange();
                if (control.getIncludeCatalog()) {
                    change.setCatalogName(column.getRelation().getSchema().getCatalog().getName());
                }
                if (control.getIncludeSchema()) {
                    change.setSchemaName(column.getRelation().getSchema().getName());
                }
                change.setTableName(column.getRelation().getName());
                change.setColumnName(column.getName());
                change.setColumnDataType(DataTypeFactory.getInstance().from(column.getType(), comparisonDatabase).toString());
                changes.add(change);
            }
        }
    }

    protected void handleTypeDifferences(Column column, ObjectDifferences differences, DiffOutputControl control, List<Change> changes, Database referenceDatabase, Database comparisonDatabase) {
        Difference typeDifference = differences.getDifference("type");
        if (typeDifference != null) {
            String catalogName = null;
            String schemaName = null;
            if (control.getIncludeCatalog()) {
                catalogName = column.getRelation().getSchema().getCatalog().getName();
            }
            if (control.getIncludeSchema()) {
                schemaName = column.getRelation().getSchema().getName();
            }


            String tableName = column.getRelation().getName();

            if ((comparisonDatabase instanceof OracleDatabase) && ("clob".equalsIgnoreCase(((DataType) typeDifference
                .getReferenceValue()).getTypeName()) || "clob".equalsIgnoreCase(((DataType) typeDifference
                .getComparedValue()).getTypeName()))) {
                String tempColName = "TEMP_CLOB_CONVERT";
                OutputChange outputChange = new OutputChange();
                outputChange.setMessage("Cannot convert directly from " + ((DataType) typeDifference.getComparedValue()).getTypeName()+" to "+((DataType) typeDifference.getReferenceValue()).getTypeName()+". Instead a new column will be created and the data transferred. This may cause unexpected side effects including constraint issues and/or table locks.");
                changes.add(outputChange);

                AddColumnChange addColumn = new AddColumnChange();
                addColumn.setCatalogName(catalogName);
                addColumn.setSchemaName(schemaName);
                addColumn.setTableName(tableName);
                AddColumnConfig addColumnConfig = new AddColumnConfig(column);
                addColumnConfig.setName(tempColName);
                addColumnConfig.setType(typeDifference.getReferenceValue().toString());
                addColumnConfig.setAfterColumn(column.getName());
                addColumn.setColumns(Arrays.asList(addColumnConfig));
                changes.add(addColumn);

                changes.add(new RawSQLChange("UPDATE "+referenceDatabase.escapeObjectName(tableName, Table.class)+" SET "+tempColName+"="+referenceDatabase.escapeObjectName(column.getName(), Column.class)));

                DropColumnChange dropColumnChange = new DropColumnChange();
                dropColumnChange.setCatalogName(catalogName);
                dropColumnChange.setSchemaName(schemaName);
                dropColumnChange.setTableName(tableName);
                dropColumnChange.setColumnName(column.getName());
                changes.add(dropColumnChange);

                RenameColumnChange renameColumnChange = new RenameColumnChange();
                renameColumnChange.setCatalogName(catalogName);
                renameColumnChange.setSchemaName(schemaName);
                renameColumnChange.setTableName(tableName);
                renameColumnChange.setOldColumnName(tempColName);
                renameColumnChange.setNewColumnName(column.getName());
                changes.add(renameColumnChange);

            } else {
                if ((comparisonDatabase instanceof MSSQLDatabase) && (column.getDefaultValue() != null)) { //have to drop the default value, will be added back with the "data type changed" logic.
                    DropDefaultValueChange dropDefaultValueChange = new DropDefaultValueChange();
                    dropDefaultValueChange.setCatalogName(catalogName);
                    dropDefaultValueChange.setSchemaName(schemaName);
                    dropDefaultValueChange.setTableName(tableName);
                    dropDefaultValueChange.setColumnName(column.getName());
                    changes.add(dropDefaultValueChange);
                }

                ModifyDataTypeChange change = new ModifyDataTypeChange();
                change.setCatalogName(catalogName);
                change.setSchemaName(schemaName);
                change.setTableName(tableName);
                change.setColumnName(column.getName());
                DataType referenceType = (DataType) typeDifference.getReferenceValue();
                change.setNewDataType(DataTypeFactory.getInstance().from(referenceType, comparisonDatabase).toString());

                changes.add(change);
            }
        }
    }

    protected void handleDefaultValueDifferences(Column column, ObjectDifferences differences, DiffOutputControl control, List<Change> changes, Database referenceDatabase, Database comparisonDatabase) {
        Difference difference = differences.getDifference("defaultValue");

        if (difference != null) {
            Object value = difference.getReferenceValue();

            LiquibaseDataType columnDataType = DataTypeFactory.getInstance().from(column.getType(), comparisonDatabase);
            if (value == null) {
                DropDefaultValueChange change = new DropDefaultValueChange();
                if (control.getIncludeCatalog()) {
                    change.setCatalogName(column.getRelation().getSchema().getCatalog().getName());
                }
                if (control.getIncludeSchema()) {
                    change.setSchemaName(column.getRelation().getSchema().getName());
                }
                change.setTableName(column.getRelation().getName());
                change.setColumnName(column.getName());
                change.setColumnDataType(columnDataType.toString());

                changes.add(change);

            } else if (shouldTriggerAddDefaultChange(column, difference, comparisonDatabase)) {
                AddDefaultValueChange change = new AddDefaultValueChange();
                if (control.getIncludeCatalog()) {
                    change.setCatalogName(column.getRelation().getSchema().getCatalog().getName());
                }
                if (control.getIncludeSchema()) {
                    change.setSchemaName(column.getRelation().getSchema().getName());
                }
                change.setTableName(column.getRelation().getName());
                change.setColumnName(column.getName());
                change.setColumnDataType(columnDataType.toString());

                //
                // Make sure we handle BooleanType values which are not Boolean
                //
                if (value instanceof Boolean || columnDataType instanceof BooleanType) {
                    if (value instanceof Boolean) {
                        change.setDefaultValueBoolean((Boolean) value);
                    }
                    else if (columnDataType instanceof BooleanType) {
                        if (value instanceof DatabaseFunction) {
                            if (value.equals(new DatabaseFunction("'false'"))) {
                                change.setDefaultValueBoolean(false);
                            } else if (value.equals(new DatabaseFunction("'true'"))) {
                                change.setDefaultValueBoolean(true);
                            } else {
                                change.setDefaultValueComputed(((DatabaseFunction) value));
                            }
                        }
                    }
                } else if (value instanceof Date) {
                    change.setDefaultValueDate(new ISODateFormat().format(((Date) value)));
                } else if (value instanceof Number) {
                    change.setDefaultValueNumeric(value.toString());
                } else if (value instanceof DatabaseFunction) {
                    change.setDefaultValueComputed(((DatabaseFunction) value));
                } else {
                    change.setDefaultValue(value.toString());
                }
                change.setDefaultValueConstraintName(column.getDefaultValueConstraintName());


                changes.add(change);
            }
        }
    }

    /**
     * For {@link PostgresDatabase} if column is of autoIncrement/SERIAL type we can ignore 'defaultValue' differences
     * (because its execution of sequence.next() anyway)
     */
    private boolean shouldTriggerAddDefaultChange(Column column, Difference difference, Database comparisonDatabase) {
        if (!(comparisonDatabase instanceof PostgresDatabase)) {
            return true;
        }
        return column.getAutoIncrementInformation() == null || !(difference.getReferenceValue() instanceof DatabaseFunction);
    }
}
