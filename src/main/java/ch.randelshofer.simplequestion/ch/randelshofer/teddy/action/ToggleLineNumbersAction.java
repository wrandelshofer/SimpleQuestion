/*
 * @(#)ToggleLineNumbersAction.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
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
 * ToggleLineNumbersAction.
 *
 * @author Werner Randelshofer
 * @version $Id: ToggleLineNumbersAction.java 527 2009-06-07 14:28:19Z rawcoder $
 */
public class ToggleLineNumbersAction extends AbstractViewAction {
    public final static long serialVersionUID = 1L;
    public final static String ID = "view.toggleLineNumbers";
    private ResourceBundleUtil labels = new ResourceBundleUtil(ResourceBundle.getBundle("ch.randelshofer.teddy.Labels"));

    /**
     * Creates a new instance.
     */
    public ToggleLineNumbersAction(Application app, View v) {
        super(app, v);
        labels.configureAction(this, ID);
        setPropertyName("lineNumbersVisible");
    }

    @Override
    public TeddyView getActiveView() {
        return (TeddyView) super.getActiveView();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        getActiveView().setLineNumbersVisible(!getActiveView().isLineNumbersVisible());
    }


    @Override
    protected void updateView() {
        putValue(
                Action.SELECTED_KEY,
                getActiveView() != null && getActiveView().isLineNumbersVisible()
        );
    }
}
