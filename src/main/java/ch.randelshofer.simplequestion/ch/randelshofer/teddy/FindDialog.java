/*
 * @(#)FindDialog.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

/*
 * FindDialog.java
 *
 * Created on 8. Oktober 2005, 13:25
 */

package ch.randelshofer.teddy;

import ch.randelshofer.teddy.regex.MatchType;
import ch.randelshofer.teddy.regex.Matcher;
import org.jhotdraw.app.Application;
import org.jhotdraw.undo.CompositeEdit;
import org.jhotdraw.util.ResourceBundleUtil;
import org.jhotdraw.util.prefs.PreferencesUtil;

import javax.swing.DefaultComboBoxModel;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultEditorKit;
import java.awt.Frame;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

/**
 * @author werni
 */
public class FindDialog extends javax.swing.JDialog {
    public final static long serialVersionUID = 1L;
    private Application app;
    private Matcher matcher;
    private Preferences prefs;
    private ResourceBundleUtil labels;

    /**
     * Creates new form FindDialog
     */
    private FindDialog(Frame parent, boolean modal) {
        super(parent, modal);
        labels = new ResourceBundleUtil(ResourceBundle.getBundle("ch.randelshofer.teddy.Labels"));
        initComponents();
        buttonPanel.setBorder(new EmptyBorder(0, -5, 0, -5));
        setAlwaysOnTop(true);
        prefs = PreferencesUtil.userNodeForPackage(getClass());

        PreferencesUtil.installFramePrefsHandler(prefs, "find", this);

        modeCombo.setModel(new DefaultComboBoxModel<String>(new String[]{
                labels.getString("find.contains.text"),
                labels.getString("find.startsWith.text"),
                labels.getString("find.word.text"),
        }));


        ignoreCaseCheck.setSelected(prefs.getBoolean("find.ignoreCase", true));
        wrapAroundCheck.setSelected(prefs.getBoolean("find.wrapAround", true));
        modeCombo.setSelectedIndex(Math.min(0, Math.max(modeCombo.getModel().getSize() - 1,
                prefs.getInt("find.mode", 0)
                ))
        );

        getRootPane().setDefaultButton(nextButton);

        InputMap im = new InputMap();

        LookAndFeel.loadKeyBindings(im, new String[]{
                "shift ENTER", DefaultEditorKit.insertBreakAction,
                "alt ENTER", DefaultEditorKit.insertBreakAction,
                "ENTER", JTextField.notifyAction,
        });

        im.setParent(findField.getInputMap(JComponent.WHEN_FOCUSED));
        findField.setInputMap(JComponent.WHEN_FOCUSED, im);
        im = new InputMap();
        LookAndFeel.loadKeyBindings(im, new String[]{
                "shift ENTER", DefaultEditorKit.insertBreakAction,
                "alt ENTER", DefaultEditorKit.insertBreakAction,
                "ENTER", JTextField.notifyAction,
        });
        im.setParent(replaceField.getInputMap(JComponent.WHEN_FOCUSED));
        replaceField.setInputMap(JComponent.WHEN_FOCUSED, im);
        pack();
    }

