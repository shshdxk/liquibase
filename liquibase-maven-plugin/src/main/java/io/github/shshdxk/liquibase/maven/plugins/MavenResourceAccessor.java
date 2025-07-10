package io.github.shshdxk.liquibase.maven.plugins;

import io.github.shshdxk.liquibase.resource.ClassLoaderResourceAccessor;

/**
 * Extension of {@link ClassLoaderResourceAccessor} for Maven which will use a default or user specified {@link ClassLoader} to load files/resources.
 */
public class MavenResourceAccessor extends ClassLoaderResourceAccessor {

    public MavenResourceAccessor(ClassLoader classLoader) {
        super(classLoader);
    }

}
