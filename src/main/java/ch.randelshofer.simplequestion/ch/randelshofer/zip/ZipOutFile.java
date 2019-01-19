/* @(#)ZipOutFile.java
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.zip.CRC32;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;


/**
 * Instances of this class can be used for writing files in the ZIP file format.
 * Includes support for both compressed and uncompressed entries.
 * This class stores the size of a ZipEntry in the header which precedes the
 * data of the entry (as opposed to writing it into the 'Ext' header, which
 * is stored after the data of the entry). Such a zip archive is better suited
 * for streaming over a network, because readers how much data to expect, before
 * they actually have to read it in.
 * <p>
 * Implementation note: Most code of this class has been derived from
 * java.util.zip.ZipOutputStream.
 * <p>
 * FIXME - Promised Functionality not implemented yet.
 *
 * @author Werner Randelshofer
 * @version 1.0  11 January 2005  Created.
 */
public class ZipOutFile extends OutputStream implements ZipOut {
    private RandomAccessFile out;

    private ZipEntryExt entry;
    /**
     * item instanceof ZipEntryExt
     */
    private Vector entries = new Vector();
    /**
     * key instanceof String
     * value instanceof ZipEntryExt
     */
    private Hashtable names = new Hashtable();
    private CRC32 crc = new CRC32();
    private long written;
    private long locoff = 0;
    private String comment;
    private int method = DEFLATED;
    private boolean finished;

    private boolean closed = false;

    /*
     * Header signatures
     */
    final static long LOCSIG = 0x04034b50;    // "PK\003\004"
    final static long EXTSIG = 0x08074b50L;    // "PK\007\008"
    static long CENSIG = 0x02014b50L;    // "PK\001\002"
    static long ENDSIG = 0x06054b50L;    // "PK\005\006"

    /**
     * This is used to set the compression level on a deflater output stream,
     * even though ZipOutFile is not a subclass of it.
     */
    private static class MyDeflaterOutputStream extends DeflaterOutputStream {
        public MyDeflaterOutputStream(OutputStream out) {
            super(out);
        }

        public void setLevel(int level) {
            this.def.setLevel(level);
        }

        public boolean finished() {
            return this.def.finished();
        }

        public void deflate() throws IOException {
            super.deflate();
        }

        public int getTotalIn() {
            return this.def.getTotalIn();
        }

        public int getTotalOut() {
            return this.def.getTotalOut();
        }

        public void reset() {
            this.def.reset();
        }
    }

    private MyDeflaterOutputStream def;

    /**
     * This is used to store additional data along with a ZipEntry in the
     * entries vector instance variable.
     */
    private static class ZipEntryExt {
        public ZipEntry entry;
        public int flag;
        public int version;
        public long offset;
    }


    /**
     * Creates a new instance.
     */
    public ZipOutFile(File f) throws FileNotFoundException {
        out = new RandomAccessFile(f, "rw");
        def = new MyDeflaterOutputStream(this);
    }


    /**
     * Check to make sure that this stream has not been closed
     */
    private void ensureOpen() throws IOException {
        if (closed) {
            throw new IOException("Stream closed");
        }
    }

    /**
     * Compression method for uncompressed (STORED) entries.
     */
    public static final int STORED = ZipEntry.STORED;

    /**
     * Compression method for compressed (DEFLATED) entries.
     */
    public static final int DEFLATED = ZipEntry.DEFLATED;

    /**
     * Sets the ZIP file comment.
     *
     * @param comment the comment string
     * @throws IllegalArgumentException if the length of the specified
     *                                  ZIP file comment is greater than 0xFFFF bytes
     */
    public void setComment(String comment) {
        if (comment != null && comment.length() > 0xffff / 3
                && getUTF8Length(comment) > 0xffff) {
            throw new IllegalArgumentException("ZIP file comment too long.");
        }
        this.comment = comment;
    }

    /**
     * Sets the default compression method for subsequent entries. This
     * default will be used whenever the compression method is not specified
     * for an individual ZIP file entry, and is initially set to DEFLATED.
     *
     * @param method the default compression method
     * @throws IllegalArgumentException if the specified compression method
     *                                  is invalid
     */
    public void setMethod(int method) {
        if (method != DEFLATED && method != STORED) {
            throw new IllegalArgumentException("invalid compression method");
        }
        this.method = method;
    }

