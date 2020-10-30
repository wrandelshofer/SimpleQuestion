/*
 * @(#)ZipOutStream.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.zip;

import java.io.OutputStream;
import java.util.zip.ZipOutputStream;

/**
 * This is a ZipOutputStream which implemements the ZipOut interface to make
 * this stream interchangeable with ZipOutputFile objects.
 *
 * @author Werner Randelshofer
 * @version 1.0  11 January 2005  Created.
 */
public class ZipOutStream extends ZipOutputStream implements ZipOut {

    /**
     * Creates a new ZIP output stream.
     *
     * @param out the actual output stream
     */
    public ZipOutStream(OutputStream out) {
        super(out);
    }

    public OutputStream getOutputStream() {
        return this;
    }

}
