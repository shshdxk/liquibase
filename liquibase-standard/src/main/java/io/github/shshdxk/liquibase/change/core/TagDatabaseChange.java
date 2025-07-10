package io.github.shshdxk.liquibase.change.core;

import io.github.shshdxk.liquibase.ChecksumVersion;
import io.github.shshdxk.liquibase.Scope;
import io.github.shshdxk.liquibase.change.*;
import io.github.shshdxk.liquibase.changelog.ChangeLogHistoryServiceFactory;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.exception.DatabaseException;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.sqlgenerator.core.MarkChangeSetRanGenerator;
import io.github.shshdxk.liquibase.statement.SqlStatement;
import io.github.shshdxk.liquibase.statement.core.MarkChangeSetRanStatement;
import lombok.Setter;

@DatabaseChange(name = "tagDatabase", description = "Applies a tag to the database to specify where to stop a rollback",
    priority = ChangeMetaData.PRIORITY_DEFAULT, since = "1.6")
@Setter
public class TagDatabaseChange extends AbstractChange {

    private String tag;

    private Boolean keepTagOnRollback;

    @DatabaseChangeProperty(description = "Tag to apply", exampleValue = "version_1.3")
    public String getTag() {
        return tag;
    }

    @DatabaseChangeProperty(description = "Tag should not be removed during a rollback. Default: false.")
    public Boolean isKeepTagOnRollback() {
        return keepTagOnRollback;
    }

    /**
     * {@inheritDoc}
     * @see MarkChangeSetRanGenerator#generateSql(MarkChangeSetRanStatement, Database, SqlGeneratorChain)
     */
    @Override
    public SqlStatement[] generateStatements(Database database) {
        return SqlStatement.EMPTY_SQL_STATEMENT;
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        try {
            return new ChangeStatus().assertComplete(
                Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(database).tagExists(getTag()), "Database not tagged");
        } catch (DatabaseException e) {
            return new ChangeStatus().unknown(e);
        }
    }

    @Override
    public String getConfirmationMessage() {
        return "Tag '"+tag+"' applied to database";
    }

    @Override
    protected Change[] createInverses() {
        return EMPTY_CHANGE;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public String[] getExcludedFieldFilters(ChecksumVersion version) {
        return new String[]{
                "keepTagOnRollback"
        };
    }
}
