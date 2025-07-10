package io.github.shshdxk.liquibase.precondition.core;

import io.github.shshdxk.liquibase.changelog.ChangeSet;
import io.github.shshdxk.liquibase.changelog.DatabaseChangeLog;
import io.github.shshdxk.liquibase.changelog.visitor.ChangeExecListener;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.*;
import io.github.shshdxk.liquibase.exception.*;
import io.github.shshdxk.liquibase.precondition.AbstractPrecondition;
import io.github.shshdxk.liquibase.snapshot.SnapshotGeneratorFactory;
import io.github.shshdxk.liquibase.structure.core.PrimaryKey;
import io.github.shshdxk.liquibase.structure.core.Schema;
import io.github.shshdxk.liquibase.structure.core.Table;
import io.github.shshdxk.liquibase.util.StringUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrimaryKeyExistsPrecondition extends AbstractPrecondition {

    private String catalogName;
    private String schemaName;
    private String primaryKeyName;
    private String tableName;

    @Override
    public Warnings warn(Database database) {
        return new Warnings();
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        if ((getPrimaryKeyName() == null) && (getTableName() == null)) {
            validationErrors.addError("Either primaryKeyName or tableName must be set");
        }
        return validationErrors;
    }

    @Override
    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet, ChangeExecListener changeExecListener)
            throws PreconditionFailedException, PreconditionErrorException {
        try {
            PrimaryKey example = new PrimaryKey();
            Table table = new Table();
            table.setSchema(new Schema(getCatalogName(), getSchemaName()));
            if (StringUtil.trimToNull(getTableName()) != null) {
                table.setName(getTableName());
            } else if (database instanceof H2Database || database instanceof MySQLDatabase || database instanceof HsqlDatabase
                || database instanceof SQLiteDatabase || database instanceof DB2Database) {
                throw new DatabaseException("Database driver requires a table name to be specified in order to search for a primary key.");
            }
            example.setTable(table);
            example.setName(getPrimaryKeyName());

            if (!SnapshotGeneratorFactory.getInstance().hasIgnoreNested(example, database)) {
                if (tableName != null) {
                    throw new PreconditionFailedException("Primary Key does not exist on " + database.escapeObjectName(getTableName(), Table.class), changeLog, this);
                } else {
                    throw new PreconditionFailedException("Primary Key " + database.escapeObjectName(getPrimaryKeyName(), PrimaryKey.class) + " does not exist", changeLog, this);
                }
            }
        } catch (PreconditionFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public String getName() {
        return "primaryKeyExists";
    }
}
