/* @(#)MutableJTree.java
 *
 * Copyright (c) 2004-2006 Werner Randelshofer
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

import ch.randelshofer.gui.tree.DefaultMutableTreeModel;
import ch.randelshofer.gui.tree.MutableTreeModel;
import org.jhotdraw.util.ResourceBundleUtil;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneLayout;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.Container;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * A JTree that uses a MutableTreeModel. Users can add and remove elements
 * using a popup menu. MutableJTree also supports the standard clipboard
 * operations cut, copy and paste.
 *
 * @author Werner Randelshofer
 * @version 2.3.1 2006-01-04 Specifying Quaqua "tableHeader" button style for popup
 * button.
 * <br>2.3  2004-07-03  Reworked due to API changes in MutableTreeModel.
 * <br>2.2.1 2004-02-03 Fixed a problem in method unconfigureEnclosingScrollPane.
 * <br>2.2 2003-06-20 Add actions from MutableTreeModel to popup menu.
 * <br>2.1 2002-12-21 Popup button added.
 * <br>2.0.2 2002-11-20 Undo support added.
 * <br>2.0.1 2001-10-13
 */
public class MutableJTree extends JTree
        implements EditableComponent {
    /**
     * This inner class is used to prevent the API from being cluttered
     * by internal listeners.
     */
    private class EventHandler implements ClipboardOwner {
        /**
         * Notifies this object that it is no longer the owner of the contents
         * of the clipboard.
         */
        public void lostOwnership(Clipboard clipboard, Transferable contents) {

        }
    }

    private EventHandler eventHandler = new EventHandler();

    /**
     * Holds locale specific resources.
     */
    private ResourceBundleUtil labels;

    /**
     * Listener for popup mouse events.
     */
    private MouseAdapter popupListener;

    /**
     * Popup button at the top right corner
     * of the enclosing scroll pane.
     */
    private JButton popupButton;


    /**
     * Constructs a MutableJTree with an empty DefaultMutableTreeModel.
     */
    public MutableJTree() {
        super(new DefaultMutableTreeModel());
        init();
    }

    /**
     * Constructs a MutableJTree with the specified MutableTreeModel.
     */
    public MutableJTree(MutableTreeModel m) {
        super(m);
        init();
    }

    /**
     * This method is called from the constructor to initialize the Object.
     */
    private void init() {
        initComponents();

        // The popup button will be placed on the top right corner
        // of the parent JScrollPane when the MutableJList is
        // added to a JScrollPane.
        popupButton = new JButton();
        popupButton.setIcon(Icons.POPUP_ICON);
        popupButton.putClientProperty("Quaqua.Button.style", "tableHeader");
        popupButton.addMouseListener(
                new MouseAdapter() {
                    public void mousePressed(MouseEvent evt) {
                        if (isEnabled()) {
                            createPopup().show(popupButton, 0, popupButton.getHeight());
                        }
                    }
                }
        );

        // The popup listener provides an alternative way for
        // opening the popup menu.
        popupListener = new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                if (isEnabled() && evt.isPopupTrigger()) {
                    createPopup().show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }

            public void mouseReleased(MouseEvent evt) {
                if (isEnabled() && evt.isPopupTrigger()) {
                    createPopup().show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }
        };
        addMouseListener(popupListener);

        // All locale specific and LAF specific
        // labels are read from a resource bundle.
        initLabels(Locale.getDefault());
    }

    /**
     * Initializes the labels in a locale specific and
     * look-and-feel (LAF) specific way.
     */
    private void initLabels(Locale locale) {
        // remove previously installed key strokes
        KeyStroke keyStroke;
        if (labels != null) {
            if (null != (keyStroke = labels.getKeyStroke("editNewAcc"))) {
                unregisterKeyboardAction(keyStroke);
            }
            if (null != (keyStroke = labels.getKeyStroke("editDuplicateAcc"))) {
                unregisterKeyboardAction(keyStroke);
            }
            if (null != (keyStroke = labels.getKeyStroke("editCutAcc"))) {
                unregisterKeyboardAction(keyStroke);
            }
            if (null != (keyStroke = labels.getKeyStroke("editCopyAcc"))) {
                unregisterKeyboardAction(keyStroke);
            }
            if (null != (keyStroke = labels.getKeyStroke("editPasteAcc"))) {
                unregisterKeyboardAction(keyStroke);
            }
            if (null != (keyStroke = labels.getKeyStroke("editDeleteAcc"))) {
                unregisterKeyboardAction(keyStroke);
            }
        }

        // get the locale and LAF specific resources
        labels = new ResourceBundleUtil(ResourceBundle.getBundle(
                "ch.randelshofer.gui.Labels", locale
        ));

        // install key strokes
        if (labels != null) {
            if (null != (keyStroke = labels.getKeyStroke("editNewAcc"))) {
                registerKeyboardAction(
                        new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                if (isEnabled()) {
                                    editNew();
                                }
                            }
                        },
                        keyStroke,
                        WHEN_FOCUSED
                );
            }

            if (null != (keyStroke = labels.getKeyStroke("editDuplicateAcc"))) {
                registerKeyboardAction(
                        new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                if (isEnabled()) {
                                    editDuplicate();
                                }
                            }
                        },
                        keyStroke,
                        WHEN_FOCUSED
                );
            }

            if (null != (keyStroke = labels.getKeyStroke("editCutAcc"))) {
                registerKeyboardAction(
                        new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                if (isEnabled()) {
                                    editCut();
                                }
                            }
                        },
                        keyStroke,
                        WHEN_FOCUSED
                );
            }

            if (null != (keyStroke = labels.getKeyStroke("editCopyAcc"))) {
                registerKeyboardAction(
                        new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                if (isEnabled()) {
                                    editCopy();
                                }
                            }
                        },
                        keyStroke,
                        WHEN_FOCUSED
                );
            }

            if (null != (keyStroke = labels.getKeyStroke("editPasteAcc"))) {
                registerKeyboardAction(
                        new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                if (isEnabled()) {
                                    editPaste();
                                }
                            }
                        },
                        keyStroke,
                        WHEN_FOCUSED
                );
            }

            if (null != (keyStroke = labels.getKeyStroke("editDeleteAcc"))) {
                registerKeyboardAction(
                        new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                if (isEnabled()) {
                                    editDelete();
                                }
                            }
                        },
                        keyStroke,
                        WHEN_FOCUSED
                );
            }
        }
    }

    /**
     * Creates the popup menu. The contents of the popup menu
     * is determined by the current selection.
     *
     * @return The popup menu.
     */
    protected JPopupMenu createPopup() {
        final TreePath[] selectedPaths = getSelectionPaths();
        final MutableTreeModel model = (MutableTreeModel) getModel();
        final TreePath leadSelectionPath = (selectedPaths == null || selectedPaths.length == 0) ? new TreePath(model.getRoot()) : getSelectionModel().getLeadSelectionPath();
        final MutableTreeNode leadNode = (MutableTreeNode) leadSelectionPath.getLastPathComponent();
        final JPopupMenu popup = new JPopupMenu();

        JMenuItem item;
        boolean b;

        // New
        Object[] types = model.getCreatableNodeTypes(leadNode);
        Object defaultType = model.getCreatableNodeType(leadNode);
        for (int i = 0; i < types.length; i++) {
            final Object newChildType = types[i];
            item = new JMenuItem(labels.getFormatted("editNewMenu", new Object[]{newChildType.toString()}));
            if (types[i] == defaultType) {
                item.setMnemonic(labels.getMnemonic("editNewMnem"));
                if (labels.getKeyStroke("editNewAcc") != null) {
                    item.setAccelerator(labels.getKeyStroke("editNewAcc"));
                }
            }

            item.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent actionEvent) {
                            MutableTreeModel m = (MutableTreeModel) getModel();
                            m.createNodeAt(
                                    newChildType,
                                    leadNode,
                                    m.getChildCount(leadNode)
                            );
                            expandPath(leadSelectionPath);
                        }
                    }
            );
            popup.add(item);
        }

        if (popup.getComponentCount() > 0) {
            popup.addSeparator();
        }

        // Cut
        item = new JMenuItem(labels.getString("editCutMenu"));
        item.setMnemonic(labels.getMnemonic("editCutMnem"));
        if (labels.getKeyStroke("editCutAcc") != null) {
            item.setAccelerator(labels.getKeyStroke("editCutAcc"));
        }
        boolean enabled = true;
        if (selectedPaths != null) {
            for (int i = 0; i < selectedPaths.length; i++) {
                if (!model.isNodeRemovable((MutableTreeNode) selectedPaths[i].getLastPathComponent())) {
                    enabled = false;
                    break;
                }
            }
        }
        item.setEnabled(enabled && model.isNodeRemovable(leadNode));
        item.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        editCut();
                    }
                }
        );
        popup.add(item);

        // Copy
        item = new JMenuItem(labels.getString("editCopyMenu"));
        item.setMnemonic(labels.getMnemonic("editCopyMnem"));
        if (labels.getKeyStroke("editCopyAcc") != null) {
            item.setAccelerator(labels.getKeyStroke("editCopyAcc"));
        }
        item.setEnabled(leadNode != null);
        item.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        editCopy();
                    }
                }
        );
        popup.add(item);

        // Paste
        item = new JMenuItem(labels.getString("editPasteMenu"));
        item.setMnemonic(labels.getMnemonic("editPasteMnem"));
        if (labels.getKeyStroke("editPasteAcc") != null) {
            item.setAccelerator(labels.getKeyStroke("editPasteAcc"));
        }
        item.setEnabled(
                model.getCreatableNodeTypes(leadNode).length > 0
                        || leadNode.getParent() != null && model.getCreatableNodeTypes(leadNode.getParent()).length > 0
        );
        item.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        editPaste();
                    }
                }
        );
        popup.add(item);

        // Add the "Delete" menu item.
        item = new JMenuItem(labels.getString("editDeleteMenu"));
        item.setMnemonic(labels.getMnemonic("editDeleteMnem"));
        if (labels.getKeyStroke("editDeleteAcc") != null) {
            item.setAccelerator(labels.getKeyStroke("editDeleteAcc"));
        }
        enabled = true;
        if (selectedPaths != null) {
            for (int i = 0; i < selectedPaths.length; i++) {
                if (!model.isNodeRemovable((MutableTreeNode) selectedPaths[i].getLastPathComponent())) {
                    enabled = false;
                    break;
                }
            }
        }
        item.setEnabled(enabled && model.isNodeRemovable(leadNode));
        item.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        editDelete();
                    }
                }
        );
        popup.add(item);

        popup.addSeparator();

        // add the "Select All" menu item
        item = new JMenuItem(labels.getString("editSelectAllMenu"));
        item.setMnemonic(labels.getMnemonic("editSelectAllMnem"));
        if (labels.getKeyStroke("editSelectAllAcc") != null) {
            item.setAccelerator(labels.getKeyStroke("editSelectAllAcc"));
        }
        item.setIcon(labels.getSmallIconProperty("editSelectAll", getClass()));
        item.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        setSelectionInterval(0, getRowCount() - 1);
                    }
                }
        );
        item.setEnabled(true);
        popup.add(item);
        // Actions
        
        /*
        // add the "New Node" menu item.
        final MutableTreeNode newNodeParent;
        if (leadSelectionPath == null) {
            newNodeParent = (MutableTreeNode) model.getRoot();
        } else {
        }
        final int newNodeIndex = (leadSelectionPath == null) ? root.getSize() : leadSelectionPath + 1;
        Object[] types = model.getCreatableTypes(newRow);
        Object defaultType = model.getCreatableType(newRow);
        for (int i = 0; i < types.length; i++) {
            final Object newRowType = types[i];
            item = new JMenuItem(labels.getFormatted("editNewMenu", new Object[] {newRowType}));
            if (newRowType.equals(defaultType)) {
                item.setMnemonic(labels.getMnemonic("editNewMnem"));
                if (labels.getKeyStroke("editNewAcc") != null)
                    item.setAccelerator(labels.getKeyStroke("editNewAcc"));
            }
            item.setIcon(labels.getImageIcon("editNewIcon", getClass()));
            item.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    model.create(newRow, newRowType);
                }
            }
            );
            item.setEnabled(model.isAddable(newRow));
            popup.add(item);
        }
         
         
        // add the "Cut" menu item.
        item = new JMenuItem(labels.getString("editCutMenu"));
        item.setMnemonic(labels.getMnemonic("editCutMnem"));
        if (labels.getKeyStroke("editCutAcc") != null)
            item.setAccelerator(labels.getKeyStroke("editCutAcc"));
        item.setIcon(labels.getImageIcon("editCutIcon", getClass()));
        item.addActionListener(
        new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                editCut();
            }
        }
        );
        b = selectedPaths.length > 0;
        for (int i = 0; i < selectedPaths.length; i++) {
            if (! model.isRemovable(selectedPaths[i])) {
                b = false;
                break;
            }
        }
        item.setEnabled(b);
        popup.add(item);
         
         
        // add the "Copy" menu item.
        item = new JMenuItem(labels.getString("editCopyMenu"));
        item.setMnemonic(labels.getMnemonic("editCopyMnem"));
        if (labels.getKeyStroke("editCopyAcc") != null)
            item.setAccelerator(labels.getKeyStroke("editCopyAcc"));
        item.setIcon(labels.getImageIcon("editCopyIcon", getClass()));
        item.addActionListener(
        new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                editCopy();
            }
        }
        );
        item.setEnabled(selectedPaths.length > 0);
        popup.add(item);
         
         
        // add the "Paste" menu item.
        item = new JMenuItem(labels.getString("editPasteMenu"));
        item.setMnemonic(labels.getMnemonic("editPasteMnem"));
        if (labels.getKeyStroke("editPasteAcc") != null)
            item.setAccelerator(labels.getKeyStroke("editPasteAcc"));
        item.setIcon(labels.getImageIcon("editPasteIcon", getClass()));
        item.addActionListener(
        new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                editPaste();
            }
        }
        );
        b = true;
        for (int i = 0; i < selectedPaths.length; i++) {
            if (! model.isRemovable(selectedPaths[i])) {
                b = false;
                break;
            }
        }
        item.setEnabled(b || selectedPaths.length == 0);
        popup.add(item);
         
         
        if (leadSelectionPath != -1) {
            // Add the duplicate row menu item
            item = new JMenuItem(labels.getString("editDuplicateMenu"));
            item.setMnemonic(labels.getMnemonic("editDuplicateMnem"));
            if (labels.getKeyStroke("editDuplicateAcc") != null)
                item.setAccelerator(labels.getKeyStroke("editDuplicateAcc"));
            item.setIcon(labels.getImageIcon("editDuplicateIcon", getClass()));
            boolean allowed = true;
            for (int i=0; i < selectedPaths.length; i++) {
                if (! model.isRemovable(i) || ! model.isAddable(i)) {
                    allowed = false;
                    break;
                }
            }
            item.setEnabled(allowed);
            item.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    //if (isEditing())
                    //    getCellEditor().stopCellEditing();
                    Transferable t = model.createTransferable(selectedPaths);
                    try {
                        model.importTransferable(t, DnDConstants.ACTION_COPY, (selectedPaths.length == 0) ? -1 : selectedPaths[0], false);
                    } catch (Exception e) {
                        throw new InternalError(e.getMessage()); // should never happen
                    }
                }
            }
            );
            popup.add(item);
         
         
            // add the "Delete" menu item.
            item = new JMenuItem(labels.getString("editDeleteMenu"));
            item.setMnemonic(labels.getMnemonic("editDeleteMnem"));
            if (labels.getKeyStroke("editDeleteAcc") != null)
                item.setAccelerator(labels.getKeyStroke("editDeleteAcc"));
            item.setIcon(labels.getImageIcon("editDeleteIcon", getClass()));
            allowed = true;
            for (int i=0; i < selectedPaths.length; i++) {
                if (! model.isRemovable(i)) {
                    allowed = false;
                    break;
                }
            }
            item.setEnabled(allowed);
            item.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    //if (isEditing())
                    //    getCellEditor().stopCellEditing();
                    for (int i=0; i < selectedPaths.length; i++) {
                        model.remove(selectedPaths[i] - i);
                    }
                }
            }
            );
            popup.add(item);
        }
         
        // add the "Select All" menu item
        item = new JMenuItem(labels.getString("editSelectAllMenu"));
        item.setMnemonic(labels.getMnemonic("editSelectAllMnem"));
        if (labels.getKeyStroke("editSelectAllAcc") != null)
            item.setAccelerator(labels.getKeyStroke("editSelectAllAcc"));
        item.setIcon(labels.getImageIcon("editSelectAllIcon", getClass()));
        item.addActionListener(
        new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                setSelectionInterval(0, getModel().getSize());
            }
        }
        );
        item.setEnabled(true);
        popup.add(item);
         
         
        if (leadSelectionPath != -1) {
            // Add actions provided by the MutableTableModel
            HashMap menus = new HashMap();
            Action[] actions = model.getActions(selectedPaths);
            if (actions != null) {
                for (int j = 0; j < actions.length; j++) {
                    String menuName = (String) actions[j].getValue("Menu");
                    if (menuName != null) {
                        if (menus.get(menuName) == null) {
                            JMenu m = new JMenu(menuName);
                            popup.add(m);
                            menus.put(menuName, m);
                        }
                        ((JMenu) menus.get(actions[j].getValue("Menu"))).add(actions[j]);
                    } else {
                        popup.add(actions[j]);
                    }
                }
            }
        }
         */
        return popup;

    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents

    }//GEN-END:initComponents

    /**
     * Inserts a new row after the lead selection row,
     * if the model allows it.
     */
    public void editNew() {
        MutableTreeModel model = (MutableTreeModel) getModel();
        TreePath path = getSelectionModel().getLeadSelectionPath();
        if (path == null) {
            path = new TreePath(model.getRoot());
        }
        int index = model.getChildCount(path.getLastPathComponent());
        do {
            MutableTreeNode node = (MutableTreeNode) path.getLastPathComponent();
            if (model.getCreatableNodeType(node) != null) {
                if (model.isNodeAddable(node, model.getChildCount(node))) {
                    model.createNodeAt(
                            model.getCreatableNodeType(node),
                            (MutableTreeNode) path.getLastPathComponent(), index
                    );
                    setSelectionPath(path.pathByAddingChild(model.getChild(node, index)));
                }
                break;
            }
            if (path.getPathCount() > 1) {
                index = node.getParent().getIndex(node) + 1;
            } else {
                index = 0;
            }
            path = path.getParentPath();
        } while (path.getPathCount() > 0);
    }

    /**
     * Cuts the selected region and place its contents into the system clipboard.
     */
    public void editCut() {
        final TreePath[] selectedPaths = getSelectionPaths();
        MutableTreeNode[] nodes = new MutableTreeNode[selectedPaths.length];
        MutableTreeModel m = (MutableTreeModel) getModel();
        for (int i = 0; i < selectedPaths.length; i++) {
            if (!m.isNodeRemovable((MutableTreeNode) selectedPaths[i].getLastPathComponent())) {
                getToolkit().beep();
                return;
            }
            nodes[i] = (MutableTreeNode) selectedPaths[i].getLastPathComponent();
        }

        getToolkit().getSystemClipboard().setContents(
                m.exportTransferable(nodes),
                eventHandler
        );

        for (int i = selectedPaths.length - 1; i > -1; i--) {
            m.removeNodeFromParent((MutableTreeNode) selectedPaths[i].getLastPathComponent());
        }
    }

    /**
     * Copies the selected region and place its contents into
     * the system clipboard.
     */
    public void editCopy() {
        System.out.println("MutableJTree.editCopy");
        final TreePath[] selectedPaths = getSelectionPaths();
        if (selectedPaths == null) {
            getToolkit().beep();
            return;
        }
        MutableTreeNode[] nodes = new MutableTreeNode[selectedPaths.length];
        Object[] selectedNodes = new Object[selectedPaths.length];
        for (int i = 0; i < selectedPaths.length; i++) {
            selectedNodes[i] = selectedPaths[i].getLastPathComponent();
            nodes[i] = (MutableTreeNode) selectedPaths[i].getLastPathComponent();
        }

        MutableTreeModel m = (MutableTreeModel) getModel();
        getToolkit().getSystemClipboard().setContents(
                m.exportTransferable(nodes),
                eventHandler
        );

    }

    /**
     * Pastes the contents of the system clipboard at the caret position.
     */
    public void editPaste() {
        System.out.println("MutableJTree.editPaste");
        TreePath insertionPath = getSelectionModel().getLeadSelectionPath();
        MutableTreeModel m = (MutableTreeModel) getModel();
        if (insertionPath == null) {
            insertionPath = new TreePath(m.getRoot());
        }
        MutableTreeNode node = (MutableTreeNode) insertionPath.getLastPathComponent();
        MutableTreeNode parent = m.isLeaf(node) ? (MutableTreeNode) (node).getParent() : node;
        if (parent == null) {
            parent = (MutableTreeNode) m.getRoot();
        }
        do {
            int index = parent.getIndex((TreeNode) insertionPath.getLastPathComponent());
            if (m.isImportable(getToolkit().getSystemClipboard().getContents(this).getTransferDataFlavors(), DnDConstants.ACTION_COPY, parent, index)) {
                try {
                    m.importTransferable(
                            getToolkit().getSystemClipboard().getContents(this),
                            DnDConstants.ACTION_COPY,
                            parent, index
                    );
                } catch (Exception e) {
                    System.out.println("MutableJTree paste failed");
                    getToolkit().beep();
                }
                return;
            }
            parent = (MutableTreeNode) parent.getParent();
        } while (parent != null);
        System.out.println("MutableJTree can't paste");
        getToolkit().beep();
    }

    /**
     * Deletes the component at (or after) the caret position.
     */
    public void editDelete() {
        if (isEditing()) {
            getCellEditor().stopCellEditing();
        }

        //if (isEnabled() && isEditable() && getSelectionCount() > 0) {
        if (isEnabled() && getSelectionCount() > 0) {
            MutableTreeModel model = (MutableTreeModel) getModel();
            TreePath[] paths = getSelectionPaths();
            if (paths == null) {
                getToolkit().beep();
                return;
            }

            int i;
            int j;

            // remove root from list of selected paths if root is not visible
            if (!isRootVisible()) {
                Object root = model.getRoot();
                for (i = 0; i < paths.length; i++) {
                    if (paths[i].getLastPathComponent() == root) {
                        removeSelectionPath(paths[i]);
                        paths = getSelectionPaths();
                        break;
                    }
                }
            }

            if (paths == null) {
                getToolkit().beep();
                return;
            }

            // remove descendants from list of selected nodes
            for (i = paths.length - 1; i >= 0; i--) {
                for (j = i - 1; j >= 0; j--) {
                    if (paths[i].isDescendant(paths[j])) {
                        paths[i] = null;
                    }
                }
            }

            // check if all nodes may be removed
            int deletableCount = 0;
            int nonDeletableCount = 0;
            for (i = 0; i < paths.length; i++) {
                if (paths[i] != null &&
                        !model.isNodeRemovable((MutableTreeNode) paths[i].getLastPathComponent())) {
                    nonDeletableCount++;
                } else {
                    deletableCount++;
                }
            }
            if (nonDeletableCount > 0) {
                getToolkit().beep();
                /*
                JOptionPane.showMessageDialog(
                this,
                labels.getString("nodeNotRemovableInfo"),
                labels.getString("nodeNotRemovableInfoTitle"),
                JOptionPane.INFORMATION_MESSAGE
                );*/
                requestFocus();
            } else {/*
                if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
                this,
                labels.getString("removeNodeQuestion"),
                labels.getString("removeNodeQuestionTitle"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
                )) {*/
                for (i = 0; i < paths.length; i++) {
                    if (paths[i] != null) {
                        model.removeNodeFromParent((MutableTreeNode) paths[i].getLastPathComponent());
                    }
                }
                //}
                requestFocus();
            }
        }
    }


    /**
     * Duplicates the selected region.
     */
    public void editDuplicate() {
        /*
        int[] selectedPaths = getSelectionPaths();
        if (selectedPaths.length > 0) {
            MutableTreeModel m = (MutableTreeModel) getModel();
         
            int row = getSelectionModel().getLeadSelectionPath() + 1;
            if (m.isAddable(row)) {
                try {
                    m.importTransferable(
                    m.createTransferable(selectedPaths),
                    DnDConstants.ACTION_COPY,
                    row,
                    false
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                    getToolkit().beep();
                }
            } else {
                getToolkit().beep();
            }
        }
         */
    }

    /**
     * Calls the configureEnclosingScrollPane method.
     *
     * @see #configureEnclosingScrollPane()
     */
    public void addNotify() {
        super.addNotify();
        configureEnclosingScrollPane();
    }


    /**
     * If this <code>MutableJList</code> is the <code>viewportView</code> of an
     * enclosing <code>JScrollPane</code> (the usual situation), configure this
     * scroll pane by, amongst other things, installing the lists's popup-menu
     * button at the top right corner of the scroll pane. When a <code>
     * MutableJList</code> is added to a <code>JScrollPane</code> in the usual
     * way, using <code>new JScrollPane(myTable)</code>, <code>addNotify<code>
     * is called in the <code>MutableJList</code> (when the table is added to
     * the viewport). <code>MutableJList<code>'s <code>addNotify<code> method
     * in turn calls this method, which is protected so that this default
     * installation procedure can be overridden by a subclass.
     *
     * @see #addNotify()
     */
    protected void configureEnclosingScrollPane() {
        //super.configureEnclosingScrollPane();

        Container p = getParent();
        if (p instanceof JViewport) {
            Container gp = p.getParent();
            if (gp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) gp;
                // Make certain we are the viewPort's view and not, for
                // example, the rowHeaderView of the scrollPane -
                // an implementor of fixed columns might do this.
                JViewport viewport = scrollPane.getViewport();
                if (viewport != null && viewport.getView() == this) {
                    // Install the mouse listener for the popup menu
                    viewport.addMouseListener(popupListener);

                    // Install a ScrollPaneLayout2 layout manager to ensure
                    // that the popup button we are going to add next is
                    // shown properly.
                    ScrollPaneLayout2 spl = new ScrollPaneLayout2();
                    scrollPane.setLayout(spl);
                    spl.syncWithScrollPane(scrollPane);

                    // Install the popup button at the top right corner
                    // of the JScrollPane
                    if (popupButton.getParent() == null) {
                        scrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, popupButton);
                    }
                }
            }
        }
    }

    /**
     * Calls the unconfigureEnclosingScrollPane method.
     *
     * @see #unconfigureEnclosingScrollPane()
     */
    public void removeNotify() {
        unconfigureEnclosingScrollPane();
        super.removeNotify();
    }


    /**
     * Reverses the effect of <code>configureEnclosingScrollPane</code> by
     * removing the button at the top right corner of the <code>JScrollPane</code>.
     * <code>MutableJTable</code>'s <code>removeNotify</code> method
     * calls this method, which is protected so that this default uninstallation
     * procedure can be overridden by a subclass.
     *
     * @see #removeNotify()
     */
    protected void unconfigureEnclosingScrollPane() {
        Container p = getParent();
        if (p != null && p instanceof JViewport) {
            Container gp = p.getParent();
            if (gp != null && gp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) gp;
                // Make certain we are the viewPort's view and not, for
                // example, the rowHeaderView of the scrollPane -
                // an implementor of fixed columns might do this.
                JViewport viewport = scrollPane.getViewport();
                if (viewport != null && viewport.getView() == this) {
                    // Remove the previously installed mouse listener for the popup menu
                    viewport.removeMouseListener(popupListener);

                    // Remove the previously installed ScrollPaneLayout2
                    // layout manager.
                    ScrollPaneLayout spl = new ScrollPaneLayout();
                    scrollPane.setLayout(spl);
                    spl.syncWithScrollPane(scrollPane);

                    // Remove the popup button from the top right corner
                    // of the JScrollPane
                    try {
                        // I would like to set the corner to null. Unfortunately
                        // we are called from removeNotify. Removing a component
                        // during removeNotify leads to a NPE.
                        scrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, new JPanel());
                        //scrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, null);
                    } catch (NullPointerException e) {
                        // This try/catch block is a workaround for
                        // bug 4247092 which is present in JDK 1.3.1 and prior
                        // versions
                    }
                }
            }
        }
    }

    /**
     * Sets the enabled state of the component.
     */
    public void setEnabled(boolean b) {
        super.setEnabled(b);
        popupButton.setEnabled(b);
    }

    public JButton getPopupButton() {
        return popupButton;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}
