/*
 * @(#)TeddyApplicationModel.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.teddy;

import ch.randelshofer.teddy.action.ToggleLineNumbersAction;
import ch.randelshofer.teddy.action.ToggleLineWrapAction;
import ch.randelshofer.teddy.action.ToggleStatusBarAction;
import org.jhotdraw.app.Application;
import org.jhotdraw.app.DefaultApplicationModel;
import org.jhotdraw.app.MenuBuilder;
import org.jhotdraw.app.View;
import org.jhotdraw.gui.JFileURIChooser;
import org.jhotdraw.gui.URIChooser;
import org.jhotdraw.util.ResourceBundleUtil;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.JToolBar;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

/**
 * TeddyApplicationModel.
 *
 * @author Werner Randelshofer
 * @version $Id: TeddyApplicationModel.java 527 2009-06-07 14:28:19Z rawcoder $
 */
public class TeddyApplicationModel extends DefaultApplicationModel {
    public final static long serialVersionUID = 1L;

    /**
     * Creates a new instance.
     */
    public TeddyApplicationModel() {
    }

    @Override
    public ActionMap createActionMap(Application a, View v) {
        ActionMap m = super.createActionMap(a, v);
        ResourceBundleUtil drawLabels = new ResourceBundleUtil(ResourceBundle.getBundle("org.jhotdraw.draw.Labels"));
        ResourceBundleUtil labels = new ResourceBundleUtil(ResourceBundle.getBundle("org.jhotdraw.samples.svg.Labels"));
        AbstractAction aa;
        m.put(ch.randelshofer.teddy.action.FindAction.ID, new ch.randelshofer.teddy.action.FindAction(a, v));
        m.put(ToggleLineWrapAction.ID, new ToggleLineWrapAction(a, v));
        m.put(ToggleStatusBarAction.ID, new ToggleStatusBarAction(a, v));
        m.put(ToggleLineNumbersAction.ID, new ToggleLineNumbersAction(a, v));
        return m;
    }

    @Override
    public void initView(Application a, View p) {
    }

    @Override
    protected MenuBuilder createMenuBuilder() {
        return new TeddyMenuBuilder();
    }


    /**
     * Creates toolbars for the application.
     * This class returns an empty list - we don't want toolbars in a text editor.
     */
    @Override
    public List<JToolBar> createToolBars(Application app, View p) {
        return Collections.emptyList();
    }

    @Override
    public URIChooser createOpenChooser(Application a, View v) {
        JFileURIChooser c = new JFileURIChooser();
        c.setAccessory(new CharacterSetAccessory());
        return c;
    }

    @Override
    public URIChooser createSaveChooser(Application a, View v) {
        JFileURIChooser c = new JFileURIChooser();
        c.setAccessory(new CharacterSetAccessory());
        return c;
    }
}