    /**
     * Sets the compression level for subsequent entries which are DEFLATED.
     * The default setting is DEFAULT_COMPRESSION.
     *
     * @param level the compression level (0-9)
     * @throws IllegalArgumentException if the compression level is invalid
     */
    public void setLevel(int level) {
        def.setLevel(level);
    }

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
    public void putNextEntry(ZipEntry e) throws IOException {
        ensureOpen();
        if (entry != null) {
            closeEntry();    // close previous entry
        }
        ZipEntryExt eext = new ZipEntryExt();
        eext.entry = e;
        if (e.getTime() == -1) {
            e.setTime(System.currentTimeMillis());
        }
        if (e.getMethod() == -1) {
            e.setMethod(method);    // use default method
        }
        switch (e.getMethod()) {
            case DEFLATED:
                if (e.getSize() == -1 || e.getCompressedSize() == -1 || e.getCrc() == -1) {
                    // store size, compressed size, and crc-32 in data descriptor
                    // immediately following the compressed entry data
                    eext.flag = 8;
                } else if (e.getSize() != -1 && e.getCompressedSize() != -1 && e.getCrc() != -1) {
                    // store size, compressed size, and crc-32 in LOC header
                    eext.flag = 0;
                } else {
                    throw new ZipException(
                            "DEFLATED entry missing size, compressed size, or crc-32");
                }
                eext.version = 20;
                break;
            case STORED:
                // compressed size, uncompressed size, and crc-32 must all be
                // set for entries using STORED compression method
                if (e.getSize() == -1) {
                    e.setSize(e.getCompressedSize());
                } else if (e.getCompressedSize() == -1) {
                    e.setCompressedSize(e.getSize());
                } else if (e.getSize() != e.getCompressedSize()) {
                    throw new ZipException(
                            "STORED entry where compressed != uncompressed size");
                }
                if (e.getSize() == -1 || e.getCrc() == -1) {
                    throw new ZipException(
                            "STORED entry missing size, compressed size, or crc-32");
                }
                eext.version = 10;
                eext.flag = 0;
                break;
            default:
                throw new ZipException("unsupported compression method");
        }
        eext.offset = written;
        if (names.put(e.getName(), eext) != null) {
            throw new ZipException("duplicate entry: " + e.getName());
        }
        writeLOC(eext);
        entries.addElement(eext);
        entry = eext;
    }

    /**
     * Closes the current ZIP entry and positions the stream for writing
     * the next entry.
     *
     * @throws ZipException if a ZIP format error has occurred
     * @throws IOException  if an I/O error has occurred
     */
    public void closeEntry() throws IOException {
        ensureOpen();
        ZipEntryExt eext = entry;
        if (eext != null) {
            ZipEntry e = eext.entry;
            switch (e.getMethod()) {
                case DEFLATED:
                    def.finish();
                    while (!def.finished()) {
                        def.deflate();
                    }
                    if ((eext.flag & 8) == 0) {
                        // verify size, compressed size, and crc-32 settings
                        if (e.getSize() != def.getTotalIn()) {
                            throw new ZipException(
                                    "invalid entry size (expected " + e.getSize() +
                                            " but got " + def.getTotalIn() + " bytes)");
                        }
                        if (e.getCompressedSize() != def.getTotalOut()) {
                            throw new ZipException(
                                    "invalid entry compressed size (expected " +
                                            e.getCompressedSize() + " but got " + def.getTotalOut() +
                                            " bytes)");
                        }
                        if (e.getCrc() != crc.getValue()) {
                            throw new ZipException(
                                    "invalid entry CRC-32 (expected 0x" +
                                            Long.toHexString(e.getCrc()) + " but got 0x" +
                                            Long.toHexString(crc.getValue()) + ")");
                        }
                    } else {
                        e.setSize(def.getTotalIn());
                        e.setCompressedSize(def.getTotalOut());
                        e.setCrc(crc.getValue());
                        writeEXT(e);
                    }
                    def.reset();
                    written += e.getCompressedSize();
                    break;
                case STORED:
                    // we already know that both e.size and e.csize are the same
                    if (e.getSize() != written - locoff) {
                        throw new ZipException(
                                "invalid entry size (expected " + e.getSize() +
                                        " but got " + (written - locoff) + " bytes)");
                    }
                    if (e.getCrc() != crc.getValue()) {
                        throw new ZipException(
                                "invalid entry crc-32 (expected 0x" +
                                        Long.toHexString(e.getCrc()) + " but got 0x" +
                                        Long.toHexString(crc.getValue()) + ")");
                    }
                    break;
                default:
                    throw new InternalError("invalid compression method");
            }
            crc.reset();
            entry = null;
        }
    }

