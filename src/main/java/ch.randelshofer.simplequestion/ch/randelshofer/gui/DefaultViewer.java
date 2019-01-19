/* @(#)DefaultViewer.java
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

import java.awt.*;

/**
 * Calls object.toString() and displays this in a JLabel.
 *
 * @author  Werner Randelshofer
 * @version 1.1 2002-02-22 Class Viewer has been renamed to ViewFactory.
 * <br>1.0 2001-10-05
 */
public class DefaultViewer 
extends javax.swing.JPanel implements ViewFactory {
    private final static long serialVersionUID=1L;

    /** Creates new form DefaultViewer */
    public DefaultViewer() {
        initComponents();
    }

    public Component getComponent(Component parent, Object value) {
        if (value instanceof Object[]) {
            return getComponent(parent, (Object[]) value);
        } else {
            label.setText((value == null) ? "" : value.toString());
        }
        return this;
    }
    public Component getComponent(Component parent, Object[] values) {
        StringBuffer buf = new StringBuffer();
        for (int i=0; i < values.length; i++) {
            if (i > 0) buf.append(' ');
            buf.append((values[i] == null) ? "" : values[i].toString());
        }
        label.setText(buf.toString());
        return this;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        label = new javax.swing.JLabel();
        
        setLayout(new java.awt.BorderLayout());
        
        label.setText("jLabel1");
        add(label, java.awt.BorderLayout.NORTH);
        
    }//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel label;
    // End of variables declaration//GEN-END:variables

}
