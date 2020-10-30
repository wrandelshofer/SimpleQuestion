/* @(#)EventWorker.java
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
package ch.randelshofer.gui.event;

import javax.swing.SwingUtilities;
import java.awt.ActiveEvent;

/**
 * This is an abstract class that you subclass to
 * perform GUI-related work in a dedicated event dispatcher.
 * <p>
 * This class is compatible with SwingWorker where it is reasonable
 * to be so. Unlike a SwingWorker it does not use an internal
 * worker thread but has to be dispatched by a dispatcher which
 * handles java.awt.ActiveEvent's.
 *
 * @author Werner Randelshofer
 * @version 1.1.1 2001-08-24 Call finished() within finally block.
 * <br>1.1 2001-08-24 Reworked for JDK 1.3.
 * <br>1.0 1998-10-07 Created.
 */
public abstract class EventWorker implements ActiveEvent {
    private Object value;  // see getValue(), setValue()

    /**
     * Calls #construct on the current thread and invokes
     * #finished on the AWT event dispatcher thread.
     */
    public void dispatch() {
        final Runnable doFinished = new Runnable() {
            public void run() {
                finished();
            }
        };
        try {
            setValue(construct());
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            SwingUtilities.invokeLater(doFinished);
        }
    }

    /**
     * Compute the value to be returned by the <code>get</code> method.
     */
    public abstract Object construct();

    /**
     * Called on the event dispatching thread (not on the worker thread)
     * after the <code>construct</code> method has returned.
     */
    public void finished() {
    }

    /**
     * Get the value produced by the worker thread, or null if it
     * hasn't been constructed yet.
     */
    protected synchronized Object getValue() {
        return value;
    }

    /**
     * Set the value produced by worker thread
     */
    private synchronized void setValue(Object x) {
        value = x;
    }
}