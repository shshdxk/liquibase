package io.github.shshdxk.liquibase.servicelocator;

import io.github.shshdxk.liquibase.Scope;
import io.github.shshdxk.liquibase.exception.ServiceNotFoundException;
import io.github.shshdxk.liquibase.plugin.Plugin;

import java.util.List;

/**
 * Abstraction for finding and creating instances of classes.
 * {@link StandardServiceLocator} is the main implementation, but can be overridden if need be.
 * <p>
 * The ServiceLocator to use should be accessed via {@link Scope#getServiceLocator()}
 */
public interface ServiceLocator extends Plugin {

    int getPriority();

    <T> List<T> findInstances(Class<T> interfaceType) throws ServiceNotFoundException;

}
