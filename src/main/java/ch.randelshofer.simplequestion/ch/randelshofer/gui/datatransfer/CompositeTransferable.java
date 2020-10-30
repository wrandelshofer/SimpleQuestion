/*
 * @(#)CompositeTransferable.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.gui.datatransfer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author Werner Randelshofer
 */
public class CompositeTransferable implements java.awt.datatransfer.Transferable {
    private HashMap<DataFlavor,Transferable> transferables = new HashMap<>();
    private LinkedList<DataFlavor> flavors = new LinkedList<>();

    /**
     * Creates a new instance of CompositeTransferable
     */
    public CompositeTransferable() {
    }

    public void add(Transferable t) {
        DataFlavor[] f = t.getTransferDataFlavors();
        for (int i = 0; i < f.length; i++) {
            if (!transferables.containsKey(f[i])) {
                flavors.add(f[i]);
            }
            transferables.put(f[i], t);

        }
    }

    /**
     * Returns an object which represents the data to be transferred.  The class
     * of the object returned is defined by the representation class of the flavor.
     *
     * @param flavor the requested flavor for the data
     * @throws IOException                if the data is no longer available
     *                                    in the requested flavor.
     * @throws UnsupportedFlavorException if the requested data flavor is
     *                                    not supported.
     * @see DataFlavor#getRepresentationClass
     */
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        Transferable t = transferables.get(flavor);
        if (t == null) {
            throw new UnsupportedFlavorException(flavor);
        }
        return t.getTransferData(flavor);
    }

    /**
     * Returns an array of DataFlavor objects indicating the flavors the data
     * can be provided in.  The array should be ordered according to preference
     * for providing the data (from most richly descriptive to least descriptive).
     *
     * @return an array of data flavors in which this data can be transferred
     */
    public DataFlavor[] getTransferDataFlavors() {
        return flavors.toArray(new DataFlavor[0]);
    }

    /**
     * Returns whether or not the specified data flavor is supported for
     * this object.
     *
     * @param flavor the requested flavor for the data
     * @return boolean indicating wjether or not the data flavor is supported
     */
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return transferables.containsKey(flavor);
    }
}
