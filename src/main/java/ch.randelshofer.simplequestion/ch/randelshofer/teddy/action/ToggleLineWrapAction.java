/* @(#)ToggleLineWrapAction.java
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

import ch.randelshofer.teddy.TeddyView;
import org.jhotdraw.app.Application;
import org.jhotdraw.app.View;
import org.jhotdraw.app.action.AbstractViewAction;
import org.jhotdraw.util.ResourceBundleUtil;

import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

/**
 * ToggleLineWrapAction.
 *
 * @author Werner Randelshofer
 * @version $Id: ToggleLineWrapAction.java 527 2009-06-07 14:28:19Z rawcoder $
 */
public class ToggleLineWrapAction extends AbstractViewAction {
    public final static long serialVersionUID = 1L;
    public final static String ID = "view.toggleLineWrap";
    private ResourceBundleUtil labels = new ResourceBundleUtil(ResourceBundle.getBundle("ch.randelshofer.teddy.Labels"));

    /**
     * Creates a new instance.
     */
    public ToggleLineWrapAction(Application app, View v) {
        super(app, v);
        labels.configureAction(this, ID);
        setPropertyName("lineWrap");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        getActiveView().setLineWrap(!getActiveView().isLineWrap());
    }

    @Override
    public TeddyView getActiveView() {
        return (TeddyView) super.getActiveView();
    }

    @Override
    protected void updateView() {
        putValue(
                Action.SELECTED_KEY,
                getActiveView() != null && getActiveView().isLineWrap()
        );
    }
}
