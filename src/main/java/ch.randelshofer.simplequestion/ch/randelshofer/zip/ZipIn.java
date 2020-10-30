/*
 * @(#)ZipIn.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */


package ch.randelshofer.zip;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;

/**
 * ZipIn defines the common interface of {@link ZipInStream} and
 * {@link ZipInDirectory}.
 *
 * @author Werner Randelshofer
 * @version 1.0  2008-12-03 Created.
 */
public interface ZipIn {

    /**
     * Reads the next ZIP file entry and positions the stream at the
     * beginning of the entry data.
     *
     * @return the next ZIP file entry, or null if there are no more entries
     * @throws IOException  if an I/O error has occurred
     */
    ZipEntry getNextEntry() throws IOException;

    /**
     * Closes the current ZIP entry and positions the stream for reading the
     * next entry.
     *
     * @throws IOException  if an I/O error has occurred
     */
    void closeEntry() throws IOException;

    /**
     * Closes this input stream and releases any system resources associated
     * with the stream.
     *
     * @throws IOException if an I/O error has occurred
     */
    void close() throws IOException;

    /**
     * Returns the input stream for reading the current ZipEntry.
     *
     * @return An InputStream.
     * @throws IOException if an I/O error has occurred
     */
    InputStream getInputStream() throws IOException;
}
