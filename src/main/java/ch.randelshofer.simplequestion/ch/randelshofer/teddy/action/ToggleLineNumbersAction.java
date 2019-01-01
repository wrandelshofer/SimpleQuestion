/*
 * @(#)ToggleLineNumbersAction.java
 *
 * Copyright (c) 2005 by the original authors of JHotDraw
 * and all its contributors.
 * All rights reserved.
 *
 * The copyright of this software is owned by the authors and
 * contributors of the JHotDraw project ("the copyright holders").
 * You may not use, copy or modify this software, except in
 * accordance with the license agreement you entered into with
 * the copyright holders. For details see accompanying license terms.
 */

package ch.randelshofer.teddy.action;

import org.jhotdraw.app.*;
import org.jhotdraw.app.action.*;
import ch.randelshofer.teddy.*;
import org.jhotdraw.util.*;
import java.awt.event.*;
import java.util.ResourceBundle;
import javax.swing.Action;

/**
 * ToggleLineNumbersAction.
 *
 * @author  Werner Randelshofer
 * @version $Id: ToggleLineNumbersAction.java 527 2009-06-07 14:28:19Z rawcoder $
 */
public class ToggleLineNumbersAction extends AbstractViewAction {
    public final static String ID = "view.toggleLineNumbers";
    private ResourceBundleUtil labels = new ResourceBundleUtil(ResourceBundle.getBundle("ch.randelshofer.teddy.Labels"));
    
    /**
     * Creates a new instance.
     */
    public ToggleLineNumbersAction(Application app, View v) {
        super(app,v);
        labels.configureAction(this, ID);
        setPropertyName("lineNumbersVisible");
    }
    
    @Override
    public TeddyView getActiveView() {
        return (TeddyView) super.getActiveView();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        getActiveView().setLineNumbersVisible(! getActiveView().isLineNumbersVisible());
    }
    
    
    @Override
    protected void updateView() {
        putValue(
                Action.SELECTED_KEY,
                getActiveView() != null && getActiveView().isLineNumbersVisible()
                );
    }
}
