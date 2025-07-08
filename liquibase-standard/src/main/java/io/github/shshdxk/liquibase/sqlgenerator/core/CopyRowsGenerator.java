package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.change.ColumnConfig;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.SQLiteDatabase;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sql.UnparsedSql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.statement.core.CopyRowsStatement;
import io.github.shshdxk.liquibase.structure.core.Relation;
import io.github.shshdxk.liquibase.structure.core.Table;

public class CopyRowsGenerator extends AbstractSqlGenerator<CopyRowsStatement> {

    @Override
    public boolean supports(CopyRowsStatement statement, Database database) {
        return (database instanceof SQLiteDatabase);
    }

    @Override
    public ValidationErrors validate(CopyRowsStatement copyRowsStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("targetTable", copyRowsStatement.getTargetTable());
        validationErrors.checkRequiredField("sourceTable", copyRowsStatement.getSourceTable());
        validationErrors.checkRequiredField("copyColumns", copyRowsStatement.getCopyColumns());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(CopyRowsStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuilder sql = new StringBuilder();
        
        if (statement.getCopyColumns().isEmpty()) {
            return new Sql[]{};
        }
        
        if (database instanceof SQLiteDatabase) {
            sql.append("INSERT INTO ").append(database.escapeTableName(null, null, statement.getTargetTable())).append(" (");

            for (int i = 0; i < statement.getCopyColumns().size(); i++) {
                ColumnConfig column = statement.getCopyColumns().get(i);
                if (i > 0) {
                    sql.append(",");
                }
                sql.append(database.escapeColumnName(null, null, statement.getTargetTable(), column.getName()));
            }

            sql.append(") SELECT ");
            for (int i = 0; i < statement.getCopyColumns().size(); i++) {
                ColumnConfig column = statement.getCopyColumns().get(i);
                if (i > 0) {
                    sql.append(",");
                }
                sql.append(database.escapeColumnName(null, null, statement.getSourceTable(), column.getName()));
            }
            sql.append(" FROM ").append(database.escapeTableName(null, null, statement.getSourceTable()));
        }

        return new Sql[]{
                new UnparsedSql(sql.toString(), getAffectedTable(statement))
        };
    }

    protected Relation getAffectedTable(CopyRowsStatement statement) {
        return new Table().setName(statement.getTargetTable()).setSchema(null, null);
    }
}
