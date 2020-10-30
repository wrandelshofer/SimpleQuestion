/* @(#)ConfigurableFileFilterAccessory.java
 * Copyright © Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.io;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * ConfigurableFileFilterAccessory changes its content depending on the
 * ConfigurableFileFilter currently in effect on a JFileChooser.
 *
 * @author Werner Randelshofer
 * @version 1.0 2. August 2006 Created.
 */
public class ConfigurableFileFilterAccessory extends JPanel {
    static final long serialVersionUID = 1L;
    private JFileChooser fileChooser;
    private PropertyChangeListener propertyHandler = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            String name = evt.getPropertyName();
            if (name.equals("fileFilterChanged")) {
                updateAccessory();
            }
        }
    };

    /**
     * Creates new form.
     */
    public ConfigurableFileFilterAccessory() {
        initComponents();
    }

    public ConfigurableFileFilterAccessory(JFileChooser c) {
        initComponents();
        setFileChooser(c);
    }

    public void setFileChooser(JFileChooser newValue) {
        JFileChooser oldValue = fileChooser;
        if (oldValue != null) {
            oldValue.removePropertyChangeListener(propertyHandler);
        }

        fileChooser = newValue;
        if (newValue != null) {
            newValue.addPropertyChangeListener(propertyHandler);
            updateAccessory();
        }
    }

    /**
     * Updates the accessory.
     * This method is invoked, when the JFileChooser changes, or when one of
     * the properties of a JFileChooser change.
     */
    protected void updateAccessory() {
        if (fileChooser != null) {
            FileFilter ff = fileChooser.getFileFilter();
            JComponent accessory = (ff == null || !(ff instanceof ConfigurableFileFilter)) ?
                    null :
                    ((ConfigurableFileFilter) ff).getAccessory();
            if (accessory == null) {
                removeAll();
                invalidate();
                fileChooser.validate();
            } else {
                if (getComponentCount() == 0) {
                    add(accessory);
                    invalidate();
                    fileChooser.validate();
                } else if (getComponent(0) != accessory) {
                    removeAll();
                    add(accessory);
                    invalidate();
                    fileChooser.validate();
                }
            }
        }
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}
