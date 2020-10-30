/*
 * @(#)ZipEntryFilter.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */


package ch.randelshofer.zip;

import java.util.zip.ZipEntry;

/**
 * ZipEntryFilter.
 *
 * @author Werner Randelshofer
 * @version 1.0 13. Januar 2004  Created.
 */
public interface ZipEntryFilter {
    /**
     * Tests if a specified file should be included in a file list.
     *
     * @param entry the entry.
     * @return true if and only if the entry should be included in the
     * file list; false otherwise.
     */
    public boolean accept(ZipEntry entry);

}
