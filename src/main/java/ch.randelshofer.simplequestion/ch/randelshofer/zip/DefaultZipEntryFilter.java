/* @(#)DefaultZipEntryFilter.java
 *
 * Copyright (c) 2004 Werner Randelshofer
 * Staldenmattweg 2, Immensee, CH-6405, Switzerland.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Werner Randelshofer. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Werner Randelshofer.
 */

package ch.randelshofer.zip;

import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;

/**
 * DefaultZipEntryFilter.
 *
 * @author  Werner Randelshofer
 * @version 1.0 13. Januar 2004  Created.
 */
public class DefaultZipEntryFilter implements ZipEntryFilter {
    private Set<String> filters;
    private boolean accept;
    
    /** Creates a new instance which accepts or rejects zip entries specified
     * in the filter set.
     * 
     * @param filters This set must contain zip entry names (Strings).
     * The names must follow the naming conventions for zip entries. That is
     * slash characters '/' as separators and a trailing slash for directories.
     * @param accept If this parameter is true, only entries which are in the
     * filters set are accepted. If this parameter is false, only entries which
     * are not in the filters set are accepted.
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
