/*
 * @(#)NonUndoableEdit.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.undo;

import javax.swing.undo.AbstractUndoableEdit;

/**
 * NonUndoableEdit.
 *
 * @author Werner Randelshofer
 * @version 1.0 5. April 2004  Created.
 */
public class NonUndoableEdit extends AbstractUndoableEdit {
    public final static long serialVersionUID = 1L;

    /**
     * Creates a new instance.
     */
    public NonUndoableEdit() {
    }

    public boolean canUndo() {
        return false;
    }

    public boolean canRedo() {
        return false;
    }
}
