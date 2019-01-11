/*
 * @(#)JVMLocalObjectTransferable.java  1.0  2002-03-18
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

import java.awt.datatransfer.*;
import java.io.IOException;
/**
 *
 * @author  werni
 * @version 1.0 2002-03-08
 */
public class JVMLocalObjectTransferable
implements Transferable {
    private DataFlavor[] flavors;
    private Object data;
    
    /** Creates new JVMLocalObjectTransferable */
    public JVMLocalObjectTransferable(Class<?> transferClass, Object data) {
        this.data = data;
        flavors = new DataFlavor[] { 
            new DataFlavor(transferClass, "Object")
        };
    }

    public Object getTransferData(DataFlavor dataFlavor) 
    throws UnsupportedFlavorException, IOException {
        if (! dataFlavor.equals(flavors[0])) {
            throw new UnsupportedFlavorException(dataFlavor);
        }
        return data;
    }
    
    public boolean isDataFlavorSupported(DataFlavor dataFlavor) {
        return dataFlavor.equals(flavors[0]);
    }
    
    public DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }
}
