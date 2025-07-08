package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.core.MySQLDatabase;
import io.github.shshdxk.liquibase.database.core.SQLiteDatabase;
import io.github.shshdxk.liquibase.database.core.SybaseASADatabase;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sql.UnparsedSql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.statement.core.DropForeignKeyConstraintStatement;
import io.github.shshdxk.liquibase.structure.core.ForeignKey;
import io.github.shshdxk.liquibase.structure.core.Table;

public class DropForeignKeyConstraintGenerator extends AbstractSqlGenerator<DropForeignKeyConstraintStatement> {

    @Override
    public boolean supports(DropForeignKeyConstraintStatement statement, Database database) {
        return (!(database instanceof SQLiteDatabase));
    }

    @Override
    public ValidationErrors validate(DropForeignKeyConstraintStatement dropForeignKeyConstraintStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("baseTableName", dropForeignKeyConstraintStatement.getBaseTableName());
        validationErrors.checkRequiredField("constraintName", dropForeignKeyConstraintStatement.getConstraintName());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(DropForeignKeyConstraintStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        if ((database instanceof MySQLDatabase) || (database instanceof SybaseASADatabase)) {
            return new Sql[] { new UnparsedSql("ALTER TABLE " + database.escapeTableName(statement.getBaseTableCatalogName(), statement.getBaseTableSchemaName(), statement.getBaseTableName()) + " DROP FOREIGN KEY " + database.escapeConstraintName(statement.getConstraintName()), getAffectedForeignKey(statement)) };
        } else {
            return new Sql[] { new UnparsedSql("ALTER TABLE " + database.escapeTableName(statement.getBaseTableCatalogName(), statement.getBaseTableSchemaName(), statement.getBaseTableName()) + " DROP CONSTRAINT " + database.escapeConstraintName(statement.getConstraintName()), getAffectedForeignKey(statement)) };
        }

    }

    protected ForeignKey getAffectedForeignKey(DropForeignKeyConstraintStatement statement) {
        return new ForeignKey().setName(statement.getConstraintName()).setForeignKeyTable((Table) new Table().setName(statement.getBaseTableName()).setSchema(statement.getBaseTableCatalogName(), statement.getBaseTableSchemaName()));
    }
}
