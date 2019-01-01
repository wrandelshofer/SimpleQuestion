/*
 * @(#)MimeTypeParseException.java  1.0  November 2, 2003
 *
 * Copyright (c) 2003 Werner Randelshofer
 * Staldenmattweg 2, Immensee, CH-6405, Switzerland.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Werner Randelshofer. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Werner Randelshofer.
 */

package ch.randelshofer.gui.datatransfer;

/**
 * A class to encapsulate MimeType parsing related exceptions
 * <p>
 * Implementation taken from java.awt.datatransfer.TypeParseException.java 1.10 01/12/03
 *
 * @serial exclude
 * @author  Werner Randelshofer
 * @version 1.0 November 2, 2003 Created.
 */
public class MimeTypeParseException extends Exception {
    
    // use serialVersionUID from JDK 1.2.2 for interoperability
    private static final long serialVersionUID = -5604407764691570741L;
    
    /**
     * Constructs a MimeTypeParseException with no specified detail message.
     */
    public MimeTypeParseException() {
        super();
    }
    
    /**
     * Constructs a MimeTypeParseException with the specified detail message.
     *
     * @param   s   the detail message.
     */
    public MimeTypeParseException(String s) {
        super(s);
    }
} // class MimeTypeParseException
