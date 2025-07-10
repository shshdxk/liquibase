package io.github.shshdxk.liquibase.change.core;

import io.github.shshdxk.liquibase.change.AbstractChange;
import io.github.shshdxk.liquibase.change.Change;
import io.github.shshdxk.liquibase.change.ChangeMetaData;
import io.github.shshdxk.liquibase.change.DatabaseChange;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.statement.SqlStatement;

@DatabaseChange(name = "empty", description = "empty", priority = ChangeMetaData.PRIORITY_DEFAULT)
public class EmptyChange extends AbstractChange {

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return SqlStatement.EMPTY_SQL_STATEMENT;
    }

    @Override
    public String getConfirmationMessage() {
        return "Empty change did nothing";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    protected Change[] createInverses() {
        return EMPTY_CHANGE;
    }
}
