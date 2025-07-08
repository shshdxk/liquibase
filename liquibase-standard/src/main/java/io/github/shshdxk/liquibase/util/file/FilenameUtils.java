package io.github.shshdxk.liquibase.util.file;

import io.github.shshdxk.liquibase.util.FilenameUtil;

/**
 * @deprecated use {@link FilenameUtil}
 */
public class FilenameUtils extends FilenameUtil {

    /**
     * @deprecated use {@link #getDirectory(String)}
     */
    public static String getFullPath(String filename) {
        return getDirectory(filename);
    }
}
