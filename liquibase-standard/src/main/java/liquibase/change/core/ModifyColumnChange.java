package liquibase.change.core;

import liquibase.change.*;
import liquibase.change.visitor.AddColumnChangeVisitor;
import liquibase.change.visitor.ChangeVisitor;
import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.parser.core.ParsedNodeException;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.AutoIncrementConstraint;
import liquibase.statement.ColumnConstraint;
import liquibase.statement.NotNullConstraint;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddColumnStatement;
import liquibase.statement.core.ModifyColumnStatement;
import liquibase.statement.core.UpdateStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Table;
import liquibase.util.ISODateFormat;
import liquibase.util.StringUtil;
import lombok.Setter;

import java.util.*;

import static liquibase.statement.SqlStatement.EMPTY_SQL_STATEMENT;

/**
 * Adds a column to an existing table.
 */
@DatabaseChange(name = "modifyColumn", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table",
    description = "Modify a new column to an existing table")
public class ModifyColumnChange extends AbstractChange implements ChangeWithColumns<AddColumnConfig> {

    @Setter
    private String catalogName;
    @Setter
    private String schemaName;
    @Setter
    private String tableName;
    private List<AddColumnConfig> columns;

    public ModifyColumnChange() {
        columns = new ArrayList<>();
    }

    @DatabaseChangeProperty(mustEqualExisting = "relation.catalog", since = "3.0", description = "Name of the database catalog")
    public String getCatalogName() {
        return catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "relation.schema", description = "Name of the database schema")
    public String getSchemaName() {
        return schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "table", description = "Name of the table to add the column to")
    public String getTableName() {
        return tableName;
    }

    @Override
    @DatabaseChangeProperty(requiredForDatabase = "all", description = "Column constraint and foreign key information. " +
        "Setting the \"defaultValue\" attribute specifies a default value for the column. " +
        "Setting the \"value\" attribute sets all rows existing to the specified value without modifying the column default.")
    public List<AddColumnConfig> getColumns() {
        return columns;
    }

    @Override
    public void setColumns(List<AddColumnConfig> columns) {
        this.columns = columns;
    }

    @Override
    public void addColumn(AddColumnConfig column) {
        this.columns.add(column);
    }

