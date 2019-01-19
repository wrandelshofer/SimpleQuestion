/*
 * LFPrintWriter.java   1.0  2002-03-15
 *
 * Copyright (c) 2002 Werner Randelshofer
 * Staldenmattweg 2, CH-6405 Immensee, Switzerland
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Werner Randelshofer. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Werner Randelshofer.
 *
 * 
 * The following conditions apply only, if this software is distributed 
 * as part of TinyLMS:
 *
 *      This program is free software; you can redistribute it and/or modify it 
 *      under the terms of the GNU General Public License as published by the 
 *      Free Software  Foundation; either version 2 of the License, or (at your
 *      option) any later version. 
 *
 *      This program is distributed in the hope that it will be useful, but 
 *      WITHOUT ANY WARRANTY; without even the implied warranty of 
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 *      Public License for more details. You should have received a copy of the
 *      GNU General Public License along with this program; if not, write to the
 *      Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 *      02111-1307 USA
 */

package ch.randelshofer.io;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * This is a subclass of java.io.PrintWriter. 
 *
 * <p>Unlike java.io.PrintWriter the println() methods use a configurable
 * line separator rather than the platform own's notion of a line separator. 
 * By default the <code>'\n'</code> character is used, but can be overriden
 * by calling <code>setLineSeparator</code>.
 *
 * @author  Werner Randelshofer
 * @version 1.0 2002-03-15
 */
public class LFPrintWriter extends PrintWriter {
    /** 
     * If this variable is true, the println() methods will flush
     * the output buffer.
     */
     private boolean autoFlush = false;

    /**
     * Line separator string.
     */
    private String lineSeparator = "\n";
    
    /**
     * Create a new PrintWriter, without automatic line flushing, and
     * with <code>'\n'</code> as line separator.
     *
     * @param  out        A character-output stream
     */
    public LFPrintWriter(Writer out) {
        this(out, false);
    }
    
    /**
     * Create a new PrintWriter,
     * with <code>'\n'</code> as line separator.
     *
     * @param  out        A character-output stream
     * @param  autoFlush  A boolean; if true, the println() methods will flush
     *                    the output buffer
     */
    public LFPrintWriter(Writer out, boolean autoFlush) {
        super(out, autoFlush);
        this.autoFlush = autoFlush;
    }
    
    /**
     * Create a new PrintWriter, without automatic line flushing, from an
     * existing OutputStream.  This convenience constructor creates the
     * necessary intermediate OutputStreamWriter, which will convert characters
     * into bytes using the default character encoding, and
     * with <code>'\n'</code> as line separator.
     *
     * @param  out        An output stream
     *
     * @see java.io.OutputStreamWriter#OutputStreamWriter(java.io.OutputStream)
     */
    public LFPrintWriter(OutputStream out) {
        this(out, false);
    }
    
    /**
     * Create a new PrintWriter from an existing OutputStream.  This
     * convenience constructor creates the necessary intermediate
     * OutputStreamWriter, which will convert characters into bytes using the
     * default character encoding, and
     * with <code>'\n'</code> as line separator.
     *
     * @param  out        An output stream
     * @param  autoFlush  A boolean; if true, the println() methods will flush
     *                    the output buffer
     *
     * @see java.io.OutputStreamWriter#OutputStreamWriter(java.io.OutputStream)
     */
    public LFPrintWriter(OutputStream out, boolean autoFlush) {
        this(new BufferedWriter(new OutputStreamWriter(out)), autoFlush);
    }
    
    /**
     * Terminate the current line by writing the line separator string.  The
     * line separator string can be defined by calling
     * <code>setLineSeparator()</code>, and is not necessarily a single newline
     * character (<code>'\n'</code>).
     */
    public void println() {
        print(lineSeparator);
        if (autoFlush) flush();
    }
    
    /**
     * Gets the line separator of the println() methods.
     */
    public String getLineSeparator() {
        return lineSeparator;
    }
    
    /**
     * Sets the line separator for the println() methods.
     */
    public void setLineSeparator(String lineSeparator) {
        this.lineSeparator = lineSeparator;
    }
}
