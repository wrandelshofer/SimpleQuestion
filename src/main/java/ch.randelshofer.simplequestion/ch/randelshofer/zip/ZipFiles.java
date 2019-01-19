/* @(#)ZipFiles.java
 *
 * Copyright (c) 2004-2008 Werner Randelshofer
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

import ch.randelshofer.util.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;
/**
 * ZipFiles.
 *
 * @author  Werner Randelshofer
 * @version 2.2 2008-12-03 Added support for ZipIn.
 * <br>2.1 2006-11-29 Added method zip with ZipOut as parameter.
 * <br>2.0 2006-07-26 Moved to package ch.randelshofer.zip. Added
 * support for ZipOut.
 * <br>1.0 13. Januar 2004  Created.
 */
public class ZipFiles {
    
    /** Prevent instance creation. */
    private ZipFiles() {
    }
    
    /**
     * Unzips a Zip archive into a directory.
     *
     * @param zipFile A Zip File.
     * @param directory The output directory.
     * @exception IOException when an I/O error occurs.
     */
    public static void unzip(File zipFile, File directory) throws IOException {
        unzip(zipFile, directory, new DefaultZipEntryFilter());
    }
    
    /**
     * Unzips a Zip archive into a directory.
     *
     * @param zipFile A Zip File.
     * @param directory The output directory.
     * @param filter A filter.
     * @exception IOException when an I/O error occurs.
     */
    public static void unzip(File zipFile, File directory, ZipEntryFilter filter) throws IOException {
        ZipInputStream in = null;
        try {
            in = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
            unzip(in, directory, filter);
        } finally {
            // Make sure we always close the output stream,
            // even when we have encountered an I/O exception of some kind.
            if (in != null) in.close();
        }
        
    }
    
    
    /**
     * Unzips a Zip input stream into a directory.
     *
     * @param in A Zip Input Stream.
     * @param directory The output directory.
     * @exception IOException when an I/O error occurs.
     */
    public static void unzip(ZipInputStream in, File directory)
    throws IOException {
        unzip(in, directory, new DefaultZipEntryFilter());
    }
    /**
     * Unzips a Zip input stream into a directory.
     *
     * @param in A Zip Input Stream.
     * @param directory The output directory.
     * @exception IOException when an I/O error occurs.
     *
     * @return Returns a list of unzipped files. The list contains the names
     * (String objects) of the unzipped entries.
     */
    public static void unzip(ZipInputStream in, File directory, ZipEntryFilter filter)
    throws IOException {
        
        // Variables used for I/O buffering
        byte[] buf = new byte[512];
        int len;
        
        // Streams and Zip entries
        OutputStream out = null;
        ZipEntry entry = null;
        
        // Algorithm
        try {
            if (! directory.exists()) directory.mkdirs();
            
            while ((entry = in.getNextEntry()) != null) {
                if (filter.accept(entry)) {
                    File outputFile = new File(directory, entry.getName().replace('/', File.separatorChar));
                    if (entry.isDirectory()) {
                        outputFile.mkdirs();
                    } else {
                        if (! outputFile.getParentFile().exists()) {
                            outputFile.getParentFile().mkdirs();
                        }
                        out = new FileOutputStream(outputFile);
                        while ((len = in.read(buf)) != -1) {
                            out.write(buf, 0, len);
                        }
                        out.close();
                        out = null;
                    }
                }
                in.closeEntry();
            }
        } finally {
            // Make sure we always close the output stream,
            // even when we have encountered an I/O exception of some kind.
            if (out != null) out.close();
        }
    }
    
