/*
 * @(#)SettingsAction.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.simplequestion.action;

import ch.randelshofer.simplequestion.SimpleQuestionView;
import org.jhotdraw.app.Application;
import org.jhotdraw.app.action.app.AbstractPreferencesAction;
import org.jhotdraw.util.prefs.PreferencesUtil;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;

/**
 * SettingsAction.
 *
 * @author Werner Randelshofer
 * @version 1.0 2009-09-07 Created.
 */
public class SettingsAction extends AbstractPreferencesAction {
    public final static long serialVersionUID = 1L;
    private SettingsDialog dialog;

    public SettingsAction(Application app) {
        super(app);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SettingsDialog d = getDialog(e);
        if (!d.isShowing()) {
            d.setVisible(true);
        } else {
            d.requestFocus();
        }
    }

    private SettingsDialog getDialog(ActionEvent e) {
        if (dialog == null) {
            Component parent;
            if (e != null && (e.getSource() instanceof Component)) {
                parent = (Component) e.getSource();
            } else {
                parent = getApplication().getComponent();
            }
            dialog = new SettingsDialog(//
                    (JFrame) (parent == null ? null : SwingUtilities.getWindowAncestor(parent)),
                    false
            );
            Preferences prefs = Preferences.userNodeForPackage(SimpleQuestionView.class);
            PreferencesUtil.installFramePrefsHandler(prefs, "settings", dialog);
        }
        return dialog;
    }
}
