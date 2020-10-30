/*
 * @(#)Undoable.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.undo;

import javax.swing.event.UndoableEditListener;

/**
 * This interface is implemented by components, which support undo
 * and redo.
 *
 * @author Werner Randelshofer
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

