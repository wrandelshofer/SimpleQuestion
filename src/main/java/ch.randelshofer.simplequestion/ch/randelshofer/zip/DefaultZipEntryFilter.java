/*
 * @(#)DefaultZipEntryFilter.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.zip;

import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;

/**
 * DefaultZipEntryFilter.
 *
 * @author Werner Randelshofer
 * @version 1.0 13. Januar 2004  Created.
 */
public class DefaultZipEntryFilter implements ZipEntryFilter {
    private Set<String> filters;
    private boolean accept;

    /**
     * Creates a new instance which accepts or rejects zip entries specified
     * in the filter set.
     *
     * @param filters This set must contain zip entry names (Strings).
     *                The names must follow the naming conventions for zip entries. That is
     *                slash characters '/' as separators and a trailing slash for directories.
     * @param accept  If this parameter is true, only entries which are in the
     *                filters set are accepted. If this parameter is false, only entries which
     *                are not in the filters set are accepted.
     */
    public DefaultZipEntryFilter(Set<String> filters, boolean accept) {
        this.filters = filters;
        this.accept = accept;
    }

    /**
     * Creates a new zip entry filter which accepts all entries.
     */
    public DefaultZipEntryFilter() {
        this.filters = new HashSet<>(0);
        this.accept = false;
    }


    public boolean accept(ZipEntry entry) {
        return filters.contains(entry.getName()) == accept;
    }
}
