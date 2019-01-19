/* @(#)ConfigurableFileFilter.java
 * Copyright © Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.io;

import java.beans.*;
import javax.swing.*;
import javax.swing.filechooser.*;

/**
 * FileFilters which can be configured implement this interface.
 * In order to work, the ConfigurableFileFilterAccessory must be set on
 * the JFileChooser.
 *
 * @see ConfigurableFileFilterAccessory
 * 
 * @author Werner Randelshofer
 * @version 1.0 2. August 2006 Created.
 */
public abstract class ConfigurableFileFilter extends FileFilter {
    /**
     * Returns an accessory for the JFileChooser.
     *
     * @return The accessory or null, if the file filter does not have one.
     */
    public abstract JComponent getAccessory();
    /**
     * Sets an accessory for the JFileChooser.
     *
     * @param acc The accessory or null, if the file filter shall not have an accessory.
     */
    public abstract void setAccessory(JComponent acc);

    /**
     * Returns the value of the property with the specified key.  Only
     * properties added with <code>putClientProperty</code> will return
     * a non-<code>null</code> value.  
     * 
     * @param key the being queried
     * @return the value of this property or <code>null</code>
     * @see #putClientProperty
     */
    public abstract Object getClientProperty(Object key);
    
    /**
     * Adds an arbitrary key/value "client property" to this component.
     * <p>
     * 
     * @param key the new client property key
     */
    public abstract void putClientProperty(Object key, Object value);
}
