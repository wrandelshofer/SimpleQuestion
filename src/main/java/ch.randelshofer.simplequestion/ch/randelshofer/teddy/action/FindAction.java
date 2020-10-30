/*
 * @(#)FindAction.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.teddy.action;

import ch.randelshofer.teddy.FindDialog;
import org.jhotdraw.app.Application;
import org.jhotdraw.app.OSXApplication;
import org.jhotdraw.app.View;
import org.jhotdraw.app.action.edit.AbstractFindAction;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * FindAction shows the find dialog.
 *
 * @author Werner Randelshofer
 * @version $Id: FindAction.java 527 2009-06-07 14:28:19Z rawcoder $
 */
public class FindAction extends AbstractFindAction {
    public final static long serialVersionUID = 1L;
    private FindDialog findDialog;

    /**
     * Creates a new instance.
     */
    public FindAction(Application app, View v) {
        super(app, v);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (findDialog == null) {
            findDialog = new FindDialog(getApplication());
            if (getApplication() instanceof OSXApplication) {
                findDialog.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent evt) {
                        if (findDialog != null) {
                            ((OSXApplication) getApplication()).removePalette(findDialog);
                            findDialog.setVisible(false);
                        }
                    }
                });
            }
        }
        findDialog.setVisible(true);
        if (getApplication() instanceof OSXApplication) {
            ((OSXApplication) getApplication()).addPalette(findDialog);
        }
    }
}
