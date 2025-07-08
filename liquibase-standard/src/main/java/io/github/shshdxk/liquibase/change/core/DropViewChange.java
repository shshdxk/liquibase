package io.github.shshdxk.liquibase.change.core;

import static io.github.shshdxk.liquibase.change.ChangeParameterMetaData.ALL;

import io.github.shshdxk.liquibase.change.AbstractChange;
import io.github.shshdxk.liquibase.change.ChangeMetaData;
import io.github.shshdxk.liquibase.change.ChangeStatus;
import io.github.shshdxk.liquibase.change.DatabaseChange;
import io.github.shshdxk.liquibase.change.DatabaseChangeProperty;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.snapshot.SnapshotGeneratorFactory;
import io.github.shshdxk.liquibase.statement.SqlStatement;
import io.github.shshdxk.liquibase.statement.core.DropViewStatement;
import io.github.shshdxk.liquibase.structure.core.View;

/**
 * Drops an existing view.
 */
@DatabaseChange(name = "dropView", description = "Drops an existing view", priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "view")
public class DropViewChange extends AbstractChange {
    private String catalogName;
    private String schemaName;
    private String viewName;
    private Boolean ifExists;

    @DatabaseChangeProperty(mustEqualExisting ="view.catalog", since = "3.0", description = "Name of the database catalog")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting ="view.schema", description = "Name of the database schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "view", description = "Name of the view to drop")
    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    @DatabaseChangeProperty(since = "4.19.0", supportsDatabase = ALL,
        description = "Appends IF EXISTS to the DROP VIEW statement. If ifExists=true, the view is only dropped if it already exists, but the migration continues even if the view does not exist. If ifExists=false and the view does not exist, the database returns an error. Default: false.")
    public Boolean isIfExists() {
        return ifExists;
    }

    public void setIfExists(Boolean ifExists) {
        this.ifExists = ifExists;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[]{
                new DropViewStatement(getCatalogName(), getSchemaName(), getViewName(), isIfExists()),
        };
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        try {
            return new ChangeStatus().assertComplete(!SnapshotGeneratorFactory.getInstance().has(new View(getCatalogName(), getSchemaName(), getViewName()), database), "View exists");
        } catch (Exception e) {
            return new ChangeStatus().unknown(e);
        }
    }


    @Override
    public String getConfirmationMessage() {
        return "View "+getViewName()+" dropped";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}
