package io.github.shshdxk.liquibase.change.visitor;

import io.github.shshdxk.liquibase.changelog.ChangeLogChild;

import java.util.Set;

public interface ChangeVisitor extends ChangeLogChild {

    String getName();

    String getChange();

    Set<String> getDbms();

}
