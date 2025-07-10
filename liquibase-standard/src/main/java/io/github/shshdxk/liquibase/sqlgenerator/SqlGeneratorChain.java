package io.github.shshdxk.liquibase.sqlgenerator;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.exception.Warnings;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.statement.SqlStatement;

import java.util.Iterator;
import java.util.SortedSet;

import static io.github.shshdxk.liquibase.sqlgenerator.SqlGenerator.EMPTY_SQL;

public class SqlGeneratorChain<T extends SqlStatement> {
    private Iterator<SqlGenerator<T>> sqlGenerators;

    public SqlGeneratorChain(SortedSet<SqlGenerator<T>> sqlGenerators) {
        if (sqlGenerators != null) {
            this.sqlGenerators = sqlGenerators.iterator();
        }
    }

    public Sql[] generateSql(T statement, Database database) {
        if (sqlGenerators == null) {
            return null;
        }

        if (!sqlGenerators.hasNext()) {
            return EMPTY_SQL;
        }

        return sqlGenerators.next().generateSql(statement, database, this);
    }

    public Warnings warn(T statement, Database database) {
        if ((sqlGenerators == null) || !sqlGenerators.hasNext()) {
            return new Warnings();
        }

        return sqlGenerators.next().warn(statement, database, this);
    }

    public ValidationErrors validate(T statement, Database database) {
        if ((sqlGenerators == null) || !sqlGenerators.hasNext()) {
            return new ValidationErrors();
        }

        return sqlGenerators.next().validate(statement, database, this);
    }
}
