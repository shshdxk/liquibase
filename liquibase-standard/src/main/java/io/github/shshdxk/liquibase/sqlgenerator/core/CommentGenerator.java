package io.github.shshdxk.liquibase.sqlgenerator.core;

import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.exception.ValidationErrors;
import io.github.shshdxk.liquibase.sql.SingleLineComment;
import io.github.shshdxk.liquibase.sql.Sql;
import io.github.shshdxk.liquibase.sqlgenerator.SqlGeneratorChain;
import io.github.shshdxk.liquibase.statement.core.CommentStatement;

public class CommentGenerator extends AbstractSqlGenerator<CommentStatement> {

    @Override
    public Sql[] generateSql(CommentStatement comment, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[] {
                new SingleLineComment(comment.getText(), database.getLineComment())     
        };
    }

    @Override
    public ValidationErrors validate(CommentStatement comment,
                                     Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("text", comment.getText());
        return validationErrors;
    }

}
