package io.github.shshdxk.liquibase.database;

import io.github.shshdxk.liquibase.Scope;
import io.github.shshdxk.liquibase.SingletonObject;
import io.github.shshdxk.liquibase.exception.DatabaseException;
import io.github.shshdxk.liquibase.servicelocator.ServiceLocator;

import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class LiquibaseTableNamesFactory implements SingletonObject {

    private final SortedSet<LiquibaseTableNames> generators;

    private LiquibaseTableNamesFactory() {
        ServiceLocator serviceLocator = Scope.getCurrentScope().getServiceLocator();
        generators = new TreeSet<>(Comparator.comparingInt(LiquibaseTableNames::getOrder));
        generators.addAll(serviceLocator.findInstances(LiquibaseTableNames.class));
    }

    public List<String> getLiquibaseTableNames(Database database) {
        return generators.stream().flatMap(f -> f.getLiquibaseGeneratedTableNames(database).stream()).collect(Collectors.toList());
    }

    public void destroy(Database abstractJdbcDatabase) throws DatabaseException {
        for (LiquibaseTableNames generator : generators) {
            generator.destroy(abstractJdbcDatabase);
            abstractJdbcDatabase.commit();
        }
    }
}
