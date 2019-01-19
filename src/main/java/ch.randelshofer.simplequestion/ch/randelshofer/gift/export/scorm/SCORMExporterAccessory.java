/* @(#)SCORMExporterAccessory.java
 * Copyright © Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.gift.export.scorm;

import ch.randelshofer.io.ConfigurableFileFilter;
import ch.randelshofer.zip.ZipIn;
import org.jhotdraw.util.ResourceBundleUtil;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JRadioButton;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.prefs.Preferences;
import java.util.zip.ZipEntry;

/**
 * SCORMExporterAccessory.
 *
 * @author Werner Randelshofer
 * @version 2.0 2007-11-14 Support external SCORM templates
 * <br>1.1 2007-01-29 Prefix field added.
 * <br>1.0 2. August 2006 Created.
 */
public class SCORMExporterAccessory extends javax.swing.JPanel {
    private final static long serialVersionUID = 1L;
    private Preferences prefs;
    private ResourceBundleUtil labels;
    private JFileChooser fileChooser;
    private HashMap<String, JRadioButton> templateChoiceMap;
    private Map<String, String> styleMap;
    private String[][] styledefs;

    private final static Locale[] localedefs = {
            Locale.GERMAN,
            Locale.ENGLISH
    };
    /*
    static {
        HashMap<String,String> m = new HashMap<String,String>();
        for (String[] def : styledefs) {
            m.put(def[0], def[1]);
        }
        styleMap = Collections.unmodifiableMap(m);
    }*/
    private ConfigurableFileFilter fileFilter;

    /**
     * Creates new form.
     */
    public SCORMExporterAccessory(ConfigurableFileFilter cff) {
        fileFilter = cff;
        prefs = Preferences.userNodeForPackage(SCORMExporterAccessory.class);
        labels = new ResourceBundleUtil(ResourceBundle.getBundle("ch/randelshofer/gift/Labels"));

        initComponents();
        updateStyles();


        DefaultComboBoxModel<Locale> cbm = new DefaultComboBoxModel<>();
        int selectedIndex = 0;
        for (int i = 0; i < localedefs.length; i++) {
            cbm.addElement(localedefs[i]);
            if (localedefs[i].toString().equals(prefs.get("scorm.locale", Locale.GERMAN.toString()))) {
                selectedIndex = i;
            }
        }
        putClientProperty("locale", localedefs[selectedIndex]);
        localeCombo.setModel(cbm);
        localeCombo.setSelectedIndex(selectedIndex);

        titleField.setText(prefs.get("scorm.title", ""));
        cff.putClientProperty("title", titleField.getText());
        titleField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                titleChanged();
            }

            public void removeUpdate(DocumentEvent e) {
                titleChanged();
            }

