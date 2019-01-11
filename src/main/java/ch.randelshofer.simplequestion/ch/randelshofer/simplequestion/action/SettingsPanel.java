/*
 * @(#)SettingsPanel.java  2.1  2008-12-03
 *
 * Copyright (c) 2006-2008 Werner Randelshofer
 * Staldenmattweg 2, CH-6405 Immensee, Switzerland
 * All rights reserved.
 *
 * The copyright of this software is owned by Werner Randelshofer. 
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * Werner Randelshofer. For details see accompanying license terms.
 */

package ch.randelshofer.simplequestion.action;

import ch.randelshofer.gift.export.scorm.SCORMExporter;
import ch.randelshofer.gui.datatransfer.*;
import ch.randelshofer.io.ExtensionFileFilter;
import ch.randelshofer.simplequestion.SimpleQuestionView;
import ch.randelshofer.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.Collator;
import java.util.*;
import java.util.prefs.*;
import javax.swing.*;
import javax.swing.event.*;
import org.jhotdraw.util.ResourceBundleUtil;

/**
 * SettingsPanel.
 *
 * @author Werner Randelshofer
 * @version 2.1 2008-12-03 The external SCORM template can either be a 
 * ZIP-file or a directory.
 * <br>2.0 2007-11-15 SCORM settings and editor settings added.
 * <br>1.0 24. Juli 2006 Created.
 */
public class SettingsPanel extends javax.swing.JPanel {
    public final static long serialVersionUID=1L;
    private Preferences prefs;
    private ResourceBundleUtil labels;
    private JFileChooser fileChooser;
    private JFileChooser htmlExportChooser;
    
    private HashMap<String,JRadioButton> giftTemplateChoiceMap;
    private HashMap<String,JRadioButton> scormTemplateChoiceMap;
    