    /**
     * Reads zip entries from the input stream and adds them to the output stream.
     * Prepends the specified directory name to the zip entry names.
     */
    public static void rezip(ZipInputStream in, ZipOutputStream out, String directoryName)
    throws IOException {
        rezip(in, out, directoryName, new DefaultZipEntryFilter());
    }
    /**
     * Reads zip entries from the input stream and adds them to the output stream.
     * Prepends the specified directory name to the zip entry names.
     * Uses the filter to decide which entries to include.
     */
    public static void rezip(ZipInputStream in, ZipOutputStream out, String directoryName, ZipEntryFilter filter)
    throws IOException {
        // Variables used for I/O buffering
        byte[] buf = new byte[512];
        int len;
        
        // Streams and Zip entries
        ZipEntry entry = null;
        
        // Make sure we have a valid directory name
        if (directoryName == null) {
            directoryName = "";
        } else if (directoryName.length() > 0 && ! directoryName.endsWith("/")) {
            directoryName += '/';
        }
        
        while ((entry = in.getNextEntry()) != null) {
            if (filter.accept(entry)) {
                String entryName = directoryName + entry.getName();
                
                if (entry.isDirectory()) {
                    out.putNextEntry(new ZipEntry(entryName));
                    out.closeEntry();
                } else {
                    out.putNextEntry(new ZipEntry(entryName));
                    while ((len = in.read(buf)) != -1) {
                        out.write(buf, 0, len);
                    }
                    out.closeEntry();
                }
            }
            in.closeEntry();
        }
    }
    /**
     * Reads zip entries from the input stream and adds them to the output stream.
     * Prepends the specified directory name to the zip entry names.
     */
    public static void rezip(ZipInputStream in, ZipOut out, String directoryName)
    throws IOException {
        rezip(in, out, directoryName, new DefaultZipEntryFilter());
    }
    /**
     * Reads zip entries from the input stream and adds them to the output stream.
     * Prepends the specified directory name to the zip entry names.
     * Uses the filter to decide which entries to include.
     */
    public static void rezip(ZipInputStream in, ZipOut out, String directoryName, ZipEntryFilter filter)
    throws IOException {
        // Variables used for I/O buffering
        byte[] buf = new byte[512];
        int len;
        
        // Streams and Zip entries
        ZipEntry entry = null;
        
        // Make sure we have a valid directory name
        if (directoryName == null) {
            directoryName = "";
        } else if (directoryName.length() > 0 && ! directoryName.endsWith("/")) {
            directoryName += '/';
        }
        
        while ((entry = in.getNextEntry()) != null) {
            if (filter.accept(entry)) {
                String entryName = directoryName + entry.getName();
                
                if (entry.isDirectory()) {
                    out.putNextEntry(new ZipEntry(entryName));
                    out.closeEntry();
                } else {
                    out.putNextEntry(new ZipEntry(entryName));
                    while ((len = in.read(buf)) != -1) {
                        out.write(buf, 0, len);
                    }
                    out.closeEntry();
                }
            }
            in.closeEntry();
        }
    }
    /**
     * Reads zip entries from the input stream and adds them to the output stream.
     * Prepends the specified directory name to the zip entry names.
     */
    public static void rezip(ZipIn in, ZipOut out, String directoryName)
    throws IOException {
        rezip(in, out, directoryName, new DefaultZipEntryFilter());
    }
    /**
     * Reads zip entries from the input stream and adds them to the output stream.
     * Prepends the specified directory name to the zip entry names.
     * Uses the filter to decide which entries to include.
     */
    public static void rezip(ZipIn in, ZipOut out, String directoryName, ZipEntryFilter filter)
    throws IOException {
        // Variables used for I/O buffering
        byte[] buf = new byte[512];
        int len;

        // Streams and Zip entries
        ZipEntry entry = null;

        // Make sure we have a valid directory name
        if (directoryName == null) {
            directoryName = "";
        } else if (directoryName.length() > 0 && ! directoryName.endsWith("/")) {
            directoryName += '/';
        }

        while ((entry = in.getNextEntry()) != null) {
            if (filter.accept(entry)) {
                String entryName = directoryName + entry.getName();

                if (entry.isDirectory()) {
                    out.putNextEntry(new ZipEntry(entryName));
                    out.closeEntry();
                } else {
                    out.putNextEntry(new ZipEntry(entryName));
                    InputStream streamIn = in.getInputStream();
                    while ((len = streamIn.read(buf)) != -1) {
                        out.write(buf, 0, len);
                    }
                    out.closeEntry();
                }
            }
            in.closeEntry();
        }
    }
    
    /**
     * Adds the contents of the file to the zip file using the specified name.
     * Prepends the specified directory name to the zip entry names.
     * Uses the filter to decide which entries to include.
     */
    public static void zip(File file, ZipOutputStream out, String fileName)
    throws IOException {
        // Variables used for I/O buffering
        byte[] buf = new byte[512];
        int len;
        
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            out.putNextEntry(new ZipEntry(fileName));
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.closeEntry();
        } finally {
            if (in != null) in.close();
        }
    }
    /**
     * Adds the contents of the file to the zip file using the specified name.
     * Prepends the specified directory name to the zip entry names.
     * Uses the filter to decide which entries to include.
     */
    public static void zip(File file, ZipOut out, String fileName)
    throws IOException {
        // Variables used for I/O buffering
        byte[] buf = new byte[512];
        int len;
        
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            out.putNextEntry(new ZipEntry(fileName));
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.closeEntry();
        } finally {
            if (in != null) in.close();
        }
    }
}
