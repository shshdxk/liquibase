package io.github.shshdxk.liquibase.changelog;

import io.github.shshdxk.liquibase.Contexts;
import io.github.shshdxk.liquibase.LabelExpression;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.exception.DatabaseException;
import io.github.shshdxk.liquibase.exception.DatabaseHistoryException;
import io.github.shshdxk.liquibase.exception.LiquibaseException;
import io.github.shshdxk.liquibase.servicelocator.PrioritizedService;

import java.util.Date;
import java.util.List;

public interface ChangeLogHistoryService extends PrioritizedService {

    boolean supports(Database database);

    void setDatabase(Database database);

    void reset();

    /**
     * Ensures the change log history container is correctly initialized for use. This method may be called multiple times so it should check state as needed.
     */
    void init() throws DatabaseException;

    /**
     * Upgrades any existing checksums with an out of date version
     */
    void upgradeChecksums(final DatabaseChangeLog databaseChangeLog, final Contexts contexts, LabelExpression labels) throws DatabaseException;

    List<RanChangeSet> getRanChangeSets() throws DatabaseException;

    RanChangeSet getRanChangeSet(ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException;

    ChangeSet.RunStatus getRunStatus(ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException;

    /**
     * Returns the date the given changeSet was ran. Returns null if changeSet was not null.
     */
    Date getRanDate(ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException;

    void setExecType(ChangeSet changeSet, ChangeSet.ExecType execType) throws DatabaseException;

    void removeFromHistory(ChangeSet changeSet) throws DatabaseException;

    int getNextSequenceValue() throws LiquibaseException;

    void tag(String tagString) throws DatabaseException;

    boolean tagExists(String tag) throws DatabaseException;

    void clearAllCheckSums() throws LiquibaseException;

    void destroy() throws DatabaseException;

    String getDeploymentId();

    void resetDeploymentId();

    void generateDeploymentId();

    }
