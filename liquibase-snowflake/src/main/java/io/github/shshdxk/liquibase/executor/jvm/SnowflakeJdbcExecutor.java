package io.github.shshdxk.liquibase.executor.jvm;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.SnowflakeDatabase;
import io.github.shshdxk.liquibase.exception.DatabaseException;
import io.github.shshdxk.liquibase.sql.visitor.SqlVisitor;
import io.github.shshdxk.liquibase.statement.SqlStatement;
import io.github.shshdxk.liquibase.statement.core.SetColumnRemarksStatement;

import java.util.List;

import static io.github.shshdxk.liquibase.sqlgenerator.core.SetColumnRemarksGeneratorSnowflake.SET_COLUMN_REMARKS_NOT_SUPPORTED_ON_VIEW_MSG;

public class SnowflakeJdbcExecutor extends JdbcExecutor {

    @Override
    public boolean supports(Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public int getPriority() {
        return PRIORITY_SPECIALIZED;
    }

    @Override
    public void execute(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        try {
            super.execute(sql, sqlVisitors);
        } catch (DatabaseException e) {
            if (sql instanceof SetColumnRemarksStatement &&
                        e.getMessage().contains("Object found is of type 'VIEW', not specified type 'TABLE'")) {
                throw new DatabaseException(SET_COLUMN_REMARKS_NOT_SUPPORTED_ON_VIEW_MSG, e);
            }
            throw e;
        }
    }
}
