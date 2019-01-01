/*
 * @(#)ZipInDirectory.java  1.0  2008-12-03
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
import java.util.LinkedList;
import java.util.Queue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Implements the ZipIn interface for a directory.
 *
 * @author wrandels
 */
public class ZipInDirectory implements ZipIn {

    private File dir;
    private Queue<ZipEntry> entryList;
    private InputStream currentInputStream;
    private ZipEntry currentEntry;

    /**
     * Creates a new ZIP input stream.
     * @param in the actual input stream
     */
    public ZipInDirectory(File dir) {
        this.dir = dir;
    }

    private void lazilyCreateEntryList() throws IOException {
        if (entryList == null) {
            entryList = new LinkedList<ZipEntry>();

        String dirPath = dir.getCanonicalPath();
        if (! dirPath.endsWith(File.separator)) {
            dirPath += File.separatorChar;
        }

            addEntriesRecursive(entryList, dir, dirPath);
        }
    }

    private void addEntriesRecursive(Queue<ZipEntry> l, File f, String dirPath) throws IOException {
        String filePath = f.getPath();
        if (filePath.startsWith(dirPath)) {
            filePath = filePath.substring(dirPath.length());
        }
        filePath = filePath.replace(File.separatorChar, '/');
        if (f.isDirectory()) {
            if (!filePath.endsWith("/")) {
                filePath += '/';
            }
            ZipEntry entry = new ZipEntry(filePath);
            l.add(entry);

            for (File subFile : f.listFiles()) {
                addEntriesRecursive(l, subFile, dirPath);
            }
        } else {
            ZipEntry entry = new ZipEntry(filePath);
            l.add(entry);
            entry.setSize(f.length());
        }
    }

    public ZipEntry getNextEntry() throws IOException {
        lazilyCreateEntryList();
        closeEntry();

        currentEntry = (entryList.isEmpty()) ? null : entryList.remove();
        return currentEntry;
    }

    public void closeEntry() throws IOException {
        if (currentInputStream != null) {
            currentInputStream.close();
            currentInputStream = null;
        }
    }

    public InputStream getInputStream() throws IOException {
        if (currentInputStream == null) {
            if (currentEntry == null) {
                throw new IOException("No current entry.");
            } else {
                String path = currentEntry.getName();
                path = path.replace('/', File.separatorChar);
                File file = new File(dir, path);
                currentInputStream = new FileInputStream(file);
            }
        }
        return currentInputStream;
    }

    public void close() throws IOException {
        closeEntry();
        entryList.clear();
    }
}
