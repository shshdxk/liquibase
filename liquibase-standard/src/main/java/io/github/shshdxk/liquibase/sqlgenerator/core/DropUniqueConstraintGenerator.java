package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.change.ColumnConfig;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.CockroachDatabase;
import io.github.shshdxk.liquibase.database.core.MySQLDatabase;
import io.github.shshdxk.liquibase.database.core.OracleDatabase;
import io.github.shshdxk.liquibase.database.core.SQLiteDatabase;
import io.github.shshdxk.liquibase.database.core.SybaseASADatabase;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sql.UnparsedSql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.statement.core.DropUniqueConstraintStatement;
import io.github.shshdxk.liquibase.structure.core.Column;
import io.github.shshdxk.liquibase.structure.core.Table;
import io.github.shshdxk.liquibase.structure.core.UniqueConstraint;

public class DropUniqueConstraintGenerator extends AbstractSqlGenerator<DropUniqueConstraintStatement> {

    @Override
    public boolean supports(DropUniqueConstraintStatement statement, Database database) {
        return !(database instanceof SQLiteDatabase);
    }

    @Override
    public ValidationErrors validate(DropUniqueConstraintStatement dropUniqueConstraintStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", dropUniqueConstraintStatement.getTableName());
        validationErrors.checkRequiredField("constraintName", dropUniqueConstraintStatement.getConstraintName());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(DropUniqueConstraintStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String sql;
        if (database instanceof MySQLDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " DROP KEY " + database.escapeConstraintName(statement.getConstraintName());
        } else if (database instanceof OracleDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " DROP CONSTRAINT " + database.escapeConstraintName(statement.getConstraintName()) + " DROP INDEX";
        } else if (database instanceof SybaseASADatabase) {
            // Syntax is pretty regular, according to:
            // https://help.sap.com/viewer/40c01c3500744c85a02db71276495de5/17.0/en-US/8169d7966ce2101497b5ac611f7413ce.html
            sql = "ALTER TABLE "
                    + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())
                    + " DROP CONSTRAINT " + database.escapeConstraintName(statement.getConstraintName());
        } else if (database instanceof CockroachDatabase) {
            sql = "DROP INDEX " + database.escapeConstraintName(statement.getConstraintName()) + " CASCADE";
        } else {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " DROP CONSTRAINT " + database.escapeConstraintName(statement.getConstraintName());
        }

        return new Sql[] {
                new UnparsedSql(sql, getAffectedUniqueConstraint(statement))
        };
    }

    protected UniqueConstraint getAffectedUniqueConstraint(DropUniqueConstraintStatement statement) {
        UniqueConstraint constraint = new UniqueConstraint().setName(statement.getConstraintName()).setRelation(new Table().setName(statement.getTableName()).setSchema(statement.getCatalogName(), statement.getSchemaName()));
        if (statement.getUniqueColumns() != null) {
            int i = 0;
            for (ColumnConfig column : statement.getUniqueColumns()) {
                constraint.addColumn(i++, new Column(column));
            }
        }
        return constraint;
    }
}
