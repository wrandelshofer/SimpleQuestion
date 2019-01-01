/*
 * @(#)ViewFactory.java 1.1  2001-10-05
 *
 * Copyright (c) 2001 Werner Randelshofer
 * Staldenmattweg 2, Immensee, CH-6405, Switzerland.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of 
 * Werner Randelshofer. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Werner Randelshofer.
 */

package ch.randelshofer.gui;

import java.awt.*;

/**
 * Defines the requirements for a viewer that displays an object. 
 *
 * @author  Werner Randelshofer
 * @version 1.1 2002-02-11 Renamed from Viewer to ViewFactory.
 * <br>1.0 2001-10-05
 */
public interface ViewFactory {
    /**
     * Sets the value of the viewer to value. 
     *
     * @param parent This is the component into which the viewer will be
     * embedded.
     * @param value This is the object to be displayed.
     */
    public Component getComponent(Component parent, Object value);
}

