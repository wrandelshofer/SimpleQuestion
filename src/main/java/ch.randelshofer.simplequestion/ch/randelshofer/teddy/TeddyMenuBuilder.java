/* @(#)SimpleQuestionMenuBuilder.java
 *
 * Copyright (c) 2012 Werner Randelshofer, Immensee, Switzerland.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package ch.randelshofer.teddy;

import ch.randelshofer.teddy.action.ToggleLineNumbersAction;
import ch.randelshofer.teddy.action.ToggleLineWrapAction;
import ch.randelshofer.teddy.action.ToggleStatusBarAction;
import org.jhotdraw.app.Application;
import org.jhotdraw.app.DefaultMenuBuilder;
import org.jhotdraw.app.View;

import javax.swing.ActionMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;

/**
 * {@code SimpleQuestionMenuBuilder}.
 *
 * @author Werner Randelshofer
 * @version 1.0 2012-04-06 Created.
 */
public class TeddyMenuBuilder extends DefaultMenuBuilder {

    @Override
    public void addOtherEditItems(JMenu m, Application app, View v) {
        super.addOtherEditItems(m, app, v);

        ActionMap am = app.getActionMap(v);

    }

    @Override
    public void addOtherViewItems(JMenu m, Application app, View v) {
        super.addOtherViewItems(m, app, v);

        JCheckBoxMenuItem cbmi;

        ActionMap am = app.getActionMap(v);

        cbmi = new JCheckBoxMenuItem(am.get(ToggleLineWrapAction.ID));
        cbmi.setAction(am.get(ToggleLineWrapAction.ID));
        m.add(cbmi);
        cbmi = new JCheckBoxMenuItem(am.get(ToggleLineNumbersAction.ID));
        cbmi.setAction(am.get(ToggleLineNumbersAction.ID));
        m.add(cbmi);
        cbmi = new JCheckBoxMenuItem(am.get(ToggleStatusBarAction.ID));
        cbmi.setAction(am.get(ToggleStatusBarAction.ID));
        m.add(cbmi);
    }

}
