package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.change.ColumnConfig;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorFactory;
import io.github.shshdxk.liquibase.statement.core.GetNextChangeSetSequenceValueStatement;
import io.github.shshdxk.liquibase.statement.core.SelectFromDatabaseChangeLogStatement;

public class GetNextChangeSetSequenceValueGenerator extends AbstractSqlGenerator<GetNextChangeSetSequenceValueStatement> {

    @Override
    public ValidationErrors validate(GetNextChangeSetSequenceValueStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    @Override
    public Sql[] generateSql(GetNextChangeSetSequenceValueStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
			String quotedColumnName = database.escapeColumnName(null, null, null, "ORDEREXECUTED");
			return SqlGeneratorFactory.getInstance()
					.generateSql(new SelectFromDatabaseChangeLogStatement(new ColumnConfig().setName("MAX("+quotedColumnName+")", true)), database);
    }
}
