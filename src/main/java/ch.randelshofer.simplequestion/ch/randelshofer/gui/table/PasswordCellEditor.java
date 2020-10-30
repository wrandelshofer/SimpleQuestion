/*
 * @(#)PasswordCellEditor.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.gui.table;

import javax.swing.DefaultCellEditor;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.UIManager;
import java.awt.Component;

/**
 * PassworCellEditor.
 *
 * @author Werner Randelshofer
 * @version 1.1 2006-05-06 Set a table cell editor border on the JPasswordField.
 * <br>1.0 August 24, 2003  Created.
 */
public class PasswordCellEditor extends DefaultCellEditor {
    static final long serialVersionUID = 1L;
//
//  Constructors
//

    /**
     * Constructs a new instance.
     */
    public PasswordCellEditor() {
        super(new JPasswordField());
        ((JPasswordField) editorComponent).setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
        //new EmptyBorder(0,0,0,0));
    }

//
//  Modifying
//

//
//  Implementing the TreeCellEditor Interface
//

    /**
     * Implements the <code>TreeCellEditor</code> interface.
     */
    public Component getTreeCellEditorComponent(JTree tree, Object value,
                                                boolean isSelected,
                                                boolean expanded,
                                                boolean leaf, int row) {
        String stringValue = tree.convertValueToText(value, isSelected,
                expanded, leaf, row, false);

        delegate.setValue(stringValue);
        ((JPasswordField) editorComponent).selectAll();
        return editorComponent;
    }

//
//  Implementing the CellEditor Interface
//

    /**
     * Implements the <code>TableCellEditor</code> interface.
     */
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected,
                                                 int row, int column) {
        delegate.setValue(value);
        ((JPasswordField) editorComponent).selectAll();
        return editorComponent;
    }
}
