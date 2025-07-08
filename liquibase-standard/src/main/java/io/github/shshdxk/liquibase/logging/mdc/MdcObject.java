package io.github.shshdxk.liquibase.logging.mdc;

import io.github.shshdxk.liquibase.Scope;

import java.io.Closeable;

public class MdcObject implements Closeable {

    private final String key;
    private final Object value;

    public MdcObject(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public void close() {
        Scope.getCurrentScope().getMdcManager().remove(key);
    }
}
