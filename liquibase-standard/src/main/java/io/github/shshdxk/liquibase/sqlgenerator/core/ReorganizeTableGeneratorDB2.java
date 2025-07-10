package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.GlobalConfiguration;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.DB2Database;
import io.github.shshdxk.liquibase.exception.DatabaseException;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sql.UnparsedSql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.statement.core.ReorganizeTableStatement;
import io.github.shshdxk.liquibase.structure.core.Relation;
import io.github.shshdxk.liquibase.structure.core.Table;

public class ReorganizeTableGeneratorDB2 extends AbstractSqlGenerator<ReorganizeTableStatement> {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(ReorganizeTableStatement statement, Database database) {
        return database instanceof DB2Database;
    }

    @Override
    public ValidationErrors validate(ReorganizeTableStatement reorganizeTableStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", reorganizeTableStatement.getTableName());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(ReorganizeTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        if (!GlobalConfiguration.AUTO_REORG.getCurrentValue()) {
            return null;
        }

        try {
            if (database.getDatabaseMajorVersion() >= 9) {
                return new Sql[]{
                        new UnparsedSql("CALL SYSPROC.ADMIN_CMD ('REORG TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + "')",
                                getAffectedTable(statement))
                };
            } else {
                return null;
            }
        } catch (DatabaseException e) {
            throw new RuntimeException(e);
        }
    }

    protected Relation getAffectedTable(ReorganizeTableStatement statement) {
        return new Table().setName(statement.getTableName()).setSchema(statement.getCatalogName(), statement.getSchemaName());
    }
}
