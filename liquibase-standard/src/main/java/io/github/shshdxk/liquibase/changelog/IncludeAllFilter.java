package io.github.shshdxk.liquibase.changelog;

public interface IncludeAllFilter {
    boolean include(String changeLogPath);
}
