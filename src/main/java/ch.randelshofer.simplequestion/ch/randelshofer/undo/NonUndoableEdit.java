/* @(#)NonUndoableEdit.java
 *
 * Copyright (c) 2003-2006 Werner Randelshofer
 * Staldenmattweg 2, CH-6405 Immensee, Switzerland
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Werner Randelshofer. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Werner Randelshofer.
 */

package ch.randelshofer.undo;

import javax.swing.undo.*;
/**
 * NonUndoableEdit.
 *
 * @author  Werner Randelshofer
 * @version 1.0 5. April 2004  Created.
 */
public class NonUndoableEdit extends AbstractUndoableEdit {
    public final static long serialVersionUID=1L;

    /** Creates a new instance. */
    public NonUndoableEdit() {
    }
    
    public boolean canUndo() {
        return false;
    }
    public boolean canRedo() {
        return false;
    }
}