    public void removeColumn(ColumnConfig column) {
        this.columns.remove(column);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {

        List<SqlStatement> sql = new ArrayList<>();
        List<ModifyColumnStatement> modifyColumnStatements = new ArrayList<>();
        List<SqlStatement> addNotNullConstraintStatements = new ArrayList<>();

        if (getColumns().isEmpty()) {
            return new SqlStatement[]{
                    new AddColumnStatement(catalogName, schemaName, tableName, null, null, null)
            };
        }

        for (AddColumnConfig column : getColumns()) {
            Set<ColumnConstraint> constraints = new HashSet<>();
            ConstraintsConfig constraintsConfig = column.getConstraints();
            if (constraintsConfig != null) {
                if ((constraintsConfig.isNullable() != null) && !constraintsConfig.isNullable()) {
                    if (column.getValueObject() != null) {
                        List<SqlStatement> sqlStatements = generateAddNotNullConstraintStatements(column, constraintsConfig, database);
                        addNotNullConstraintStatements.addAll(sqlStatements);
                    } else {
                        NotNullConstraint notNullConstraint = createNotNullConstraint(constraintsConfig);
                        constraints.add(notNullConstraint);
                    }
                }

            }

            if ((column.isAutoIncrement() != null) && column.isAutoIncrement()) {
                constraints.add(new AutoIncrementConstraint(column.getName(), column.getStartWith(), column.getIncrementBy(), column.getGenerationType(), column.getDefaultOnNull()));
            }

            ModifyColumnStatement modifyColumnStatement = new ModifyColumnStatement(getCatalogName(), getSchemaName(),
                    getTableName(),
                    column.getName(),
                    column.getType(),
                    column.getDefaultValueObject(),
                    column.getRemarks(),
                    constraints.toArray(new ColumnConstraint[0]));
            modifyColumnStatement.setDefaultValueConstraintName(column.getDefaultValueConstraintName());
            modifyColumnStatement.setComputed(column.getComputed());

            modifyColumnStatement.setAddAfterColumn(column.getAfterColumn());
            modifyColumnStatement.setAddBeforeColumn(column.getBeforeColumn());
            modifyColumnStatement.setAddAtPosition(column.getPosition());

            modifyColumnStatements.add(modifyColumnStatement);

            if (column.getValueObject() != null) {
                UpdateStatement updateStatement = new UpdateStatement(getCatalogName(), getSchemaName(), getTableName());
                updateStatement.addNewColumnValue(column.getName(), column.getValueObject());
                sql.add(updateStatement);
            }
        }


        if (modifyColumnStatements.size() == 1) {
            sql.add(0, modifyColumnStatements.get(0));
        } else {
            sql.add(0, new ModifyColumnStatement(modifyColumnStatements));
        }

        sql.addAll(addNotNullConstraintStatements);
        return sql.toArray(EMPTY_SQL_STATEMENT);
    }

    @Override
    protected Change[] createInverses() {
        List<Change> inverses = new ArrayList<>();

        DropColumnChange inverse = new DropColumnChange();
        inverse.setCatalogName(getCatalogName());
        inverse.setSchemaName(getSchemaName());
        inverse.setTableName(getTableName());

        for (ColumnConfig aColumn : columns) {
            if (aColumn.hasDefaultValue()) {
                DropDefaultValueChange dropChange = new DropDefaultValueChange();
                dropChange.setTableName(getTableName());
                dropChange.setColumnName(aColumn.getName());
                dropChange.setSchemaName(getSchemaName());

                inverses.add(dropChange);
            }

            inverse.addColumn(aColumn);
        }
        inverses.add(inverse);
        return inverses.toArray(EMPTY_CHANGE);
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        ChangeStatus result = new ChangeStatus();
        try {
            for (AddColumnConfig column : getColumns()) {
                Column snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(new Column(Table.class, getCatalogName(), getSchemaName(), getTableName(), column.getName()), database);
                result.assertComplete(snapshot != null, "Column " + column.getName() + " does not exist");

                if (snapshot != null) {
                    PrimaryKey snapshotPK = ((Table) snapshot.getRelation()).getPrimaryKey();

                    ConstraintsConfig constraints = column.getConstraints();
                    if (constraints != null) {
                        result.assertComplete(constraints.isPrimaryKey() == ((snapshotPK != null) && snapshotPK
                                .getColumnNames().contains(column.getName())), "Column " + column.getName() + " not set as primary key");
                    }
                }
            }
        } catch (Exception e) {
            return result.unknown(e);
        }

        return result;
    }

    @Override
    public String getConfirmationMessage() {
        List<String> names = new ArrayList<>(columns.size());
        for (ColumnConfig col : columns) {
            names.add(col.getName() + "(" + col.getType() + ")");
        }

        return "Columns " + StringUtil.join(names, ",") + " added to " + tableName;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public void modify(ChangeVisitor changeVisitor) throws ParsedNodeException {
        if(!changeVisitor.getName().equals("addColumn")) {
            return;
        }
        String remove = ((AddColumnChangeVisitor)changeVisitor).getRemove();
        switch (remove){
            case "afterColumn":
                getColumns().forEach(c -> c.setAfterColumn(null));
                break;
            case "beforeColumn":
                getColumns().forEach(c -> c.setBeforeColumn(null));
                break;
            case "position":
                getColumns().forEach(c -> c.setPosition(null));
                break;
            default:
                throw new ParsedNodeException("Unexpected value found under removeChangeSetProperty for remove tag: " + remove);
        }
    }

    private NotNullConstraint createNotNullConstraint(ConstraintsConfig constraintsConfig) {
        NotNullConstraint notNullConstraint = new NotNullConstraint();
        if (constraintsConfig.getValidateNullable() != null && !constraintsConfig.getValidateNullable()) {
            notNullConstraint.setValidateNullable(false);
        }
        notNullConstraint.setConstraintName(constraintsConfig.getNotNullConstraintName());
        return notNullConstraint;
    }

    private List<SqlStatement> generateAddNotNullConstraintStatements(AddColumnConfig column, ConstraintsConfig constraints, Database database) {
        AddNotNullConstraintChange addNotNullConstraintChange = createAddNotNullConstraintChange(column, constraints);
        List<SqlStatement> returnList = new ArrayList<>(Arrays.asList(addNotNullConstraintChange.generateStatements(database)));

        if (database instanceof MySQLDatabase && column.getDefaultValueObject() != null) {
            //mysql's addNotNullConstraint call above loses the default value
            AddDefaultValueChange change = new AddDefaultValueChange();
            change.setCatalogName(this.getCatalogName());
            change.setSchemaName(this.getSchemaName());
            change.setTableName(this.getTableName());
            change.setColumnName(column.getName());
            change.setColumnDataType(column.getName());

            if (column.getDefaultValueDate() != null) {
                change.setDefaultValueDate(new ISODateFormat().format(column.getDefaultValueDate()));
            } else {
                //try to set them all, only one will be non-null
                change.setDefaultValue(column.getDefaultValue());
                Number defaultValueNumeric = column.getDefaultValueNumeric();
                if (defaultValueNumeric != null) {
                    change.setDefaultValueNumeric(String.valueOf(defaultValueNumeric));
                }
                change.setDefaultValueBoolean(column.getDefaultValueBoolean());
                change.setDefaultValueComputed(column.getDefaultValueComputed());
                change.setDefaultValueSequenceNext(column.getDefaultValueSequenceNext());
            }
            change.setDefaultValueConstraintName(column.getDefaultValueConstraintName());

            returnList.addAll(Arrays.asList(change.generateStatements(database)));
        }

        return returnList;
    }

    private AddNotNullConstraintChange createAddNotNullConstraintChange(AddColumnConfig column, ConstraintsConfig constraints) {
        AddNotNullConstraintChange addNotNullConstraintChange = new AddNotNullConstraintChange();
        addNotNullConstraintChange.setCatalogName(getCatalogName());
        addNotNullConstraintChange.setSchemaName(getSchemaName());
        addNotNullConstraintChange.setTableName(getTableName());
        addNotNullConstraintChange.setColumnName(column.getName());
        addNotNullConstraintChange.setColumnDataType(column.getType());
        addNotNullConstraintChange.setValidate(constraints.getValidateNullable());
        addNotNullConstraintChange.setConstraintName(constraints.getNotNullConstraintName());
        return addNotNullConstraintChange;
    }
}
