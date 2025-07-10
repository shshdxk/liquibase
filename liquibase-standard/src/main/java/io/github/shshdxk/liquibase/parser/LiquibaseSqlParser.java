package io.github.shshdxk.liquibase.parser;

import io.github.shshdxk.liquibase.changelog.ChangeSet;
import io.github.shshdxk.liquibase.plugin.Plugin;
import io.github.shshdxk.liquibase.util.StringClauses;

public interface LiquibaseSqlParser extends Plugin {

    StringClauses parse(String sqlBlock);

    StringClauses parse(String sqlBlock, boolean preserveWhitespace, boolean preserveComments);

    /**
     * @param changeSet the changeset associated with the sql being parsed. If not null, the changeset identifying
     *                  information should be included in any exceptions thrown if the sql cannot be parsed.
     */
    default StringClauses parse(String sqlBlock, boolean preserveWhitespace, boolean preserveComments, ChangeSet changeSet) {
        return parse(sqlBlock, preserveWhitespace, preserveComments);
    }

    int getPriority();
}
