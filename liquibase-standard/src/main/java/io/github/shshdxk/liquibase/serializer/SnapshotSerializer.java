package io.github.shshdxk.liquibase.serializer;

import io.github.shshdxk.liquibase.servicelocator.PrioritizedService;
import io.github.shshdxk.liquibase.snapshot.DatabaseSnapshot;

import java.io.IOException;
import java.io.OutputStream;

public interface SnapshotSerializer extends PrioritizedService {

    String[] getValidFileExtensions();

    String serialize(LiquibaseSerializable object, boolean pretty);

    void write(DatabaseSnapshot snapshot, OutputStream out) throws IOException;
}
