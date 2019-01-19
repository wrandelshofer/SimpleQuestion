/* @(#)TreeModels.java
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

package ch.randelshofer.gui.tree;

import ch.randelshofer.gui.datatransfer.DefaultTransferable;
import ch.randelshofer.gui.datatransfer.JVMLocalObjectTransferable;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * TreeModels.
 *
 * @author Werner Randelshofer
 * @version 1.0 November 1, 2003 Created.
 */
public class TreeModels {

    /**
     * Prevent instance creation.
     */
    private TreeModels() {
    }

    /**
     * Creates a transferable in text/html format from
     * a mutable tree model.
     *
     * @return A transferable of type text/html
     */
    public static Transferable createHTMLTransferable(MutableTreeModel model, MutableTreeNode[] nodes) {
        try {
            CharArrayWriter w = new CharArrayWriter();
            w.write("<html><body><ul>");
            for (int i = 0; i < nodes.length; i++) {
                Object elem = ((DefaultMutableTreeNode) nodes[i]).getUserObject();
                w.write("<li>");
                w.write(elem.toString());
                w.write("</li>");
            }
            w.write("</ul></body></html>");
            w.close();
            return new DefaultTransferable(w.toCharArray(), "text/html", "HTML");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Creates a transferable in text/plain format from
     * a mutable tree model.
     *
     * @return A transferable of type java.awt.datatransfer.StringSelection
     */
    public static Transferable createPlainTransferable(MutableTreeModel model, MutableTreeNode[] nodes) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < nodes.length; i++) {
            Object elem = ((DefaultMutableTreeNode) nodes[i]).getUserObject();

            if (i != 0) {
                buf.append('\n');
            }
            buf.append(elem.toString());
        }
        return new StringSelection(buf.toString());
    }

    /**
     * Creates a local JVM transferable from
     * a mutable tree model.
     *
     * @return A JVM local object transferable of type java.util.LinkedList if
     * indices.length > 1. A JVM local object transferable of type
     * model.getElementAt(indices[0]).getClass() if indices.length = 1.
     */
    public static Transferable createLocalTransferable(MutableTreeModel model, MutableTreeNode[] nodes, Class<?> baseclass) {
        LinkedList<Object> l = new LinkedList<>();
        for (MutableTreeNode node : nodes) {
            Object elem = ((DefaultMutableTreeNode) node).getUserObject();
            l.add(node);
        }
        return new JVMLocalObjectTransferable(List.class, l);
    }

    /**
     * Removes all descendants from a node array.
     * <p>
     * A node is removed from the array when it is a descendant from
     * another node in the array.
     */
    public static MutableTreeNode[] removeDescendantsFromNodeArray(MutableTreeNode[] nodes) {
        int i, j;
        TreePath[] paths = new TreePath[nodes.length];
        for (i = 0; i < nodes.length; i++) {
            paths[i] = new TreePath(getPathToRoot(nodes[i]));
        }

        int removeCount = 0;
        for (i = 0; i < paths.length; i++) {
            for (j = 0; j < paths.length; j++) {
                if (i != j && paths[j] != null) {
                    if (paths[j].isDescendant(paths[i])) {
                        paths[i] = null;
                        removeCount++;
                        break;
                    }
                }
            }
        }

        MutableTreeNode[] result = new MutableTreeNode[nodes.length - removeCount];
        j = 0;
        for (i = 0; i < paths.length; i++) {
            if (paths[i] != null) {
                result[j++] = nodes[i];
            }
        }
        return result;
    }

    /**
     * Builds the parents of node up to and including the root node,
     * where the original node is the last element in the returned array.
     * The length of the returned array gives the node's depth in the
     * tree.
     *
     * @param aNode the TreeNode to get the path for
     * @param an    array of TreeNodes giving the path from the root to the
     *              specified node.
     */
    public static TreeNode[] getPathToRoot(TreeNode aNode) {
        return getPathToRoot(aNode, 0);
    }

    /**
     * Builds the parents of node up to and including the root node,
     * where the original node is the last element in the returned array.
     * The length of the returned array gives the node's depth in the
     * tree.
     *
     * @param aNode the TreeNode to get the path for
     * @param depth an int giving the number of steps already taken towards
     *              the root (on recursive calls), used to size the returned array
     * @return an array of TreeNodes giving the path from the root to the
     * specified node
     */
    public static TreeNode[] getPathToRoot(TreeNode aNode, int depth) {
        TreeNode[] retNodes;
        // This method recurses, traversing towards the root in order
        // size the array. On the way back, it fills in the nodes,
        // starting from the root and working back to the original node.

        /* Check for null, in case someone passed in a null node, or
           they passed in an element that isn't rooted at root. */
        if (aNode == null) {
            if (depth == 0) {
                return null;
            } else {
                retNodes = new TreeNode[depth];
            }
        } else {
            depth++;
            if (aNode.getParent() == null) {
                retNodes = new TreeNode[depth];
            } else {
                retNodes = getPathToRoot(aNode.getParent(), depth);
            }
            retNodes[retNodes.length - depth] = aNode;
        }
        return retNodes;
    }
}
