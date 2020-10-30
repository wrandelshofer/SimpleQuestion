/*
 * @(#)DirectoryFileFilter.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.io;

import javax.swing.JComponent;
import java.io.File;
import java.util.HashMap;

/**
 * Filters all files except directories.
 *
 * @author Werner Randelshofer
 * @version 1.0 26. Juli 2006 Created.
 */
public class DirectoryFileFilter extends ConfigurableFileFilter {
    private String description;
    private JComponent accessory;
    private HashMap<Object, Object> clientProperties;

    /**
     * Creates a new instance.
     *
     * @param description A human readable description.
     */
    public DirectoryFileFilter(String description) {
        this.description = description;
    }

    public boolean accept(File pathname) {
        if (pathname.isDirectory()) {
            return true;
        } else {
            return false;
        }
    }

    public String getDescription() {
        return description;
    }

    public void setAccessory(JComponent c) {
        accessory = c;
    }

    public JComponent getAccessory() {
        return accessory;
    }

    public Object getClientProperty(Object key) {
        return (clientProperties == null) ?
                null :
                clientProperties.get(key);
    }

    public void putClientProperty(Object key, Object value) {
        if (clientProperties == null) {
            clientProperties = new HashMap<Object, Object>();
        }
        clientProperties.put(key, value);
    }
}
