package io.github.shshdxk.liquibase.change.custom;

import io.github.shshdxk.liquibase.change.AbstractChange;
import io.github.shshdxk.liquibase.change.CheckSum;

/**
 * Interface to implement that allows a custom change to generate its own checksum.
 *
 * @see CustomChange
 * @see AbstractChange#generateCheckSum()
 */
public interface CustomChangeChecksum {

    /**
     * Generates a checksum for the current state of the change.
     *
     * @return the generated checksum
     */
    CheckSum generateChecksum();

}