    /**
     * Writes an array of bytes to the current ZIP entry data. This method
     * will block until all the bytes are written.
     *
     * @param b   the data to be written
     * @param off the start offset in the data
     * @param len the number of bytes that are written
     * @throws ZipException if a ZIP file error has occurred
     * @throws IOException  if an I/O error has occurred
     */
    public synchronized void write(byte[] b, int off, int len)
            throws IOException {
        ensureOpen();
        if (off < 0 || len < 0 || off > b.length - len) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }

        if (entry == null) {
            throw new ZipException("no current ZIP entry");
        }
        ZipEntry e = entry.entry;
        switch (e.getMethod()) {
            case DEFLATED:
                super.write(b, off, len);
                break;
            case STORED:
                written += len;
                if (written - locoff > e.getSize()) {
                    throw new ZipException(
                            "attempt to write past end of STORED entry");
                }
                out.write(b, off, len);
                break;
            default:
                throw new InternalError("invalid compression method");
        }
        crc.update(b, off, len);
    }

    /**
     * Finishes writing the contents of the ZIP output stream without closing
     * the underlying stream. Use this method when applying multiple filters
     * in succession to the same output stream.
     *
     * @throws ZipException if a ZIP file error has occurred
     * @throws IOException  if an I/O exception has occurred
     */
    public void finish() throws IOException {
        ensureOpen();
        if (finished) {
            return;
        }
        if (entry != null) {
            closeEntry();
        }
        if (entries.size() < 1) {
            throw new ZipException("ZIP file must have at least one entry");
        }
        // write central directory
        long off = written;
        Enumeration e = entries.elements();
        while (e.hasMoreElements()) {
            writeCEN((ZipEntryExt) e.nextElement());
        }
        writeEND(off, written - off);
        finished = true;
    }

    /**
     * Closes the ZIP output stream as well as the stream being filtered.
     *
     * @throws ZipException if a ZIP file error has occurred
     * @throws IOException  if an I/O error has occurred
     */
    public void close() throws IOException {
        if (!closed) {
            super.close();
            closed = true;
        }
    }

    /*
     * Writes local file (LOC) header for specified entry.
     */
    private void writeLOC(ZipEntryExt eext) throws IOException {
        ZipEntry e = eext.entry;
        writeInt(LOCSIG);        // LOC header signature
        writeShort(eext.version);      // version needed to extract
        writeShort(eext.flag);         // general purpose bit flag
        writeShort(e.getMethod());       // compression method
        writeInt(e.getTime());           // last modification time
        if ((eext.flag & 8) == 8) {
            // store size, uncompressed size, and crc-32 in data descriptor
            // immediately following compressed entry data
            writeInt(0);
            writeInt(0);
            writeInt(0);
        } else {
            writeInt(e.getCrc());        // crc-32
            writeInt(e.getCompressedSize());      // compressed size
            writeInt(e.getSize());       // uncompressed size
        }
        byte[] nameBytes = getUTF8Bytes(e.getName());
        writeShort(nameBytes.length);
        byte[] extra = e.getExtra();
        writeShort(extra != null ? extra.length : 0);
        writeBytes(nameBytes, 0, nameBytes.length);
        if (extra != null) {
            writeBytes(extra, 0, extra.length);
        }
        locoff = written;
    }

    /*
     * Writes extra data descriptor (EXT) for specified entry.
     */
    private void writeEXT(ZipEntry e) throws IOException {
        writeInt(EXTSIG);        // EXT header signature
        writeInt(e.getCrc());        // crc-32
        writeInt(e.getCompressedSize());        // compressed size
        writeInt(e.getSize());        // uncompressed size
    }

    /*
     * Write central directory (CEN) header for specified entry.
     * REMIND: add support for file attributes
     */
    private void writeCEN(ZipEntryExt eext) throws IOException {
        ZipEntry e = eext.entry;
        writeInt(CENSIG);        // CEN header signature
        writeShort(eext.version);        // version made by
        writeShort(eext.version);        // version needed to extract
        writeShort(eext.flag);        // general purpose bit flag
        writeShort(e.getMethod());        // compression method
        writeInt(e.getTime());        // last modification time
        writeInt(e.getCrc());        // crc-32
        writeInt(e.getCompressedSize());        // compressed size
        writeInt(e.getSize());        // uncompressed size
        byte[] nameBytes = getUTF8Bytes(e.getName());
        writeShort(nameBytes.length);
        byte[] extra = e.getExtra();
        writeShort(extra != null ? extra.length : 0);
        byte[] commentBytes;
        if (e.getComment() != null) {
            commentBytes = getUTF8Bytes(e.getComment());
            writeShort(commentBytes.length);
        } else {
            commentBytes = null;
            writeShort(0);
        }
        writeShort(0);            // starting disk number
        writeShort(0);            // internal file attributes (unused)
        writeInt(0);            // external file attributes (unused)
        writeInt(eext.offset);        // relative offset of local header
        writeBytes(nameBytes, 0, nameBytes.length);
        if (extra != null) {
            writeBytes(extra, 0, extra.length);
        }
        if (commentBytes != null) {
            writeBytes(commentBytes, 0, commentBytes.length);
        }
    }

    /*
     * Writes end of central directory (END) header.
     */
    private void writeEND(long off, long len) throws IOException {
        writeInt(ENDSIG);        // END record signature
        writeShort(0);            // number of this disk
        writeShort(0);            // central directory start disk
        writeShort(entries.size()); // number of directory entries on disk
        writeShort(entries.size()); // total number of directory entries
        writeInt(len);            // length of central directory
        writeInt(off);            // offset of central directory
        if (comment != null) {        // zip file comment
            byte[] b = getUTF8Bytes(comment);
            writeShort(b.length);
            writeBytes(b, 0, b.length);
        } else {
            writeShort(0);
        }
    }

    /*
     * Writes a 16-bit short to the output stream in little-endian byte order.
     */
    private void writeShort(int v) throws IOException {
        //OutputStream out = this.out;
        out.write((v >>> 0) & 0xff);
        out.write((v >>> 8) & 0xff);
        written += 2;
    }

    /*
     * Writes a 32-bit int to the output stream in little-endian byte order.
     */
    private void writeInt(long v) throws IOException {
        //OutputStream out = this.out;
        out.write((int) ((v >>> 0) & 0xff));
        out.write((int) ((v >>> 8) & 0xff));
        out.write((int) ((v >>> 16) & 0xff));
        out.write((int) ((v >>> 24) & 0xff));
        written += 4;
    }

    /*
     * Writes an array of bytes to the output stream.
     */
    private void writeBytes(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
        written += len;
    }

    /*
     * Returns the length of String's UTF8 encoding.
     */
    static int getUTF8Length(String s) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch <= 0x7f) {
                count++;
            } else if (ch <= 0x7ff) {
                count += 2;
            } else {
                count += 3;
            }
        }
        return count;
    }

    /*
     * Returns an array of bytes representing the UTF8 encoding
     * of the specified String.
     */
    private static byte[] getUTF8Bytes(String s) {
        char[] c = s.toCharArray();
        int len = c.length;
        // Count the number of encoded bytes...
        int count = 0;
        for (int i = 0; i < len; i++) {
            int ch = c[i];
            if (ch <= 0x7f) {
                count++;
            } else if (ch <= 0x7ff) {
                count += 2;
            } else {
                count += 3;
            }
        }
        // Now return the encoded bytes...
        byte[] b = new byte[count];
        int off = 0;
        for (int i = 0; i < len; i++) {
            int ch = c[i];
            if (ch <= 0x7f) {
                b[off++] = (byte) ch;
            } else if (ch <= 0x7ff) {
                b[off++] = (byte) ((ch >> 6) | 0xc0);
                b[off++] = (byte) ((ch & 0x3f) | 0x80);
            } else {
                b[off++] = (byte) ((ch >> 12) | 0xe0);
                b[off++] = (byte) (((ch >> 6) & 0x3f) | 0x80);
                b[off++] = (byte) ((ch & 0x3f) | 0x80);
            }
        }
        return b;
    }

    /*
     * Returns an array of bytes representing the ASCII respectively
     * ISO/IEC 8859-1 encoding of the specified String.
     *
     * Note: This conversion may result in duplicate file names.
     *       To reduce the risk a little bit, we should use a smarter
     *       conversion algorithm, which coalesces less characters into
     *       the '_' placeholder character.
     */
    private static byte[] getASCIIBytes(String s) {
        char[] c = s.toCharArray();
        int len = c.length;
        // Count the number of encoded bytes...
        int count = len;
        // Now return the encoded bytes...
        byte[] b = new byte[count];
        int off = 0;
        for (int i = 0; i < len; i++) {
            int ch = c[i];
            if (ch <= 0x100) {
                b[off++] = (byte) ch;
            } else {
                b[off++] = (byte) '_';
            }
        }
        return b;
    }

    public void write(int b) throws IOException {
    }

    public OutputStream getOutputStream() {
        return this;
    }

}
