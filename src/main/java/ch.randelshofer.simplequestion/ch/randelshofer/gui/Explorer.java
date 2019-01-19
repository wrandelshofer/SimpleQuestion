/* @(#)Explorer.java
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

package ch.randelshofer.gui;

import ch.randelshofer.gui.tree.UndoableTreeSelectionModel;
import ch.randelshofer.undo.Undoable;

import javax.swing.JPanel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.undo.UndoManager;
import java.awt.Component;
/**
 * This panel acts like an Explorer Window as commonly used
 * on Windows.
 *
 * @author  Werner Randelshofer
 * @version 1.1 2002-02-11 Class Viewer has been renamed to ViewFactory.
 * <br>1.0 2001-10-05
 */
public class Explorer extends javax.swing.JPanel {
    /**
     * The viewFactory supplies components for rendering the
     * selected item(s) of the tree.
     */
    private ViewFactory viewFactory = new DefaultViewer();
    /**
     * This variable holds the Object or Object[]-array
     * of the objects being viewed currently.
     */
    private Object viewedObject;
    /**
     * This variable holds the Component which displays
     * the current viewedObject.
     */
    private Component view;
    
    /**
     * Undo Manager for undo/redo support.
     */
    private UndoManager undo;

    /** Creates new form Explorer */
    public Explorer() {
        initComponents();
        tree.setSelectionModel(new UndoableTreeSelectionModel());
        tree.addTreeSelectionListener(
            new TreeSelectionListener() {
                public void valueChanged(TreeSelectionEvent evt) {
                    if ((undo != null) 
                    && (view instanceof Undoable)) {
                            ((Undoable) view).removeUndoableEditListener(undo);
                    }

                    Component newView = null;
                    
                    TreePath[] paths = tree.getSelectionPaths();
                    if (paths == null || paths.length == 0) {
                        newView = null;
                        
                    } else if (paths.length == 1) {
                        Object value = paths[0].getLastPathComponent();
                        viewedObject = value;
                        newView = viewFactory.getComponent(Explorer.this, viewedObject);
                    } else {
                        Object[] values = new Object[paths.length];
                        for (int i=0; i < paths.length; i++) {
                            values[i] = paths[i].getLastPathComponent();
                        }
                        viewedObject = values;
                        newView = viewFactory.getComponent(Explorer.this, viewedObject);
                    }
                    
                    if (newView == null) 
                        newView = new JPanel();
                    if (newView != view) {
                        view = newView;
                        rightPane.removeAll();
                        rightPane.add(view);
                        view.invalidate();
                        rightPane.validate();
                        rightPane.repaint();
                        view.setEnabled(isEnabled());
                    }
                    
                    if ((undo != null) 
                    && (view instanceof Undoable)) {
                        ((Undoable) view).addUndoableEditListener(undo);
                    }
                }
            }
        );
    }
    
    /**
     * Sets the tree model.
     */
    public void setTreeModel(TreeModel m) {
        tree.setModel(m);
    }

    /**
     * Expands all tree nodes, up to the
     * specified depth.
     *
     * @param depthLimit The depth limit.
     */
    public void expandAll(int depthLimit) {
        expandAll(depthLimit, Integer.MAX_VALUE);
    }

    /**
     * Expands all tree nodes, up to the
     * specified depth and maximal number
     * of children.
     *
     * @param depthLimit The depth limit.
     * @param childLimit The child count limit;
     */
    public void expandAll(int depthLimit, int childLimit) {
        TreeModel treeModel = getTreeModel();
        
        depthLimit += 2;
        for (int i=0; i < tree.getRowCount(); i++) {
            TreePath path = tree.getPathForRow(i);
            if (path.getPathCount() < depthLimit) {
                Object node = path.getLastPathComponent();
                if (! treeModel.isLeaf(node) && treeModel.getChildCount(node) < childLimit) {
                    tree.expandRow(i);
                }
            }
        }
    }
    
    /**
     * Gets the tree model.
     */
    public TreeModel getTreeModel() {
        return tree.getModel();
    }
    
    /**
     * Gets the tree component.
     */
    public MutableJTree getTree() {
        return tree;
    }
    
    public void setUndoManager(UndoManager value) {
        if (undo != null) {
            ((UndoableTreeSelectionModel) tree.getSelectionModel()).removeUndoableEditListener(undo);
        }
        undo = value;
        if (undo != null) {
            ((UndoableTreeSelectionModel) tree.getSelectionModel()).addUndoableEditListener(undo);
        }
    }

    public void setEnabled(boolean b) {
        super.setEnabled(b);
        tree.setEnabled(b);
        scrollPane.setEnabled(b);
        if (view != null) view.setEnabled(b);
    }
    
    /**
     * Sets the view factory.
     * The view factory renders the currently selected
     * TreeNode or Array of TreeNodes in the
     * right pane of the explorer.
     */
    public void setViewFactory(ViewFactory v) {
        viewFactory = v;
    }

    /**
     * Gets the viewFactory.
     */
    public ViewFactory getViewFactory() {
        return viewFactory;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        splitPane = new javax.swing.JSplitPane();
        rightPane = new javax.swing.JPanel();
        scrollPane = new javax.swing.JScrollPane();
        tree = new ch.randelshofer.gui.MutableJTree();

        setLayout(new java.awt.BorderLayout());

        splitPane.setBorder(null);
        splitPane.setDividerLocation(196);
        splitPane.setOneTouchExpandable(true);
        rightPane.setLayout(new java.awt.BorderLayout());

        rightPane.setPreferredSize(new java.awt.Dimension(400, 400));
        rightPane.setMinimumSize(new java.awt.Dimension(0, 0));
        splitPane.setRightComponent(rightPane);

        scrollPane.setBorder(null);
        scrollPane.setPreferredSize(new java.awt.Dimension(200, 400));
        scrollPane.setMinimumSize(new java.awt.Dimension(0, 0));
        tree.setShowsRootHandles(true);
        tree.setLargeModel(true);
        tree.setRootVisible(false);
        scrollPane.setViewportView(tree);

        splitPane.setLeftComponent(scrollPane);

        add(splitPane, java.awt.BorderLayout.CENTER);

    }//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel rightPane;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JSplitPane splitPane;
    private ch.randelshofer.gui.MutableJTree tree;
    // End of variables declaration//GEN-END:variables
}
