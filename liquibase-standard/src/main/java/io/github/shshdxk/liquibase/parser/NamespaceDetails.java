package io.github.shshdxk.liquibase.parser;

import io.github.shshdxk.liquibase.serializer.LiquibaseSerializer;
import io.github.shshdxk.liquibase.servicelocator.PrioritizedService;

public interface NamespaceDetails extends PrioritizedService{

    int PRIORITY_EXTENSION = PRIORITY_DATABASE;

    boolean supports(LiquibaseSerializer serializer, String namespaceOrUrl);

    boolean supports(LiquibaseParser parser, String namespaceOrUrl);

    String getShortName(String namespaceOrUrl);

    String getSchemaUrl(String namespaceOrUrl);

    String[] getNamespaces();
}
