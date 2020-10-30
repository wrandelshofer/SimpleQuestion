/*
 * @(#)TreeNodeCollection.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.gui.tree;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.AbstractList;

/**
 * Wraps a default mutable tree node with the List interface.
 *
 * @author Werni Randelshofer
 * @version 1.0 2001-10-08
 */
public class TreeNodeCollection extends AbstractList<Object> {
    DefaultMutableTreeNode model;

    /**
     * Creates new TreeNodeCollection
     */
    public TreeNodeCollection(DefaultMutableTreeNode n) {
        model = n;
    }

    public Object get(int index) {
        return model.getChildAt(index);
    }

    public int size() {
        return model.getChildCount();
    }
}
