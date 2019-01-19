/* @(#)VerifySyntaxAction.java
 * Copyright © Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.simplequestion.action;

import ch.randelshofer.simplequestion.SimpleQuestionView;
import org.jhotdraw.app.Application;
import org.jhotdraw.app.View;
import org.jhotdraw.app.action.AbstractViewAction;
import org.jhotdraw.util.ResourceBundleUtil;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

/**
 * VerifySyntaxAction.
 *
 * @author Werner Randelshofer
 * @version 1.0 11. Mai 2006 Created.
 */
public class VerifySyntaxAction extends AbstractViewAction {
    public final static long serialVersionUID = 1L;
    public final static String ID = "verifySyntax";
    private ResourceBundleUtil labels = new ResourceBundleUtil(ResourceBundle.getBundle("ch.randelshofer.simplequestion.Labels"));

    /**
     * Creates a new instance.
     */
    public VerifySyntaxAction(Application app, View v) {
        super(app, v);
        labels.configureAction(this, ID);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ((SimpleQuestionView) getActiveView()).verifySyntax();
    }

}
