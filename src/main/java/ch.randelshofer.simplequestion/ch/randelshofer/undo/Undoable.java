/* @(#)Undoable.java
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

package ch.randelshofer.undo;

import javax.swing.event.*;

/**
 * This interface is implemented by components, which support undo
 * and redo.
 *
 * @author  Werner Randelshofer
 * @version 1.0 2001-10-09
 */
public interface Undoable {

    /**
     * Adds an UndoableEditListener.
     */
    public void addUndoableEditListener(UndoableEditListener l);
    
    /**
     * Removes an UndoableEditListener.
     */
    public void removeUndoableEditListener(UndoableEditListener l);
    
}

