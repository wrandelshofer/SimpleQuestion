/*
 * @(#)SettingsDialog.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

/*
 * SettingsDialog.java
 *
 * Created on 24. Juli 2006, 13:29
 */

package ch.randelshofer.simplequestion.action;

import org.jhotdraw.util.ResourceBundleUtil;

import javax.swing.JFrame;
import java.util.ResourceBundle;

/**
 * @author wrandels
 */
public class SettingsDialog extends javax.swing.JDialog {
    public final static long serialVersionUID = 1L;
    private ResourceBundleUtil labels;

    /**
     * Creates new form SettingsDialog
     */
    public SettingsDialog(JFrame parent, boolean modal) {
        super(parent, modal);
        labels = new ResourceBundleUtil(ResourceBundle.getBundle("ch.randelshofer.simplequestion.Labels"));
        initComponents();
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        settingsPanel = new ch.randelshofer.simplequestion.action.SettingsPanel();
        buttonPanel = new javax.swing.JPanel();
        closeButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(labels.getString("settings")); // NOI18N
        setResizable(false);

        jPanel1.setLayout(new java.awt.BorderLayout());
        jPanel1.add(settingsPanel, java.awt.BorderLayout.CENTER);

        buttonPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 20, 20, 20));
        buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));

        closeButton.setText(labels.getString("settings.closeButton")); // NOI18N
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonPerformed(evt);
            }
        });
        buttonPanel.add(closeButton);

        jPanel1.add(buttonPanel, java.awt.BorderLayout.SOUTH);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void closeButtonPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonPerformed
        dispose();
    }//GEN-LAST:event_closeButtonPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SettingsDialog(new javax.swing.JFrame(), true).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton closeButton;
    private javax.swing.JPanel jPanel1;
    private ch.randelshofer.simplequestion.action.SettingsPanel settingsPanel;
    // End of variables declaration//GEN-END:variables

}
