package io.github.shshdxk.liquibase.precondition.core;

import io.github.shshdxk.liquibase.changelog.ChangeSet;
import io.github.shshdxk.liquibase.changelog.visitor.ChangeExecListener;
import io.github.shshdxk.liquibase.changelog.DatabaseChangeLog;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.exception.PreconditionErrorException;
import io.github.shshdxk.liquibase.exception.PreconditionFailedException;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.exception.Warnings;
import io.github.shshdxk.liquibase.precondition.AbstractPrecondition;
import io.github.shshdxk.liquibase.snapshot.SnapshotGeneratorFactory;
import io.github.shshdxk.liquibase.structure.core.Column;
import io.github.shshdxk.liquibase.structure.core.Index;
import io.github.shshdxk.liquibase.structure.core.Schema;
import io.github.shshdxk.liquibase.structure.core.Table;
import io.github.shshdxk.liquibase.util.StringUtil;

public class IndexExistsPrecondition extends AbstractPrecondition {
    private String catalogName;
    private String schemaName;
    private String tableName;
    private String columnNames;
    private String indexName;

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(String columnNames) {
        this.columnNames = columnNames;
    }

    @Override
    public Warnings warn(Database database) {
        return new Warnings();
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        if (getIndexName() == null && (getTableName() == null || getColumnNames() == null)) {
            validationErrors.addError("indexName OR (tableName and columnNames) is required");
        }
        return validationErrors;
    }

    @Override
    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet, ChangeExecListener changeExecListener)
            throws PreconditionFailedException, PreconditionErrorException {
        try {
            Schema schema = new Schema(getCatalogName(), getSchemaName());
            Index example = new Index();
            String tableName = StringUtil.trimToNull(getTableName());
            if (tableName != null) {
                example.setRelation(new Table()
                        .setName(database.correctObjectName(getTableName(), Table.class))
                        .setSchema(schema));
            }
            example.setName(database.correctObjectName(getIndexName(), Index.class));
            if (StringUtil.trimToNull(getColumnNames()) != null) {
                for (String column : getColumnNames().split("\\s*,\\s*")) {
                    example.addColumn(new Column(database.correctObjectName(column, Column.class)));
                }
            }
            if (!SnapshotGeneratorFactory.getInstance().has(example, database)) {
                String name = "";

                if (getIndexName() != null) {
                    name += database.escapeObjectName(getIndexName(), Index.class);
                }

                if (tableName != null) {
                    name += " on "+database.escapeObjectName(getTableName(), Table.class);

                    if (StringUtil.trimToNull(getColumnNames()) != null) {
                        name += " columns "+getColumnNames();
                    }
                }
                throw new PreconditionFailedException("Index "+ name +" does not exist", changeLog, this);
            }
        } catch (Exception e) {
            if (e instanceof PreconditionFailedException) {
                throw (((PreconditionFailedException) e));
            }
            throw new PreconditionErrorException(e, changeLog, this);
        }
    }

    @Override
    public String getName() {
        return "indexExists";
    }

    @Override
    public String toString() {
        String string = "Index Exists Precondition: ";

        if (getIndexName() != null) {
            string += getIndexName();
        }

        if (tableName != null) {
            string += " on "+getTableName();

            if (StringUtil.trimToNull(getColumnNames()) != null) {
                string += " columns "+getColumnNames();
            }
        }

        return string;
    }
}
