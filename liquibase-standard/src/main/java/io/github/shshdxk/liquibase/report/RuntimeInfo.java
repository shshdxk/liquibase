package io.github.shshdxk.liquibase.report;

import io.github.shshdxk.liquibase.util.LiquibaseUtil;
import io.github.shshdxk.liquibase.util.NetUtil;
import io.github.shshdxk.liquibase.util.SystemUtil;
import lombok.Data;
import org.apache.commons.lang3.SystemProperties;

@Data
public class RuntimeInfo {
    private final String systemUsername = SystemProperties.getUserName();
    private final String hostname = NetUtil.getLocalHostName();
    private final String os = System.getProperty("os.name");
    private String interfaceType;
    private String startTime;
    private String updateDuration;
    private final String liquibaseVersion = LiquibaseUtil.getBuildVersionInfo();
    private String javaVersion = SystemUtil.getJavaVersion();
}
