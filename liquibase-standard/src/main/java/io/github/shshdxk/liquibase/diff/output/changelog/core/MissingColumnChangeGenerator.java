package io.github.shshdxk.liquibase.diff.output.changelog.core;

import io.github.shshdxk.liquibase.change.AddColumnConfig;
import io.github.shshdxk.liquibase.change.Change;
import io.github.shshdxk.liquibase.change.ConstraintsConfig;
import io.github.shshdxk.liquibase.change.core.AddColumnChange;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.datatype.DataTypeFactory;
import io.github.shshdxk.liquibase.datatype.DatabaseDataType;
import io.github.shshdxk.liquibase.datatype.LiquibaseDataType;
import io.github.shshdxk.liquibase.diff.output.DiffOutputControl;
import io.github.shshdxk.liquibase.diff.output.changelog.AbstractChangeGenerator;
import io.github.shshdxk.liquibase.diff.output.changelog.ChangeGeneratorChain;
import io.github.shshdxk.liquibase.diff.output.changelog.MissingObjectChangeGenerator;
import io.github.shshdxk.liquibase.structure.DatabaseObject;
import io.github.shshdxk.liquibase.structure.core.Column;
import io.github.shshdxk.liquibase.structure.core.PrimaryKey;
import io.github.shshdxk.liquibase.structure.core.Table;
import io.github.shshdxk.liquibase.structure.core.View;

public class MissingColumnChangeGenerator extends AbstractChangeGenerator implements MissingObjectChangeGenerator {

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
        return new Class[] { PrimaryKey.class };
    }

    @Override
    public Change[] fixMissing(DatabaseObject missingObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        Column column = (Column) missingObject;

        if (column.getRelation() instanceof View) {
            return null;
        }

        if (column.getRelation().getSnapshotId() == null) { //not an actual table, maybe an alias, maybe in a different schema. Don't fix it.
            return null;
        }


        AddColumnChange change = createAddColumnChange();
        change.setTableName(column.getRelation().getName());
        if (control.getIncludeCatalog()) {
            change.setCatalogName(column.getRelation().getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            change.setSchemaName(column.getRelation().getSchema().getName());
        }

        AddColumnConfig columnConfig = createAddColumnConfig();
        columnConfig.setName(column.getName());

        LiquibaseDataType ldt = DataTypeFactory.getInstance().from(column.getType(), referenceDatabase);
        DatabaseDataType ddt = ldt.toDatabaseDataType(comparisonDatabase);
        String typeString = ddt.toString();
        columnConfig.setType(typeString);

        MissingTableChangeGenerator.setDefaultValue(columnConfig, column, comparisonDatabase);

        Column.AutoIncrementInformation autoIncrementInfo = column.getAutoIncrementInformation();
        if (autoIncrementInfo != null) {
            columnConfig.setAutoIncrement(true);
            columnConfig.setGenerationType(autoIncrementInfo.getGenerationType());
            columnConfig.setDefaultOnNull(autoIncrementInfo.getDefaultOnNull());
        }

        if (column.getRemarks() != null) {
            columnConfig.setRemarks(column.getRemarks());
        }
        ConstraintsConfig constraintsConfig = columnConfig.getConstraints();
        if ((column.isNullable() != null) && !column.isNullable()) {
            if (constraintsConfig == null) {
                constraintsConfig = new ConstraintsConfig();
            }
            constraintsConfig.setNullable(false);
            constraintsConfig.setNotNullConstraintName(column.getAttribute("notNullConstraintName", String.class));
            if (!column.getValidateNullable()) {
                constraintsConfig.setValidateNullable(false);
            }
        }
        if (constraintsConfig != null) {
            columnConfig.setConstraints(constraintsConfig);
        }

        change.addColumn(columnConfig);

        return new Change[] { change };
    }

    protected AddColumnConfig createAddColumnConfig() {
        return new AddColumnConfig();
    }

    protected AddColumnChange createAddColumnChange() {
        return new AddColumnChange();
    }
}
