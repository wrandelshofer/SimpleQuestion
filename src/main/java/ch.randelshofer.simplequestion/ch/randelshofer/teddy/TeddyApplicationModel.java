/*
 * @(#)TeddyApplicationModel.java
 *
 * Copyright (c) 2007-2008 by the original authors of JHotDraw
 * and all its contributors.
 * All rights reserved.
 *
 * The copyright of this software is owned by the authors and  
 * contributors of the JHotDraw project ("the copyright holders").  
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * the copyright holders. For details see accompanying license terms. 
 */

package ch.randelshofer.teddy;

import javax.swing.*;
import org.jhotdraw.app.*;
import java.util.*;
import org.jhotdraw.app.action.*;
import ch.randelshofer.teddy.action.*;
import org.jhotdraw.gui.JFileURIChooser;
import org.jhotdraw.gui.URIChooser;
import org.jhotdraw.util.*;

/**
 * TeddyApplicationModel.
 *
 * @author Werner Randelshofer
 * @version $Id: TeddyApplicationModel.java 527 2009-06-07 14:28:19Z rawcoder $
 */
public class TeddyApplicationModel extends DefaultApplicationModel {
    
    /** Creates a new instance. */
    public TeddyApplicationModel() {
    }
    
    @Override
    public ActionMap createActionMap(Application a, View v) {
        ActionMap m = super.createActionMap(a, v);
        ResourceBundleUtil drawLabels = new ResourceBundleUtil(ResourceBundle.getBundle("org.jhotdraw.draw.Labels"));
        ResourceBundleUtil labels = new ResourceBundleUtil(ResourceBundle.getBundle("org.jhotdraw.samples.svg.Labels"));
        AbstractAction aa;
        m.put(ch.randelshofer.teddy.action.FindAction.ID, new ch.randelshofer.teddy.action.FindAction(a,v));
        m.put(ToggleLineWrapAction.ID, new ToggleLineWrapAction(a,v));
        m.put(ToggleStatusBarAction.ID, new ToggleStatusBarAction(a,v));
        m.put(ToggleLineNumbersAction.ID, new ToggleLineNumbersAction(a,v));
        return m;
    }
    
    @Override public void initView(Application a, View p) {
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
