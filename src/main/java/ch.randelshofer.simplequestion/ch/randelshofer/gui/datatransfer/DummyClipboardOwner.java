/*
 * @(#)DummyClipboardOwner.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.gui.datatransfer;

/**
 * DummyClipboardOwner.
 *
 * @author Werner Randelshofer
 * @version 1.0 November 1, 2003 Created.
 */
public class DummyClipboardOwner implements java.awt.datatransfer.ClipboardOwner {
    private static DummyClipboardOwner instance;

    public static DummyClipboardOwner getInstance() {
        if (instance == null) {
            instance = new DummyClipboardOwner();
        }
        return instance;
    }

    /**
     * Creates a new instance.
     */
    private DummyClipboardOwner() {
    }

    public void lostOwnership(java.awt.datatransfer.Clipboard clipboard, java.awt.datatransfer.Transferable contents) {
    }

}
