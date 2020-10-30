/*
 * @(#)ZipInStream.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.zip;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

/**
 * Implements the ZipIn interface for ZipInputStream.
 *
 * @author wrandels
 */
public class ZipInStream extends ZipInputStream implements ZipIn {
    /**
     * Creates a new ZIP input stream.
     *
     * @param in the actual input stream
     */
    public ZipInStream(InputStream in) {
        super(in);
    }

    public InputStream getInputStream() throws IOException {
        return this;
    }
}
