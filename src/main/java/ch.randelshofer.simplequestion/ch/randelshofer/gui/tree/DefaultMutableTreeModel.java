/* @(#)DefaultMutableTreeModel.java * * Copyright (c) 2001 Werner Randelshofer * Staldenmattweg 2, Immensee, CH-6405, Switzerland. * All rights reserved. * * This software is the confidential and proprietary information of * Werner Randelshofer. ("Confidential Information").  You shall not * disclose such Confidential Information and shall use it only in * accordance with the terms of the license agreement you entered into * with Werner Randelshofer. */package ch.randelshofer.gui.tree;import ch.randelshofer.gui.datatransfer.*;import java.awt.datatransfer.*;import javax.swing.*;import javax.swing.tree.*;import java.util.*;import java.io.*;/** * A simple mutable tree model. * * FIXME: Should override more methods in the superclass. * * @author Werner Randelshofer * @author 2.3 2002-04-08 Support for transferables added. * <br>2.0 2001-07-18 */public class DefaultMutableTreeModelextends DefaultTreeModelimplements MutableTreeModel {    static final long serialVersionUID = 1L;    protected Object[] childTypes = new Object[] {"Leaf", "Folder"};    private boolean enabled;    private boolean editable;    /**     * Supported Flavors for data import.     *     * Note: Implementation of method importData depends on     * the contents of this array.     */    private final static DataFlavor[] supportedFlavors = {        new DataFlavor(List.class, "Local VM Object"),        new DataFlavor(Object.class, "Local VM Object"),        DataFlavor.getTextPlainUnicodeFlavor()    };        /**     * Constructs a new DefaultMutableTreeModel using a     * DefaultMutableTreeNode as root of the tree.     */    public DefaultMutableTreeModel() {        this(new DefaultMutableTreeNode());    }    /**     * Constructs a new DefaultMutableTreeModel using the given tree node     * as the root of the tree.     */    public DefaultMutableTreeModel(TreeNode root) {        super(root, true);        this.root = root;    }        /**     * Invoke this to insert a new child at location index in parents children.     * This will then message nodesWereInserted to create the appropriate     * event. This is the preferred way to add children as it will create the     * appropriate event.     *     * @param   type       the type of the new child to be created.     * @param   parent     a node from the tree, obtained from this data source.     * @param   index      index of the child.     * @exception   IllegalStateException if the parent node does not allow children.     */    public void createNodeAt(Object type, MutableTreeNode parent, int index) throws IllegalStateException {        // determine if the new node may be inserted        int i;        Object[] allowedTypes = getCreatableNodeTypes(parent);        for (i=0; i < allowedTypes.length; i++) {            if (type.equals(allowedTypes[i])) break;        }        if (i == allowedTypes.length) {            throw new IllegalStateException("Can't insert node.");        };                // insert the node        DefaultMutableTreeNode newChild = new DefaultMutableTreeNode("unnamed");        newChild.setAllowsChildren(type.equals("Folder"));        insertNodeInto((MutableTreeNode) newChild, parent, index);    }        public void insertNodeInto(MutableTreeNode newChild, MutableTreeNode parent, int index) {        if (! isNodeAddable(parent, index)) {            throw new IllegalStateException("Cannot insert node");        }        super.insertNodeInto(newChild, parent, index);    }    /**     * Returns the type of children that may be created at     * this node.     */    public Object[] getCreatableNodeTypes(Object node) {        return (!isEnabled() || isLeaf(node)) ? new Object[] {} : childTypes;    }    /**     * Returns the type of children that may be created at     * this node.     */    public Object getCreatableNodeType(Object node) {        return (!isEnabled() || isLeaf(node)) ? null : childTypes[0];    }    /**     * Gets the editable state of the model.     */    public boolean isEditable() {        return editable;    }    /**     * Gets the enabled state of the model.     */    public boolean isEnabled() {        return enabled;    }    public boolean isNodeAddable(MutableTreeNode parent, int index) {        return isEnabled() && isEditable();    }    /**     * Returns wether the specified node may be removed.     *     * @param   node   a node from the tree, obtained from this data source.     * @return  Returns true for all nodes except for the root.     *          Returns false if the model is disabled.     */    public boolean isNodeRemovable(MutableTreeNode node) {        return isEnabled() && isEditable() && getRoot() != node;    }    /**     * Returns wether the specified node may be renamed.     *     * @param   node   a node from the tree, obtained from this data source.     * @return  Returns true for all nodes except.     *          Returns false if the model is disabled.     */    public boolean isNodeEditable(MutableTreeNode node) {        return isEnabled() && isEditable();    }    /**     * Removes a child from its parent.     *     * @param   node   a node from the tree, obtained from this data source.     */    public void removeNode(MutableTreeNode node) {        // determine if the node may be removed        if (! isNodeRemovable(node)) {            throw new IllegalStateException("Can't remove node.");        }                super.removeNodeFromParent(node);    }    /**     * Sets the editable state of the model.     *     * A disabled tree model returns Object[]{} for getAllowedChildTypes(Object),     * and false for isRemoveAllowed()     *     */    public void setEditable(boolean b) {        editable = b;    }    /**     * Sets the enabled state of the model.     *     * A disabled tree model returns Object[]{} for getAllowedChildTypes(Object),     * and false for isRemoveAllowed()     */    public void setEnabled(boolean b) {        enabled = b;    }    /**     * Sets the node types to be returned by getInsertableNodeTypes.     */    protected void setInsertableNodeTypes(Object[] childTypes) {        this.childTypes = childTypes;    }    /**     * Sets the root to <code>root</code>. This will throw an     * IllegalArgumentException if <code>root</code> is null.     */    public void setRoot(TreeNode aRoot) {        if(aRoot == null)            throw new IllegalArgumentException("Root must not be null.");        this.root = aRoot;        nodeStructureChanged(this.root);    }        /**     * Indicates whether the model would accept an import of the     * given set of data flavors prior to actually attempting     * to import it.     *     * @return true if the data can be inserted into the component,     * false otherwise     */    public boolean isImportable(DataFlavor[] transferFlavors, int action, MutableTreeNode parent, int index) {        if (isLeaf(parent)) return false;                for (int i=0; i < transferFlavors.length; i++) {            if (transferFlavors[i].isMimeTypeEqual("text/plain")) {                return true;            }            for (int j=0; j < supportedFlavors.length; j++) {                if (transferFlavors[i].equals(supportedFlavors[j])) {                    return true;                }            }        }                return false;    }        public Transferable exportTransferable(MutableTreeNode[] nodes) {        nodes = removeDescendantsFromNodeArray(nodes);                CompositeTransferable t = new CompositeTransferable();        try {            LinkedList<Object> l = new LinkedList<>();            CharArrayWriter w = new CharArrayWriter();            w.write("<html><body><ul>");            StringBuffer buf = new StringBuffer();            for (int i=0; i < nodes.length; i++) {                Object elem = ((DefaultMutableTreeNode) nodes[i]).getUserObject();                l.add(elem);                w.write("<li>");                w.write(elem.toString());                w.write("</li>");                                if (i != 0) buf.append('\n');                buf.append(elem.toString());            }            w.write("</ul></body></html>");            w.close();            t.add(new JVMLocalObjectTransferable(List.class, l));            t.add(new CharArrayReaderTransferable(w.toCharArray(), "text/html", "HTML"));            t.add(new StringSelection(buf.toString()));        } catch (IOException e) {            e.printStackTrace();            return null;        }                return t;    }        public int importTransferable(Transferable t, int action, MutableTreeNode parent, int index) {        if (! isImportable(t.getTransferDataFlavors(), action, parent, index)) return 0;        return 0;    }        /**     * Gets actions for the indicated nodes.     *     * @param   nodes   The nodes.     */    public Action[] getNodeActions(MutableTreeNode[] nodes) {        return new Action[0];    }            /**     * Removes all descendants from a node array.     * <p>     * A node is removed from the array when it is a descendant from     * another node in the array.     */    private MutableTreeNode[] removeDescendantsFromNodeArray(MutableTreeNode[] nodes) {        int i, j;        TreePath[] paths = new TreePath[nodes.length];        for (i=0; i < nodes.length; i++) {            paths[i] = new TreePath(getPathToRoot((TreeNode) nodes[i]));        }                int removeCount = 0;        for (i=0; i < paths.length; i++) {            for (j=0; j < paths.length; j++) {                if (i != j && paths[j] != null) {                    if (paths[j].isDescendant(paths[i])) {                        paths[i] = null;                        removeCount++;                        break;                    }                }            }        }                MutableTreeNode[] result = new MutableTreeNode[nodes.length - removeCount];        j = 0;        for (i=0; i < paths.length; i++) {            if (paths[i] != null) {                result[j++] = nodes[i];            }        }        return result;    }        /*    public void setValueAt(Object value, MutableTreeNode node) {        node.setUserObject(value);        fireTreeNodesChanged(this, getPathToRoot(node.getParent()), new int[] {node.getParent().getIndex(node)}, new Object[] {node});     }     */}