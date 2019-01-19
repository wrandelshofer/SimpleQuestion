/* @(#)ZipEntryFilter.java
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

import java.util.zip.*;
/**
 * ZipEntryFilter.
 *
 * @author  Werner Randelshofer
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
