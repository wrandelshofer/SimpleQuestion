/*
 * @(#)DirectoryFileFilter.java  1.0  26. Juli 2006
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

import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.util.*;
/**
 * Filters all files except directories.
 *
 * @author Werner Randelshofer
 * @version 1.0 26. Juli 2006 Created.
 */
public class DirectoryFileFilter extends ConfigurableFileFilter {
    private String description;
    private JComponent accessory;
    private HashMap<Object,Object> clientProperties;
    
    /**
     * Creates a new instance.
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
            clientProperties = new HashMap<Object,Object>();
        }
        clientProperties.put(key, value);
    }
}
