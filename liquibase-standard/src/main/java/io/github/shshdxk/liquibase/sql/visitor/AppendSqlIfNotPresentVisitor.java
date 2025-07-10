package io.github.shshdxk.liquibase.sql.visitor;

import io.github.shshdxk.liquibase.database.Database;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
public class AppendSqlIfNotPresentVisitor extends AppendSqlVisitor {
    @Override
    public String modifySql(String sql, Database database) {
        String returnValue = sql;
        String appendValue = getValue();
        if (! returnValue.endsWith(appendValue)) {
            returnValue += appendValue;
        }
        return returnValue;
    }

}
