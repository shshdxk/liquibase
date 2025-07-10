package io.github.shshdxk.liquibase.license;

import io.github.shshdxk.liquibase.plugin.Plugin;

public interface LicenseTrackingListener extends Plugin {

    int getPriority();
    void handleEvent(LicenseTrackList event) throws Exception;
}
