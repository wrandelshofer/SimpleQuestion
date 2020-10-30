/*
 * @(#)ZipInDirectory.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */
package ch.randelshofer.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.zip.ZipEntry;

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
     *
     * @param in the actual input stream
     */
    public ZipInDirectory(File dir) {
        this.dir = dir;
    }

    private void lazilyCreateEntryList() throws IOException {
        if (entryList == null) {
            entryList = new LinkedList<ZipEntry>();

            String dirPath = dir.getCanonicalPath();
            if (!dirPath.endsWith(File.separator)) {
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
