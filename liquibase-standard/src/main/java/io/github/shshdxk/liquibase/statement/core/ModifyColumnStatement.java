package io.github.shshdxk.liquibase.statement.core;

import io.github.shshdxk.liquibase.statement.*;

import java.util.*;

public class ModifyColumnStatement extends AbstractSqlStatement {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String columnName;
    private String columnType;
    private Object defaultValue;
    private String defaultValueConstraintName;
    private String remarks;
    private String addAfterColumn;
    private String addBeforeColumn;
    private Integer addAtPosition;
    private Boolean computed;
    private Set<ColumnConstraint> constraints = new HashSet<>();

    private List<ModifyColumnStatement> columns = new ArrayList<>();

    public ModifyColumnStatement(String catalogName, String schemaName, String tableName, String columnName, String columnType, Object defaultValue, ColumnConstraint... constraints) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columnName = columnName;
        this.columnType = columnType;
        this.defaultValue = defaultValue;
        if (constraints != null) {
            this.constraints.addAll(Arrays.asList(constraints));
        }
    }

    public ModifyColumnStatement(String catalogName, String schemaName, String tableName, String columnName, String columnType, Object defaultValue, String remarks, ColumnConstraint... constraints) {
        this(catalogName,schemaName,tableName,columnName,columnType,defaultValue,constraints);
        this.remarks = remarks;
    }

    public ModifyColumnStatement(List<ModifyColumnStatement> columns) {
        this.columns.addAll(columns);
    }

    public ModifyColumnStatement(ModifyColumnStatement... columns) {
        this(Arrays.asList(columns));
    }


    public boolean isMultiple() {
        return !columns.isEmpty();
    }

    public List<ModifyColumnStatement> getColumns() {
        return columns;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getColumnType() {
        return columnType;
    }

    public String getRemarks() {
        return remarks;
    }

    public Set<ColumnConstraint> getConstraints() {
        return constraints;
    }

    public boolean isAutoIncrement() {
        for (ColumnConstraint constraint : getConstraints()) {
            if (constraint instanceof AutoIncrementConstraint) {
                return true;
            }
        }
        return false;
    }

    public boolean isNullable() {
        for (ColumnConstraint constraint : getConstraints()) {
            if (constraint instanceof NotNullConstraint) {
                return false;
            }
        }
        return true;
    }

    public boolean shouldValidateNullable() {
        for (ColumnConstraint constraint : getConstraints()) {
            if (constraint instanceof NotNullConstraint) {
                if (!((NotNullConstraint) constraint).shouldValidateNullable()) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isUnique() {
        for (ColumnConstraint constraint : getConstraints()) {
            if (constraint instanceof UniqueConstraint) {
                return true;
            }
        }
        return false;
    }


    public Object getDefaultValue() {
        return defaultValue;
    }

    public String getAddAfterColumn() {
        return addAfterColumn;
    }

    public void setAddAfterColumn(String addAfterColumn) {
        this.addAfterColumn = addAfterColumn;
    }

    public String getAddBeforeColumn() {
        return addBeforeColumn;
    }

    public void setAddBeforeColumn(String addBeforeColumn) {
        this.addBeforeColumn = addBeforeColumn;
    }

    public Integer getAddAtPosition() {
        return addAtPosition;
    }

    public void setAddAtPosition(Integer addAtPosition) {
        this.addAtPosition = addAtPosition;
    }

    public String getDefaultValueConstraintName() {
        return defaultValueConstraintName;
    }

    public void setDefaultValueConstraintName(String defaultValueConstraintName) {
        this.defaultValueConstraintName = defaultValueConstraintName;
    }

    public Boolean getComputed() {
        return computed;
    }

    public void setComputed(Boolean computed) {
        this.computed = computed;
    }
}
