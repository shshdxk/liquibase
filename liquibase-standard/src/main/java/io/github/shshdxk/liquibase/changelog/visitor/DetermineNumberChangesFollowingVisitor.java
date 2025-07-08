package io.github.shshdxk.liquibase.changelog.visitor;

import io.github.shshdxk.liquibase.changelog.ChangeSet;
import io.github.shshdxk.liquibase.changelog.DatabaseChangeLog;
import io.github.shshdxk.liquibase.changelog.filter.ChangeSetFilterResult;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.exception.LiquibaseException;

import java.util.Set;

public class DetermineNumberChangesFollowingVisitor implements ChangeSetVisitor {
  private String id;
  private String author;
  private String filePath;
  private int changeSetPosition;
  private int totalChangeSets;

  public DetermineNumberChangesFollowingVisitor(String id, String author, String filePath) {
    this.id = id;
    this.author = author;
    this.filePath = filePath;
  }

  public int getNumChangeSetsFollowing() {
    return totalChangeSets - changeSetPosition;
  }
  @Override
  public Direction getDirection() {
    return Direction.FORWARD;
  }

  @Override
  public void visit(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, Set<ChangeSetFilterResult> filterResults) throws LiquibaseException {
    totalChangeSets++;
    if (changeSet.getId().equalsIgnoreCase(this.id) &&
            changeSet.getAuthor().equalsIgnoreCase(this.author) &&
            changeSet.getFilePath().equals(this.filePath)) {
      changeSetPosition = totalChangeSets;
    }
  }
}
