/*
 * @(#)TreeNodeCollection.java  1.0  2001-10-08
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

package ch.randelshofer.gui.tree;
import java.util.*;
import javax.swing.tree.*;
import ch.randelshofer.util.*;
/**
 * Wraps a default mutable tree node with the List interface.
 *
 * @author  Werni Randelshofer
 * @version 1.0 2001-10-08
 */
public class TreeNodeCollection extends AbstractList<Object> {
    DefaultMutableTreeNode model;

    /** Creates new TreeNodeCollection */
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
