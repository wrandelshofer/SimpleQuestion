/* @(#)ZipOutStream.java
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

import java.io.OutputStream;
import java.util.zip.ZipOutputStream;

/**
 * This is a ZipOutputStream which implemements the ZipOut interface to make
 * this stream interchangeable with ZipOutputFile objects.
 *
 * @author  Werner Randelshofer
 * @version 1.0  11 January 2005  Created.
 */
public class ZipOutStream extends ZipOutputStream implements ZipOut {
    
    /**
     * Creates a new ZIP output stream.
     * @param out the actual output stream
     */
    public ZipOutStream(OutputStream out) {
        super(out);
    }

    public OutputStream getOutputStream() {
        return this;
    }
    
}
