/* @(#)UndoableTreeSelectionModel.java
 * Copyright © Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.gui.tree;


import javax.swing.event.EventListenerList;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.tree.TreePath;
import javax.swing.undo.StateEdit;
import javax.swing.undo.StateEditable;
import javax.swing.undo.UndoableEdit;
import java.util.Hashtable;

/**
 * Supports UndoableEditListeners.
 *
 * @author Werner Randelshofer
 * @version 1.0 2001-10-09
 */
public class UndoableTreeSelectionModel
        extends javax.swing.tree.DefaultTreeSelectionModel
        implements ch.randelshofer.undo.Undoable, StateEditable {
    static final long serialVersionUID = 1L;

    private static class TreeSelectionEdit
            extends StateEdit {
        static final long serialVersionUID = 1L;

        public TreeSelectionEdit(StateEditable anObject, String name) {
            super(anObject, name);
        }

        public boolean isSignificant() {
            return false;
        }
/*        public boolean addEdit(UndoableEdit edit) {
            if (edit instanceof TreeSelectionEdit) {
                TreeSelectionEdit that = (TreeSelectionEdit) edit;
                if (that.object == this.object) {
                    this.postState = that.postState;
                    that.die();
                    return true;
                }
            }
            return false;
        }/*
        public boolean replaceEdit(UndoableEdit edit) {
            if (edit instanceof TreeSelectionEdit) {
                TreeSelectionEdit that = (TreeSelectionEdit) edit;
                if (that.object == this.object) {
                    this.preState = that.preState;
                    that.die();
                    return true;
                }
            }
            return false;
        }
        */
    }


    protected EventListenerList listenerList = new EventListenerList();

    /**
     * Creates new UndoableTreeSelectionModel
     */
    public UndoableTreeSelectionModel() {
    }

    /**
     * Adds an UndoableEditListener.
     */
    public void addUndoableEditListener(UndoableEditListener listener) {
        listenerList.add(UndoableEditListener.class, listener);
    }

    /**
     * Removes an UndoableEditListener.
     */
    public void removeUndoableEditListener(UndoableEditListener listener) {
        listenerList.add(UndoableEditListener.class, listener);
    }

    /**
     * Notify all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the parameters passed into
     * the fire method.
     */
    protected void fireUndoableEditEvent(UndoableEdit edit) {
        UndoableEditEvent evt = null;

        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == UndoableEditListener.class) {
                // Lazily create the event
                if (evt == null) {
                    evt = new UndoableEditEvent(this, edit);
                }
                ((UndoableEditListener) listeners[i + 1]).undoableEditHappened(evt);
            }
        }
    }

    /**
     * Adds paths to the current selection.  If any of the paths in
     * paths are not currently in the selection the TreeSelectionListeners
     * are notified.
     * <p>The lead path is set to the last element in <code>paths</code>.
     *
     * @param paths the new paths to add to the current selection.
     */
    public void addSelectionPaths(TreePath[] paths) {
        // Create the edit during the "before" state of the object
        TreeSelectionEdit newEdit = new TreeSelectionEdit(this, "Select Tree Nodes");
        // Modify the object
        super.addSelectionPaths(paths);
        // "end" the edit when you are done modifying the object
        newEdit.end();
        fireUndoableEditEvent(newEdit);
    }

    /**
     * Empties the current selection.  If this represents a change in the
     * current selection, the selection listeners are notified.
     */
    public void clearSelection() {
        TreeSelectionEdit newEdit = new TreeSelectionEdit(this, "Clear Tree Selection");
        super.clearSelection();
        newEdit.end();
        fireUndoableEditEvent(newEdit);
    }

    /**
     * Removes path from the selection.  If path is in the selection
     * The TreeSelectionListeners are notified.
     *
     * @param path the path to remove from the selection.
     */
    public void removeSelectionPath(TreePath path) {
        TreeSelectionEdit newEdit = new TreeSelectionEdit(this, "Deselect Tree Node");
        super.removeSelectionPath(path);
        newEdit.end();
        fireUndoableEditEvent(newEdit);
    }

    /**
     * Removes paths from the selection.  If any of the paths in paths
     * are in the selection the TreeSelectionListeners are notified.
     *
     * @param paths the paths to remove from the selection.
     */
    public void removeSelectionPaths(TreePath[] paths) {
        TreeSelectionEdit newEdit = new TreeSelectionEdit(this, "Deselect Tree Nodes");
        super.removeSelectionPaths(paths);
        newEdit.end();
        fireUndoableEditEvent(newEdit);
    }

    /**
     * Sets the selection to path.  If this represents a change, then
     * the TreeSelectionListeners are notified.
     *
     * @param path new path to select
     */
    public void setSelectionPath(TreePath path) {
        TreeSelectionEdit newEdit = new TreeSelectionEdit(this, "Select Tree Node");
        super.setSelectionPath(path);
        newEdit.end();
        fireUndoableEditEvent(newEdit);
    }

    /**
     * Sets the selection to the paths in paths.  If this represents a
     * change the TreeSelectionListeners are notified.  Potentially
     * paths will be held by the reciever, in other words don't change
     * any of the objects in the array once passed in.
     * <p>The lead path is set to the last path in <code>pPaths</code>.
     *
     * @param paths new selection.
     */
    public void setSelectionPaths(TreePath[] paths) {
        TreeSelectionEdit newEdit = new TreeSelectionEdit(this, "Select Tree Nodes");
        super.setSelectionPaths(paths);
        newEdit.end();
        fireUndoableEditEvent(newEdit);
    }

    /**
     * Upon receiving this message the receiver should extract any relevant
     * state out of <EM>state</EM>.
     */
    public void restoreState(Hashtable<?, ?> state) {
        Object value = state.get("selection");

        if (value != null) {
            if (value instanceof TreePath[]) {
                super.setSelectionPaths((TreePath[]) value);
            } else {
                super.clearSelection();
            }
        }
    }

    /**
     * Upon receiving this message the receiver should place any relevant
     * state into <EM>state</EM>.
     */
    public void storeState(Hashtable<Object, Object> state) {
        if (selection != null) {
            state.put("selection", selection.clone());
        } else {
            state.put("selection", "EMPTY");
        }
    }
}
