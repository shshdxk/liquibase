package io.github.shshdxk.liquibase.exception;

import io.github.shshdxk.liquibase.util.LiquibaseUtil;

public class UnsupportedChecksumVersionException extends RuntimeException {

    private static final long serialVersionUID = -229400973681987065L;

    public UnsupportedChecksumVersionException(int i) {
        super(String.format("Liquibase detected an unknown checksum calculation version %s. This can be caused by a previous " +
                        "operation on this changelog with a newer version than your current %S release. ",
                 i, LiquibaseUtil.getBuildVersionInfo()));
    }
}
