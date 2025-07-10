package io.github.shshdxk.liquibase.precondition.core;

import io.github.shshdxk.liquibase.changelog.ChangeSet;
import io.github.shshdxk.liquibase.changelog.visitor.ChangeExecListener;
import io.github.shshdxk.liquibase.changelog.DatabaseChangeLog;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.OracleDatabase;
import io.github.shshdxk.liquibase.exception.PreconditionErrorException;
import io.github.shshdxk.liquibase.exception.PreconditionFailedException;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.exception.Warnings;
import io.github.shshdxk.liquibase.precondition.AbstractPrecondition;
import io.github.shshdxk.liquibase.snapshot.SnapshotGeneratorFactory;
import io.github.shshdxk.liquibase.structure.core.ForeignKey;
import io.github.shshdxk.liquibase.structure.core.Schema;
import io.github.shshdxk.liquibase.structure.core.Table;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Setter
@Getter
public class ForeignKeyExistsPrecondition extends AbstractPrecondition {
    private String catalogName;
    private String schemaName;
    private String foreignKeyTableName;
    private String foreignKeyName;

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public Warnings warn(Database database) {
        return new Warnings();
    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }

    @Override
    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet, ChangeExecListener changeExecListener) throws PreconditionFailedException, PreconditionErrorException {
        try {
            ForeignKey example = new ForeignKey();
            example.setName(getForeignKeyName());
            example.setForeignKeyTable(new Table());
            if (StringUtils.trimToNull(getForeignKeyTableName()) != null) {
                example.getForeignKeyTable().setName(getForeignKeyTableName());
            }
            String catalogName = getCatalogName();
            if(!(database instanceof OracleDatabase) && getCatalogName() == null) {
                catalogName = database.getDefaultCatalogName();
            }
            String schemaName = getSchemaName() != null ? getSchemaName() : database.getDefaultSchemaName();
            example.getForeignKeyTable().setSchema(new Schema(catalogName, schemaName));

            if (!SnapshotGeneratorFactory.getInstance().hasIgnoreNested(example, database)) {
                throw new PreconditionFailedException("Foreign Key " +
                    database.escapeIndexName(catalogName, schemaName, foreignKeyName) + " does not exist",
                    changeLog,
                    this
                );
            }
        } catch (PreconditionFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
    }

    @Override
    public String getName() {
        return "foreignKeyConstraintExists";
    }
}