    /**
     * Creates new form FindDialog
     */
    public FindDialog(Application app) {
        this((Frame) SwingUtilities.getWindowAncestor(app.getComponent()), false);
        this.app = app;
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

        findLabel = new javax.swing.JLabel();
        replaceLabel = new javax.swing.JLabel();
        buttonPanel = new javax.swing.JPanel();
        replaceAllButton = new javax.swing.JButton();
        replaceButton = new javax.swing.JButton();
        replaceAndFindButton = new javax.swing.JButton();
        previousButton = new javax.swing.JButton();
        nextButton = new javax.swing.JButton();
        optionsPanel = new javax.swing.JPanel();
        ignoreCaseCheck = new javax.swing.JCheckBox();
        wrapAroundCheck = new javax.swing.JCheckBox();
        modeCombo = new javax.swing.JComboBox<>();
        findScrollPane = new javax.swing.JScrollPane();
        findField = new javax.swing.JTextArea();
        replaceScrollPane = new javax.swing.JScrollPane();
        replaceField = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Find");

        findLabel.setText(labels.getString("find.findLabel.text")); // NOI18N

        replaceLabel.setText(labels.getString("find.replaceWithLabel.text")); // NOI18N

        buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 5, 0));

        replaceAllButton.setText(labels.getString("find.replaceAll.text")); // NOI18N
        replaceAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceAll(evt);
            }
        });
        buttonPanel.add(replaceAllButton);

        replaceButton.setText(labels.getString("find.replace.text")); // NOI18N
        replaceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replace(evt);
            }
        });
        buttonPanel.add(replaceButton);

        replaceAndFindButton.setText(labels.getString("find.replaceAndFind.text")); // NOI18N
        replaceAndFindButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceAndFind(evt);
            }
        });
        buttonPanel.add(replaceAndFindButton);

        previousButton.setText(labels.getString("find.previous.text")); // NOI18N
        previousButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previous(evt);
            }
        });
        buttonPanel.add(previousButton);

        nextButton.setText(labels.getString("find.next.text")); // NOI18N
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                next(evt);
            }
        });
        buttonPanel.add(nextButton);

        optionsPanel.setLayout(new java.awt.GridBagLayout());

        ignoreCaseCheck.setText(labels.getString("find.ignoreCase.text")); // NOI18N
        ignoreCaseCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ignoreCasePerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        optionsPanel.add(ignoreCaseCheck, gridBagConstraints);

        wrapAroundCheck.setText(labels.getString("find.wrapAround.text")); // NOI18N
        wrapAroundCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wrapAroundPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        optionsPanel.add(wrapAroundCheck, gridBagConstraints);

        modeCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Contains", "Starts with", "Word"}));
        modeCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                modeChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        optionsPanel.add(modeCombo, gridBagConstraints);

        findField.setLineWrap(true);
        findField.setRows(2);
        findScrollPane.setViewportView(findField);

        replaceField.setLineWrap(true);
        replaceField.setRows(2);
        replaceScrollPane.setViewportView(replaceField);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(buttonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(11, 11, 11)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addComponent(replaceLabel)
                                                        .addComponent(findLabel))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(optionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(replaceScrollPane)
                                                        .addComponent(findScrollPane))))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(findLabel)
                                        .addComponent(findScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(replaceLabel)
                                        .addComponent(replaceScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(optionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(14, 14, 14)
                                .addComponent(buttonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(30, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void modeChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_modeChanged
        if (prefs != null) {
            prefs.putInt("find.mode", modeCombo.getSelectedIndex());
        }
    }//GEN-LAST:event_modeChanged

    private void wrapAroundPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wrapAroundPerformed
        if (prefs != null) {
            prefs.putBoolean("find.wrapAround", wrapAroundCheck.isSelected());
        }

    }//GEN-LAST:event_wrapAroundPerformed

    private void ignoreCasePerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ignoreCasePerformed
        if (prefs != null) {
            prefs.putBoolean("find.ignoreCase", ignoreCaseCheck.isSelected());
        }
    }//GEN-LAST:event_ignoreCasePerformed

    private void replace(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replace
        TeddyView view = ((TeddyView) app.getActiveView());
        if (view != null) {
            CompositeEdit edit = new CompositeEdit("Replace");
            view.fireEdit(edit);
            view.replaceRange(

                    replaceField.getText(),
                    view.getSelectionStart(),
                    view.getSelectionEnd()
            );
            view.fireEdit(edit);
        }
    }//GEN-LAST:event_replace

    private void replaceAndFind(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceAndFind
        TeddyView view = ((TeddyView) app.getActiveView());
        if (view != null) {
            CompositeEdit edit = new CompositeEdit("Replace And Find");
            view.fireEdit(edit);
            view.replaceRange(
                    replaceField.getText(),
                    view.getSelectionStart(),
                    view.getSelectionEnd()
            );
            next(evt);
            view.fireEdit(edit);
        }
    }//GEN-LAST:event_replaceAndFind

    private void previous(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previous
        TeddyView view = ((TeddyView) app.getActiveView());
        if (view != null) {
            updateMatcher();
            if (matcher != null) {
                matcher.setStartIndex(view.getSelectionStart() - 1);
                int pos = matcher.findPrevious();
                if (pos == -1 && wrapAroundCheck.isSelected()) {
                    pos = matcher.findPrevious(view.getDocument().getLength());
                }
                if (pos == -1) {
                    getToolkit().beep();
                } else {
                    view.select(pos, matcher.getFindString().length() + pos);
                }
            }
        }
    }//GEN-LAST:event_previous

    private void next(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_next
        TeddyView view = ((TeddyView) app.getActiveView());
        if (view != null) {
            updateMatcher();
            if (matcher != null) {
                int pos = matcher.findNext(view.getSelectionEnd());
                if (pos == -1 && wrapAroundCheck.isSelected()) {
                    pos = matcher.findNext(0);
                }
                if (pos == -1) {
                    getToolkit().beep();
                } else {
                    view.select(pos, matcher.getFindString().length() + pos);
                }
            }
        }
    }//GEN-LAST:event_next

    private void replaceAll(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceAll
        TeddyView view = ((TeddyView) app.getActiveView());
        if (view != null) {
            updateMatcher();
            if (matcher != null) {
                int pos = matcher.findNext((wrapAroundCheck.isSelected()) ? 0 : view.getSelectionEnd());
                if (pos == -1) {
                    getToolkit().beep();
                } else {
                    CompositeEdit edit = new CompositeEdit("Replace All");
                    view.fireEdit(edit);
                    view.select(pos, matcher.getFindString().length() + pos);
                    do {
                        view.replaceRange(
                                replaceField.getText(),
                                pos,
                                pos + matcher.getFindString().length()
                        );
                        pos = matcher.findNext(pos + replaceField.getText().length());
                    } while (pos != -1);
                    view.fireEdit(edit);
                }
            }
        }
    }//GEN-LAST:event_replaceAll

    private void updateMatcher() {
        TeddyView view = ((TeddyView) app.getActiveView());
        if (view != null) {
            MatchType matchType;
            switch (modeCombo.getSelectedIndex()) {
                case 0:
                    matchType = MatchType.CONTAINS;
                    break;
                case 1:
                    matchType = MatchType.STARTS_WITH;
                    break;
                case 2:
                default:
                    matchType = MatchType.FULL_WORD;
                    break;
            }
            matcher = new Matcher(view.getDocument(),
                    findField.getText(),
                    !ignoreCaseCheck.isSelected(),
                    matchType
            );
        } else {
            matcher = null;
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JPanel buttonPanel;
    public javax.swing.JTextArea findField;
    public javax.swing.JLabel findLabel;
    public javax.swing.JScrollPane findScrollPane;
    public javax.swing.JCheckBox ignoreCaseCheck;
    public javax.swing.JComboBox<String> modeCombo;
    public javax.swing.JButton nextButton;
    public javax.swing.JPanel optionsPanel;
    public javax.swing.JButton previousButton;
    public javax.swing.JButton replaceAllButton;
    public javax.swing.JButton replaceAndFindButton;
    public javax.swing.JButton replaceButton;
    public javax.swing.JTextArea replaceField;
    public javax.swing.JLabel replaceLabel;
    public javax.swing.JScrollPane replaceScrollPane;
    public javax.swing.JCheckBox wrapAroundCheck;
    // End of variables declaration//GEN-END:variables

}
