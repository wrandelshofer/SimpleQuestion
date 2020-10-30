/*
 * @(#)ZipOut.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.zip;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

/**
 * ZipOut.
 *
 * @author Werner Randelshofer
 * @version 1.0  11 January 2005  Created.
 */
public interface ZipOut extends Closeable, Flushable {
    /**
     * Begins writing a new ZIP file entry and positions the stream to the
     * start of the entry data. Closes the current entry if still active.
     * The default compression method will be used if no compression method
     * was specified for the entry, and the current time will be used if
     * the entry has no set modification time.
     *
     * @param e the ZIP entry to be written
     * @throws ZipException if a ZIP format error has occurred
     * @throws IOException  if an I/O error has occurred
     */
    public void putNextEntry(ZipEntry zipEntry) throws IOException;

    /**
     * Closes the current ZIP entry and positions the stream for writing
     * the next entry.
     *
     * @throws ZipException if a ZIP format error has occurred
     * @throws IOException  if an I/O error has occurred
     */
    public void closeEntry() throws IOException;

    public abstract void write(int b) throws IOException;

    /**
     * Writes <code>b.length</code> bytes from the specified byte array
     * to this output stream. The general contract for <code>write(b)</code>
     * is that it should have exactly the same effect as the call
     * <code>write(b, 0, b.length)</code>.
     *
     * @param b the data.
     * @throws IOException if an I/O error occurs.
     * @see java.io.OutputStream#write(byte[], int, int)
     */
    public void write(byte b[]) throws IOException;

    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to this output stream.
     * The general contract for <code>write(b, off, len)</code> is that
     * some of the bytes in the array <code>b</code> are written to the
     * output stream in order; element <code>b[off]</code> is the first
     * byte written and <code>b[off+len-1]</code> is the last byte written
     * by this operation.
     * <p>
     * The <code>write</code> method of <code>OutputStream</code> calls
     * the write method of one argument on each of the bytes to be
     * written out. Subclasses are encouraged to override this method and
     * provide a more efficient implementation.
     * <p>
     * If <code>b</code> is <code>null</code>, a
     * <code>NullPointerException</code> is thrown.
     * <p>
     * If <code>off</code> is negative, or <code>len</code> is negative, or
     * <code>off+len</code> is greater than the length of the array
     * <code>b</code>, then an <tt>IndexOutOfBoundsException</tt> is thrown.
     *
     * @param b   the data.
     * @param off the start offset in the data.
     * @param len the number of bytes to write.
     * @throws IOException if an I/O error occurs. In particular,
     *                     an <code>IOException</code> is thrown if the output
     *                     stream is closed.
     */
    public void write(byte b[], int off, int len) throws IOException;

    /**
     * Returns the underlying output stream.
     */
    public OutputStream getOutputStream() throws IOException;
}

