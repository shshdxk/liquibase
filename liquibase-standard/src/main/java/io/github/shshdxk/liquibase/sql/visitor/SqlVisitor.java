package io.github.shshdxk.liquibase.sql.visitor;

import io.github.shshdxk.liquibase.ContextExpression;
import io.github.shshdxk.liquibase.Labels;
import io.github.shshdxk.liquibase.change.CheckSum;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.serializer.LiquibaseSerializable;

import java.util.Set;

public interface SqlVisitor extends LiquibaseSerializable {

    String modifySql(String sql, Database database);

    String getName();

    Set<String> getApplicableDbms();

    void setApplicableDbms(Set<String> modifySqlDbmsList);

    void setApplyToRollback(boolean applyOnRollback);

    boolean isApplyToRollback();

    ContextExpression getContextFilter();

    void setContextFilter(ContextExpression contextFilter);

    Labels getLabels();
    void setLabels(Labels labels);

    CheckSum generateCheckSum();

}
