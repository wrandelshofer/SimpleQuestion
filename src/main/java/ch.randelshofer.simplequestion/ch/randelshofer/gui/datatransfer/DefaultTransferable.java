/* @(#)DefaultTransferable.java
 *
 * Copyright (c) 2001 Werner Randelshofer
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

import java.io.*;
import java.awt.datatransfer.*;
/**
 * A Transferable which uses a char array or a byte array as its data source.
 *
 * @author  Werner Randelshofer
 * @version 2.0 2003-11-02 Data representation is binary. Uset the charset
 * parameter of the mimetype for conversion or add our own charset parameter
 * (utf-8).
 * <br>1.0 2002-04-07 Created.
 */
public class DefaultTransferable implements Transferable {
    private byte[] data;
    private DataFlavor flavor;
    
    /**
     * Creates a new instance using a char array as the data source.
     * The charset parameter of the mimetype is used to convert the chars 
     * into bytes.
     * If no charset parameter is specified <code>;charset="UTF-8"</code> is
     * added and the data is encoded using UTF-8.
     */
    public DefaultTransferable(char[] chars, String mimetype, String description) {
        try {
            MimeType mt = new MimeType(mimetype);
            String charset = mt.getParameter("charset");
            if (charset == null) {
                charset = "UTF-8";
                mt.setParameter("charset", charset);
            }
            this.data = new String(chars).getBytes(charset);
            this.flavor = new DataFlavor(mt.toString(), description);
        } catch (Exception e) {
            InternalError error = new InternalError(e.getMessage());
            error.initCause(e);
            throw error;
        }
    }

    /**
     * Creates a new instance using a byte array as the data source.
     */
    public DefaultTransferable(byte[] data, String mimetype, String description) {
            this.data = data;
            this.flavor = new DataFlavor(mimetype, description);
    }
    
    
    /**
     * Returns an object which represents the data to be transferred.  The class
     * of the object returned is defined by the representation class of the flavor.
     *
     * @param flavor the requested flavor for the data
     * @see DataFlavor#getRepresentationClass
     * @exception IOException                if the data is no longer available
     *             in the requested flavor.
     * @exception UnsupportedFlavorException if the requested data flavor is
     *             not supported.
     */
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (flavor.equals(this.flavor)) {
            //return new CharArrayReader(data);
            return new ByteArrayInputStream(data);
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }
    
    /**
     * Returns an array of DataFlavor objects indicating the flavors the data
     * can be provided in.  The array should be ordered according to preference
     * for providing the data (from most richly descriptive to least descriptive).
     * @return an array of data flavors in which this data can be transferred
     */
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] {flavor};
    }
    
    /**
     * Returns whether or not the specified data flavor is supported for
     * this object.
     * @param flavor the requested flavor for the data
     * @return boolean indicating wjether or not the data flavor is supported
     */
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(this.flavor);
    }
    
}
