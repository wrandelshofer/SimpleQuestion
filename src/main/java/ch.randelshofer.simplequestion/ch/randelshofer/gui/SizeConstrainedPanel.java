/*
 * @(#)SizeConstrainedPanel.java  1.0  02 January 2005
 *
 * Copyright (c) 2004 Werner Randelshofer
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
 * SizeConstrainedPanel.
 *
 * @author  Werner Randelshofer
 * @version 1.0  02 January 2005  Created.
 */
public class SizeConstrainedPanel extends javax.swing.JPanel {
    private int preferredWidth = -1;
    private int preferredHeight = -1;
    
    /** Creates new form. */
    public SizeConstrainedPanel() {
        initComponents();
    }
    
    /**
     * Sets the preferred width of the panel, without affecting its preferred
     * height.
     * @param w Preferred width. The value -1 clears the preferred width.
     */
    public void setPreferredWidth(int w) {
        this.preferredWidth = w;
    }
    /**
     * Sets the preferred height of the panel, without affecting its preferred
     * width.
     * @param w Preferred width. The value -1 clears the preferred height.
     */
    public void setPreferredHeight(int h) {
        this.preferredHeight = h;
    }
    
    public Dimension getPreferredSize() {
        Dimension dim = super.getPreferredSize();
        if (preferredWidth != -1) dim.width = preferredWidth;
        if (preferredHeight != -1) dim.height = preferredHeight;
        return dim;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents

        setLayout(new java.awt.BorderLayout());

    }//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
}
