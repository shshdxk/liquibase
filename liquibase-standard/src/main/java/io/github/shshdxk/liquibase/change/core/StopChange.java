package io.github.shshdxk.liquibase.change.core;

import io.github.shshdxk.liquibase.change.AbstractChange;
import io.github.shshdxk.liquibase.change.ChangeMetaData;
import io.github.shshdxk.liquibase.change.DatabaseChange;
import io.github.shshdxk.liquibase.change.DatabaseChangeProperty;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.parser.core.ParsedNode;
import io.github.shshdxk.liquibase.parser.core.ParsedNodeException;
import io.github.shshdxk.liquibase.resource.ResourceAccessor;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.statement.SqlStatement;
import io.github.shshdxk.liquibase.statement.core.RuntimeStatement;
import io.github.shshdxk.liquibase.util.StringUtil;

@DatabaseChange(name = "stop", description = "Stops Liquibase execution with a message. Mainly useful for debugging " +
    "and stepping through a changelog", priority = ChangeMetaData.PRIORITY_DEFAULT, since = "1.9")
public class StopChange extends AbstractChange {

    private String message = "Stop command in changelog file";

    @Override
    public boolean generateStatementsVolatile(Database database) {
        return true;
    }

    @DatabaseChangeProperty(description = "Message to send to output when execution stops", exampleValue = "What just happened???")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = StringUtil.trimToNull(message);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[] { new RuntimeStatement() {
            @Override
            public Sql[] generate(Database database) {
                throw new StopChangeException(getMessage());
            }
        }};

    }

    @Override
    public String getConfirmationMessage() {
        return "Changelog Execution Stopped";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    protected void customLoadLogic(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        Object value = parsedNode.getValue();
        if ((value instanceof String)) {
            setMessage((String) value);
        }
    }

    public static class StopChangeException extends RuntimeException {
        private static final long serialVersionUID = 6681759443230468424L;

        public StopChangeException(String message) {
            super(message);
        }
    }
}
