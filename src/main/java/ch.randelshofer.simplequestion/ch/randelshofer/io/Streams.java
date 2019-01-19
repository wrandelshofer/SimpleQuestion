/* @(#)Streams.java
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

package ch.randelshofer.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
/**
 * Streams.
 *
 * @author Werner Randelshofer
 * @version 1.0 11. April 2006 Created.
 */
public class Streams {
    
    /** Prevent instance creation. */
    private Streams() {
    }
    
    /**
     * Writes the contents of the input stream to the specified output file.
     */
    public static void writeTo(InputStream in, File outfile)
    throws IOException {
        OutputStream out = null;
        try {
            out = new FileOutputStream(outfile);
            writeTo(in, out);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
    /**
     * Writes the contents of the input stream to the specified output stream.
     */
    public static void writeTo(InputStream in, OutputStream out)
    throws IOException {
        // Variables used for I/O buffering
        byte[] buf = new byte[512];
        int len;
        
        while ((len = in.read(buf)) != -1) {
            out.write(buf, 0, len);
        }
    }
    /**
     * Reads the contents of the input stream into a String using UTF-8 encoding.
     */
    public static String toString(InputStream in)
    throws IOException {
        return toString(new InputStreamReader(in, "UTF-8"));
    }
    /**
     * Reads the contents of the input stream into a String.
     */
    public static String toString(Reader in)
    throws IOException {
        StringBuilder str = new StringBuilder();
    
        // Variables used for I/O buffering
        char[] cbuf = new char[512];
        int len;
        
        while ((len = in.read(cbuf)) != -1) {
            str.append(cbuf, 0, len);
        }
        
        return str.toString();
    }
}
