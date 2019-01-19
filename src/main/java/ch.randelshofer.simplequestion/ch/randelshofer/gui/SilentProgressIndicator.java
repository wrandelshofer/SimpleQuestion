/* @(#)SilentProgressIndicator.java
 *
 * Copyright (c) 2005 Werner Randelshofer
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

import javax.swing.*;
/**
 * SilentProgressIndicator.
 *
 * @author  Werner Randelshofer
 * @version 1.0 August 1, 2005 Created.
 */
public class SilentProgressIndicator implements ProgressIndicator {
    private BoundedRangeModel model = new DefaultBoundedRangeModel();
    private boolean isClosed;
    private String note;
    /**
     * Creates a new instance.
     */
    public SilentProgressIndicator() {
    }
    
    public void close() {
    }
    
    public int getMaximum() {
        return model.getMaximum();
    }
    
    public int getMinimum() {
        return model.getMaximum();
    }
    
    public String getNote() {
        return note;
    }
    
    public int getProgress() {
        return model.getValue();
    }
    
    public boolean isCanceled() {
        return false;
    }
    
    public boolean isClosed() {
        return isClosed;
    }
    
    public void setIndeterminate(boolean newValue) {
    }
    
    public void setMaximum(int m) {
        model.setMaximum(m);
    }
    
    public void setMinimum(int m) {
        model.setMinimum(m);
    }
    
    public void setNote(String note) {
        this.note = note;
    }
    
    public void setProgress(int nv) {
        model.setValue(nv);
    }
    
}