    /** Creates new form. */
    public SettingsPanel() {
        prefs = Preferences.userNodeForPackage(SimpleQuestionView.class);
        labels = new ResourceBundleUtil(ResourceBundle.getBundle("ch/randelshofer/simplequestion/Labels"));
        initComponents();
        editorLabel.setFont(UIManager.getFont("EmphasizedSystemFont"));
        giftTemplateLabel.setFont(UIManager.getFont("EmphasizedSystemFont"));
        scormTemplateLabel.setFont(UIManager.getFont("EmphasizedSystemFont"));
        
        // Editor settings
        String[] ffn = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        Arrays.sort(ffn, Collator.getInstance());
        DefaultComboBoxModel<String> cbm = new DefaultComboBoxModel<>(ffn);
        cbm.setSelectedItem(prefs.get("editorFontFamily", "Dialog"));
        fontFamilyCombo.setModel(cbm);
        fontFamilyCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                prefs.put("editorFontFamily", (String) fontFamilyCombo.getSelectedItem());
            }
        });
        fontSizeSpinner.setValue(prefs.getInt("editorFontSize", 13));
        fontSizeSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                prefs.putInt("editorFontSize", (Integer) fontSizeSpinner.getValue());
            }
        });
        
        // GIFT settings
        giftTemplateChoiceMap = new HashMap<String,JRadioButton>();
        for (Enumeration<AbstractButton> e=giftTemplateGroup.getElements(); e.hasMoreElements();) {
            JRadioButton b = (JRadioButton) e.nextElement();
            giftTemplateChoiceMap.put(b.getActionCommand(), b);
        }
        giftTemplateChoiceMap.get(prefs.get("templateChoice","sample")).setSelected(true);
        giftTemplateFileField.setText(prefs.get("templateFile",""));
        giftTemplateFileField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateGIFTtemplate();
            }
            public void removeUpdate(DocumentEvent e) {
                updateGIFTtemplate();
            }
            public void changedUpdate(DocumentEvent e) {
                updateGIFTtemplate();
            }
            private void updateGIFTtemplate() {
                prefs.put("templateFile", giftTemplateFileField.getText());
            }
        });
        giftTemplateFileField.setTransferHandler(new FileTextFieldTransferHandler());

        
        // SCORM settings
        final Preferences scormPrefs = Preferences.userNodeForPackage(SCORMExporter.class);
        scormTemplateChoiceMap = new HashMap<String,JRadioButton>();
        for (Enumeration<AbstractButton> e = scormTemplateGroup.getElements(); e.hasMoreElements();) {
            JRadioButton b = (JRadioButton) e.nextElement();
            scormTemplateChoiceMap.put(b.getActionCommand(), b);
        }
        scormTemplateChoiceMap.get(scormPrefs.get("scormTemplateChoice","internal")).setSelected(true);
        
        scormTemplateFileField.setText(scormPrefs.get("scormTemplateFile",""));
        scormTemplateFileField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateSCORMtemplate();
            }
            public void removeUpdate(DocumentEvent e) {
                updateSCORMtemplate();
            }
            public void changedUpdate(DocumentEvent e) {
                updateSCORMtemplate();
            }
            private void updateSCORMtemplate() {
                scormPrefs.put("scormTemplateFile", scormTemplateFileField.getText());
            }
        });
        scormTemplateFileField.setTransferHandler(new FileTextFieldTransferHandler());

        scormTemplateDirectoryField.setText(scormPrefs.get("scormTemplateDirectory",""));
        scormTemplateDirectoryField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateSCORMtemplate();
            }
            public void removeUpdate(DocumentEvent e) {
                updateSCORMtemplate();
            }
            public void changedUpdate(DocumentEvent e) {
                updateSCORMtemplate();
            }
            private void updateSCORMtemplate() {
                scormPrefs.put("scormTemplateDirectory", scormTemplateDirectoryField.getText());
            }
        });
        scormTemplateDirectoryField.setTransferHandler(new FileTextFieldTransferHandler(JFileChooser.DIRECTORIES_ONLY));
    }
    
    protected JFileChooser getFileChooser() {
        if (fileChooser == null) {
            fileChooser = new JFileChooser();
            String templateFile = prefs.get("templateFile","");
            if (templateFile.length() > 0) {
                fileChooser.setSelectedFile(new File("templateFile"));
            }
            fileChooser.setApproveButtonText(labels.getString("settings.chooseButton"));
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        }
        return fileChooser;
    }
    protected JFileChooser getDirectoryChooser() {
        if (fileChooser == null) {
            fileChooser = new JFileChooser();
            String templateFile = prefs.get("templateDirectory","");
            if (templateFile.length() > 0) {
                fileChooser.setSelectedFile(new File("templateDirectory"));
            }
            fileChooser.setApproveButtonText(labels.getString("filechooser.chooseApprove.text"));
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }
        return fileChooser;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        giftTemplateGroup = new javax.swing.ButtonGroup();
        scormTemplateGroup = new javax.swing.ButtonGroup();
        giftTemplateLabel = new javax.swing.JLabel();
        giftTemplateEmptyChoice = new javax.swing.JRadioButton();
        giftTemplateSampleChoice = new javax.swing.JRadioButton();
        giftTemplateFileChoice = new javax.swing.JRadioButton();
        giftTemplateFileField = new javax.swing.JTextField();
        giftTemplateFileChooseButton = new javax.swing.JButton();
        scormTemplateLabel = new javax.swing.JLabel();
        scormlTemplateInternalChoice = new javax.swing.JRadioButton();
        scormTemplateFileChoice = new javax.swing.JRadioButton();
        scormTemplateFileField = new javax.swing.JTextField();
        scormTemplateFileChooseButton = new javax.swing.JButton();
        scormTemplateExportButton1 = new javax.swing.JButton();
        editorLabel = new javax.swing.JLabel();
        fontLabel = new javax.swing.JLabel();
        fontFamilyCombo = new javax.swing.JComboBox<>();
        fontSizeSpinner = new javax.swing.JSpinner();
        scormTemplateDirectoryChoice = new javax.swing.JRadioButton();
        scormTemplateDirectoryField = new javax.swing.JTextField();
        scormTemplateDirectoryChooseButton = new javax.swing.JButton();

        giftTemplateLabel.setText(labels.getString("settings.giftTemplate")); // NOI18N

        giftTemplateGroup.add(giftTemplateEmptyChoice);
        giftTemplateEmptyChoice.setText(labels.getString("settings.giftTemplateEmpty")); // NOI18N
        giftTemplateEmptyChoice.setActionCommand("empty");
        giftTemplateEmptyChoice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                giftTemplateChoicePerformed(evt);
            }
        });

        giftTemplateGroup.add(giftTemplateSampleChoice);
        giftTemplateSampleChoice.setText(labels.getString("settings.giftTemplateSample")); // NOI18N
        giftTemplateSampleChoice.setActionCommand("sample");
        giftTemplateSampleChoice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                giftTemplateChoicePerformed(evt);
            }
        });

        giftTemplateGroup.add(giftTemplateFileChoice);
        giftTemplateFileChoice.setText(labels.getString("settings.giftTemplateFile")); // NOI18N
        giftTemplateFileChoice.setActionCommand("file");
        giftTemplateFileChoice.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                giftTemplateFileChoiceStateChanged(evt);
            }
        });
        giftTemplateFileChoice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                giftTemplateChoicePerformed(evt);
            }
        });

        giftTemplateFileField.setColumns(30);
        giftTemplateFileField.setEnabled(false);

        giftTemplateFileChooseButton.setText(labels.getString("settings.choose")); // NOI18N
        giftTemplateFileChooseButton.setEnabled(false);
        giftTemplateFileChooseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                giftChooseButtonPerformed(evt);
            }
        });

        scormTemplateLabel.setText(labels.getString("settings.scormTemplate")); // NOI18N

        scormTemplateGroup.add(scormlTemplateInternalChoice);
        scormlTemplateInternalChoice.setSelected(true);
        scormlTemplateInternalChoice.setText(labels.getString("settings.scormTemplateInternal")); // NOI18N
        scormlTemplateInternalChoice.setActionCommand("internal");
        scormlTemplateInternalChoice.setMargin(new java.awt.Insets(0, 0, 0, 0));
        scormlTemplateInternalChoice.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                scormTemplateChoiceChanged(evt);
            }
        });
        scormlTemplateInternalChoice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scormTemplateChoicePerformed(evt);
            }
        });

        scormTemplateGroup.add(scormTemplateFileChoice);
        scormTemplateFileChoice.setText(labels.getString("settings.scormTemplateFile")); // NOI18N
        scormTemplateFileChoice.setActionCommand("file");
        scormTemplateFileChoice.setMargin(new java.awt.Insets(0, 0, 0, 0));
        scormTemplateFileChoice.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                scormTemplateChoiceChanged(evt);
            }
        });
        scormTemplateFileChoice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scormTemplateChoicePerformed(evt);
            }
        });

        scormTemplateFileField.setColumns(30);
        scormTemplateFileField.setEnabled(false);

        scormTemplateFileChooseButton.setText(labels.getString("settings.choose")); // NOI18N
        scormTemplateFileChooseButton.setEnabled(false);
        scormTemplateFileChooseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scormFileChooseButtonPerformed(evt);
            }
        });

        scormTemplateExportButton1.setText(labels.getString("settings.export")); // NOI18N
        scormTemplateExportButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scormExportButtonPerformed(evt);
            }
        });

        editorLabel.setText(labels.getString("settings.editor")); // NOI18N

        fontLabel.setText(labels.getString("settings.font")); // NOI18N

        scormTemplateGroup.add(scormTemplateDirectoryChoice);
        scormTemplateDirectoryChoice.setText(labels.getString("settings.scormTemplateDirectory")); // NOI18N
        scormTemplateDirectoryChoice.setActionCommand("directory");
        scormTemplateDirectoryChoice.setMargin(new java.awt.Insets(0, 0, 0, 0));
        scormTemplateDirectoryChoice.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                scormTemplateChoiceChanged(evt);
            }
        });
        scormTemplateDirectoryChoice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scormTemplateChoicePerformed(evt);
            }
        });

        scormTemplateDirectoryField.setColumns(30);
        scormTemplateDirectoryField.setEnabled(false);

        scormTemplateDirectoryChooseButton.setText(labels.getString("settings.choose")); // NOI18N
        scormTemplateDirectoryChooseButton.setEnabled(false);
        scormTemplateDirectoryChooseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scormDirectoryChooseButtonPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(giftTemplateLabel)
                    .addComponent(scormTemplateLabel)
                    .addComponent(editorLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(giftTemplateFileChoice)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(giftTemplateFileField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(giftTemplateFileChooseButton))
                            .addComponent(giftTemplateSampleChoice)
                            .addComponent(giftTemplateEmptyChoice)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(scormTemplateDirectoryChoice)
                                    .addComponent(scormTemplateFileChoice)
                                    .addComponent(scormlTemplateInternalChoice))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(scormTemplateExportButton1)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                            .addComponent(scormTemplateDirectoryField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(scormTemplateDirectoryChooseButton))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                            .addComponent(scormTemplateFileField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(scormTemplateFileChooseButton)))))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(fontLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fontFamilyCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fontSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(54, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(editorLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fontLabel)
                    .addComponent(fontFamilyCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fontSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(giftTemplateLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(giftTemplateEmptyChoice)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(giftTemplateSampleChoice)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(giftTemplateFileChoice)
                    .addComponent(giftTemplateFileField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(giftTemplateFileChooseButton))
                .addGap(18, 18, 18)
                .addComponent(scormTemplateLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(scormlTemplateInternalChoice)
                    .addComponent(scormTemplateExportButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(scormTemplateFileField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scormTemplateFileChooseButton)
                    .addComponent(scormTemplateFileChoice))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(scormTemplateDirectoryField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scormTemplateDirectoryChooseButton)
                    .addComponent(scormTemplateDirectoryChoice))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void scormExportButtonPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scormExportButtonPerformed
        if (htmlExportChooser == null) {
            htmlExportChooser = new JFileChooser();
            htmlExportChooser.setFileFilter(new ExtensionFileFilter("zip", "Zip Archive"));
            htmlExportChooser.setSelectedFile(new File(prefs.get("htmlExportedTemplate", "GIFT Editor SCORM HTML Templates.zip")));
            htmlExportChooser.setApproveButtonText(labels.getString("filechooser.exportApprove.text"));
        }
        if (JFileChooser.APPROVE_OPTION == htmlExportChooser.showSaveDialog(this)) {
            prefs.put("htmlExportedTemplate", htmlExportChooser.getSelectedFile().getPath());
            final File target = htmlExportChooser.getSelectedFile();
            new Worker() {
                public Object construct() {
                    Object result = null;
                    if (! target.getParentFile().exists()) {
                        target.getParentFile().mkdirs();
                    }
                    InputStream in = null;
                    OutputStream out = null;
                    try {
                        in = getClass().getResourceAsStream("/scormtemplates.zip");
                        out = new FileOutputStream(target);
                        Files.copyStream(in, out);
                    } catch (IOException e) {
                        result = e;
                    } finally {
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException ex) {
                                result = (result == null) ? ex : result;
                            }
                        }
                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException ex) {
                                result = (result == null) ? ex : result;
                            }
                        }
                    }
                    return result;
                }
                public void finished(Object result) {
                    
                }
            }.start();
        }
    }//GEN-LAST:event_scormExportButtonPerformed
        
    private void scormTemplateChoicePerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scormTemplateChoicePerformed
        prefs.put("scormTemplateChoice", evt.getActionCommand());

}//GEN-LAST:event_scormTemplateChoicePerformed
    
    private void scormFileChooseButtonPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scormFileChooseButtonPerformed
        JFileChooser fc = getFileChooser();
        switch (fc.showOpenDialog(this)) {
            case JFileChooser.APPROVE_OPTION :
                scormTemplateFileField.setText(fc.getSelectedFile().getPath());
                prefs.put("scormTemplateFile", fc.getSelectedFile().getPath());
                break;
            default :
                break;
        }
}//GEN-LAST:event_scormFileChooseButtonPerformed
    
    private void giftChooseButtonPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_giftChooseButtonPerformed
        JFileChooser fc = getFileChooser();
        switch (fc.showOpenDialog(this)) {
            case JFileChooser.APPROVE_OPTION :
                giftTemplateFileField.setText(fc.getSelectedFile().getPath());
                prefs.put("templateFile", fc.getSelectedFile().getPath());
                break;
            default :
                break;
        }
    }//GEN-LAST:event_giftChooseButtonPerformed
    
    private void giftTemplateFileChoiceStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_giftTemplateFileChoiceStateChanged
        giftTemplateFileField.setEnabled(giftTemplateFileChoice.isSelected());
        giftTemplateFileChooseButton.setEnabled(giftTemplateFileChoice.isSelected());
    }//GEN-LAST:event_giftTemplateFileChoiceStateChanged
    
    private void giftTemplateChoicePerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_giftTemplateChoicePerformed
        prefs.put("templateChoice", evt.getActionCommand());
    }//GEN-LAST:event_giftTemplateChoicePerformed

    private void scormTemplateChoiceChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_scormTemplateChoiceChanged
         scormTemplateDirectoryField.setEnabled(scormTemplateDirectoryChoice.isSelected());
        scormTemplateDirectoryChooseButton.setEnabled(scormTemplateDirectoryChoice.isSelected());
        scormTemplateFileField.setEnabled(scormTemplateFileChoice.isSelected());
        scormTemplateFileChooseButton.setEnabled(scormTemplateFileChoice.isSelected());

}//GEN-LAST:event_scormTemplateChoiceChanged

    private void scormDirectoryChooseButtonPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scormDirectoryChooseButtonPerformed
        JFileChooser fc = getDirectoryChooser();
        switch (fc.showOpenDialog(this)) {
            case JFileChooser.APPROVE_OPTION :
                scormTemplateFileField.setText(fc.getSelectedFile().getPath());
                prefs.put("scormTemplateDirectory", fc.getSelectedFile().getPath());
                break;
            default :
                break;
        }
}//GEN-LAST:event_scormDirectoryChooseButtonPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel editorLabel;
    private javax.swing.JComboBox<String> fontFamilyCombo;
    private javax.swing.JLabel fontLabel;
    private javax.swing.JSpinner fontSizeSpinner;
    private javax.swing.JRadioButton giftTemplateEmptyChoice;
    private javax.swing.JRadioButton giftTemplateFileChoice;
    private javax.swing.JButton giftTemplateFileChooseButton;
    private javax.swing.JTextField giftTemplateFileField;
    private javax.swing.ButtonGroup giftTemplateGroup;
    private javax.swing.JLabel giftTemplateLabel;
    private javax.swing.JRadioButton giftTemplateSampleChoice;
    private javax.swing.JRadioButton scormTemplateDirectoryChoice;
    private javax.swing.JButton scormTemplateDirectoryChooseButton;
    private javax.swing.JTextField scormTemplateDirectoryField;
    private javax.swing.JButton scormTemplateExportButton1;
    private javax.swing.JRadioButton scormTemplateFileChoice;
    private javax.swing.JButton scormTemplateFileChooseButton;
    private javax.swing.JTextField scormTemplateFileField;
    private javax.swing.ButtonGroup scormTemplateGroup;
    private javax.swing.JLabel scormTemplateLabel;
    private javax.swing.JRadioButton scormlTemplateInternalChoice;
    // End of variables declaration//GEN-END:variables
    
}
