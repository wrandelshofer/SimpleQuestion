/*
 * @(#)DummyClipboardOwner.java  1.0  November 1, 2003
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
 * DummyClipboardOwner.
 *
 * @author  Werner Randelshofer
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
    
    /** Creates a new instance. */
    private DummyClipboardOwner() {
    }
    
    public void lostOwnership(java.awt.datatransfer.Clipboard clipboard, java.awt.datatransfer.Transferable contents) {
    }
    
}
