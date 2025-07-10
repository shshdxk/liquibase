package io.github.shshdxk.liquibase.changelog;

import io.github.shshdxk.liquibase.ContextExpression;
import io.github.shshdxk.liquibase.serializer.AbstractLiquibaseSerializable;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
public class ChangeLogInclude extends AbstractLiquibaseSerializable implements ChangeLogChild {
    private String file;
    private Boolean relativeToChangelogFile;
    private Boolean errorIfMissing;
    private ContextExpression context;

    @Override
    public Set<String> getSerializableFields() {
        return new LinkedHashSet<>(Arrays.asList("file", "relativeToChangelogFile", "errorIfMissing", "context"));
    }

    @Override
    public String getSerializedObjectName() {
        return "include";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

}
