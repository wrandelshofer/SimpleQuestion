/*
 * @(#)VerifySyntaxAction.java  1.0  11. Mai 2006
 *
 * Copyright (c) 2008 Werner Randelshofer
 * Staldenmattweg 2, CH-6405 Immensee, Switzerland
 * All rights reserved.
 *
 * The copyright of this software is owned by Werner Randelshofer. 
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * Werner Randelshofer. For details see accompanying license terms.
 */

package ch.randelshofer.simplequestion.action;

import ch.randelshofer.simplequestion.SimpleQuestionView;
import java.awt.event.*;
import java.util.ResourceBundle;
import javax.swing.*;
import org.jhotdraw.app.Application;
import org.jhotdraw.app.View;
import org.jhotdraw.app.action.AbstractViewAction;
import org.jhotdraw.util.ResourceBundleUtil;
/**
 * VerifySyntaxAction.
 *
 * @author Werner Randelshofer
 * @version 1.0 11. Mai 2006 Created.
 */
public class VerifySyntaxAction extends AbstractViewAction {
    public final static String ID = "verifySyntax";
    private ResourceBundleUtil labels = new ResourceBundleUtil(ResourceBundle.getBundle("ch.randelshofer.simplequestion.Labels"));
    
    /** Creates a new instance. */
    public VerifySyntaxAction(Application app, View v) {
        super(app,v);
        labels.configureAction(this, ID);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ((SimpleQuestionView) getActiveView()).verifySyntax();
    }
    
}