            public void changedUpdate(DocumentEvent e) {
                titleChanged();
            }
        });
        prefixField.setText(prefs.get("scorm.prefix", ""));
        cff.putClientProperty("prefix", prefixField.getText());
        prefixField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                prefixChanged();
            }

            public void removeUpdate(DocumentEvent e) {
                prefixChanged();
            }

            public void changedUpdate(DocumentEvent e) {
                prefixChanged();
            }
        });


        if (!UIManager.getLookAndFeel().getID().equals("Aqua") &&
                !UIManager.getLookAndFeel().getID().equals("Quaqua")) {
            Component[] components = {
                    titleLabel,
                    titleField,
                    styleLabel,
                    styleCombo,
                    localeLabel,
                    localeCombo,
                    prefixLabel,
                    prefixField
            };
            for (int i = 0; i < components.length; i++) {
                GridBagLayout layout = (GridBagLayout) getLayout();
                GridBagConstraints gbc;
                gbc = layout.getConstraints(components[i]);
                gbc.gridy = i / 2;
                if (gbc.gridy == 0) {
                    gbc.insets = new Insets(0, 10, 0, 0);
                } else {
                    gbc.insets = new Insets(4, 10, 0, 0);
                }
                layout.setConstraints(components[i], gbc);
            }
        }

        fileFilter.putClientProperty("locale", new Locale(prefs.get("scorm.locale", "de")));
        fileFilter.putClientProperty("stylesheet", prefs.get("scorm.style", "style/style_hslu.css"));
        fileFilter.putClientProperty("prefix", getPrefix());
        fileFilter.putClientProperty("title", getTitle());
    }

    public void addNotify() {
        super.addNotify();
        updateStyles();
    }

    private void updateStyles() {
        styleCombo.setEnabled(false);
        new ch.randelshofer.gui.SwingWorker() {
            public Object construct() {
                TreeMap<String, String> m = new TreeMap<String, String>();

                ZipIn zin = null;
                try {
                    zin = SCORMExporter.getSCORMTemplates();
                    for (ZipEntry entry = zin.getNextEntry(); entry != null; entry = zin.getNextEntry()) {
                        if (!entry.isDirectory()) {
                            String title = entry.getName();
                            int p = title.lastIndexOf('/');
                            if (p != -1) {
                                title = title.substring(p + 1);
                            }
                            if (title.startsWith("style_") && title.endsWith(".css")) {
                                title = title.substring(6, title.length() - 4);
                                m.put(title, entry.getName());
                            }
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                } finally {
                    if (zin != null) {
                        try {
                            zin.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                styledefs = new String[m.size()][2];
                int index = 0;
                for (Map.Entry<String, String> e : m.entrySet()) {
                    styledefs[index][0] = e.getKey();
                    styledefs[index][1] = e.getValue();
                    index++;
                }
                return m;
            }

            @Override
            public void finished() {
                styleMap = (Map<String, String>) getValue();

                DefaultComboBoxModel<String> cbm = new DefaultComboBoxModel<>();
                Object selectedItem = null;
                for (String[] def : styledefs) {
                    cbm.addElement(def[0]);
                    if (def[1].equals(prefs.get("scorm.style", "style/style_hslu.css"))) {
                        selectedItem = def[0];
                    }
                }
                cbm.setSelectedItem(selectedItem);
                styleCombo.setModel(cbm);
                styleCombo.setEnabled(true);
            }
        }.start();
    }

    public String getStylesheet() {
        return prefs.get("scorm.style", "style/style_hslu.css");
    }

    public String getTitle() {
        return titleField.getText();
    }

    public String getPrefix() {
        return prefixField.getText();
    }


    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        titleLabel = new javax.swing.JLabel();
        titleField = new javax.swing.JTextField();
        styleLabel = new javax.swing.JLabel();
        styleCombo = new javax.swing.JComboBox();
        prefixLabel = new javax.swing.JLabel();
        prefixField = new javax.swing.JTextField();
        localeLabel = new javax.swing.JLabel();
        localeCombo = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        titleLabel.setLabelFor(titleField);
        titleLabel.setText(labels.getString("exporter.scorm.title.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(titleLabel, gridBagConstraints);

        titleField.setColumns(12);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        add(titleField, gridBagConstraints);

        styleLabel.setLabelFor(styleCombo);
        styleLabel.setText(labels.getString("exporter.scorm.style.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        add(styleLabel, gridBagConstraints);

        styleCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stylePerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        add(styleCombo, gridBagConstraints);

        prefixLabel.setLabelFor(prefixField);
        prefixLabel.setText(labels.getString("exporter.scorm.prefix.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(prefixLabel, gridBagConstraints);

        prefixField.setColumns(6);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        add(prefixField, gridBagConstraints);

        localeLabel.setLabelFor(localeCombo);
        localeLabel.setText(labels.getString("exporter.scorm.language.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        add(localeLabel, gridBagConstraints);

        localeCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                localePerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        add(localeCombo, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void titleChanged() {
        prefs.put("scorm.title", titleField.getText());
        fileFilter.putClientProperty("title", titleField.getText());
    }

    private void prefixChanged() {
        prefs.put("scorm.prefix", prefixField.getText());
        fileFilter.putClientProperty("prefix", prefixField.getText());
    }

    private void localePerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_localePerformed
        prefs.put("scorm.locale", localeCombo.getSelectedItem().toString());
        fileFilter.putClientProperty("locale", localeCombo.getSelectedItem());
    }//GEN-LAST:event_localePerformed

    private void stylePerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stylePerformed
        prefs.put("scorm.style", styleMap.get(styleCombo.getSelectedItem()));
        System.out.println("SCORMExporterAccessyry scorm.style:" + styleMap.get(styleCombo.getSelectedItem()));
        fileFilter.putClientProperty("stylesheet", getStylesheet());
    }//GEN-LAST:event_stylePerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox localeCombo;
    private javax.swing.JLabel localeLabel;
    private javax.swing.JTextField prefixField;
    private javax.swing.JLabel prefixLabel;
    private javax.swing.JComboBox<String> styleCombo;
    private javax.swing.JLabel styleLabel;
    private javax.swing.JTextField titleField;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables

}
