/*
 * @(#)ViewFactory.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.gui;

import java.awt.Component;

/**
 * Defines the requirements for a viewer that displays an object.
 *
 * @author Werner Randelshofer
 * @version 1.1 2002-02-11 Renamed from Viewer to ViewFactory.
 * <br>1.0 2001-10-05
 */
public interface ViewFactory {
    /**
     * Sets the value of the viewer to value.
     *
     * @param parent This is the component into which the viewer will be
     *               embedded.
     * @param value  This is the object to be displayed.
     */
    public Component getComponent(Component parent, Object value);
}

