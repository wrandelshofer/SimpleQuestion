/*
 * @(#)ConfigurableFileFilter.java  1.0  2. August 2006
 *
 * Copyright (c) 2008 Werner Randelshofer
 * Staldenmattweg 2, CH-6405 Immensee, Switzerland
 * All rights reserved.
 *
 * The copyright of this software is owned by Werner Randelshofer. 
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * Werner Randelshofer. For details see accompanying license terms.
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
