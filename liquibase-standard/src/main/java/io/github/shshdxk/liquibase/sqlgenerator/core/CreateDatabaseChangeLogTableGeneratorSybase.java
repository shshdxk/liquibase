package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.ObjectQuotingStrategy;
import io.github.shshdxk.liquibase.database.core.SybaseDatabase;
import io.github.shshdxk.liquibase.datatype.DataTypeFactory;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sql.UnparsedSql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.statement.core.CreateDatabaseChangeLogTableStatement;
import io.github.shshdxk.liquibase.structure.core.Relation;
import io.github.shshdxk.liquibase.structure.core.Table;

public class CreateDatabaseChangeLogTableGeneratorSybase extends AbstractSqlGenerator<CreateDatabaseChangeLogTableStatement> {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(CreateDatabaseChangeLogTableStatement statement, Database database) {
        return database instanceof SybaseDatabase;
    }

    @Override
    public ValidationErrors validate(CreateDatabaseChangeLogTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    @Override
    public Sql[] generateSql(CreateDatabaseChangeLogTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ObjectQuotingStrategy currentStrategy = database.getObjectQuotingStrategy();
        database.setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY);
        try {
            return new Sql[]{
                    new UnparsedSql("CREATE TABLE " + database.escapeTableName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName()) + " (ID VARCHAR(150) NOT NULL, " +
                            "AUTHOR VARCHAR(150) NOT NULL, " +
                            "FILENAME VARCHAR(255) NOT NULL, " +
                            "DATEEXECUTED " + DataTypeFactory.getInstance().fromDescription("datetime", database).toDatabaseDataType(database) + " NOT NULL, " +
                            "ORDEREXECUTED INT NOT NULL, " +
                            "EXECTYPE VARCHAR(10) NOT NULL, " +
                            "MD5SUM VARCHAR(35) NULL, " +
                            "DESCRIPTION VARCHAR(255) NULL, " +
                            "COMMENTS VARCHAR(255) NULL, " +
                            "TAG VARCHAR(255) NULL, " +
                            "LIQUIBASE VARCHAR(20) NULL, " +
                            "CONTEXTS VARCHAR(255) NULL, " +
                            "LABELS VARCHAR(255) NULL, " +
                            "DEPLOYMENT_ID VARCHAR(10) NULL, " +
                            "PRIMARY KEY(ID, AUTHOR, FILENAME))",
                            getAffectedTable(database))
            };
        } finally {
            database.setObjectQuotingStrategy(currentStrategy);
        }
    }

    protected Relation getAffectedTable(Database database) {
        return new Table().setName(database.getDatabaseChangeLogTableName()).setSchema(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName());
    }
}
