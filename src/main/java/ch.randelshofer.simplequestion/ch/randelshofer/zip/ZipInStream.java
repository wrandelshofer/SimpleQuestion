/*
 * @(#)ZipInStream.java  1.0  2008-12-03
 *
 * Copyright (c) 2008 Werner Randelshofer
 * Staldenmattweg 2, Immensee, CH-6405, Switzerland.
 * All rights reserved.
 *
 * The copyright of this software is owned by Werner Randelshofer.
 * You may not use, copy or modify this software, except in
 * accordance with the license agreement you entered into with
 * Werner Randelshofer. For details see accompanying license terms.
 */

package ch.randelshofer.zip;

import java.io.*;
import java.util.zip.ZipInputStream;

/**
 * Implements the ZipIn interface for ZipInputStream.
 *
 * @author wrandels
 */
public class ZipInStream extends ZipInputStream implements ZipIn {
    /**
     * Creates a new ZIP input stream.
     * @param in the actual input stream
     */
    public ZipInStream(InputStream in) {
        super(in);
    }

    public InputStream getInputStream() throws IOException {
        return this;
    }
}
