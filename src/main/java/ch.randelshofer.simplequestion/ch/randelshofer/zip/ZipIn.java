/* @(#)ZipIn.java
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

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;

/**
 * ZipIn defines the common interface of {@link ZipInStream} and
 * {@link ZipInDirectory}.
 *
 * @author  Werner Randelshofer
 * @version 1.0  2008-12-03 Created.
 */
public interface ZipIn {

    /**
     * Reads the next ZIP file entry and positions the stream at the
     * beginning of the entry data.
     * @return the next ZIP file entry, or null if there are no more entries
     * @exception ZipException if a ZIP file error has occurred
     * @exception IOException if an I/O error has occurred
     */
    public ZipEntry getNextEntry() throws IOException;
    /**
     * Closes the current ZIP entry and positions the stream for reading the
     * next entry.
     * @exception ZipException if a ZIP file error has occurred
     * @exception IOException if an I/O error has occurred
     */
    public void closeEntry() throws IOException;
    /**
     * Closes this input stream and releases any system resources associated
     * with the stream.
     * @exception IOException if an I/O error has occurred
     */
    public void close() throws IOException;

    /**
     * Returns the input stream for reading the current ZipEntry.
     * @exception IOException if an I/O error has occurred
     * @return An InputStream.
     */
    public InputStream getInputStream() throws IOException;
}
