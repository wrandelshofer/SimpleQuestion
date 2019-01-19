/* @(#)Main.java
 *
 * Copyright (c) 2009-2011 Werner Randelshofer
 * Staldenmattweg 2, Immensee, CH-6405, Switzerland.
 * All rights reserved.
 *
 * The copyright of this software is owned by Werner Randelshofer.
 * You may not use, copy or modify this software, except in
 * accordance with the license agreement you entered into with
 * Werner Randelshofer. For details see accompanying license terms.
 */

package ch.randelshofer.simplequestion;

import org.jhotdraw.app.Application;
import org.jhotdraw.app.DefaultApplicationModel;
import org.jhotdraw.app.OSXApplication;
import org.jhotdraw.app.SDIApplication;

/**
 * Main.
 *
 * @author Werner Randelshofer
 * @version 1.0.1 2011-06-06 It is 2011 now.
 * <br>1.0 2009-09-06 Created.
 */
public class Main {

    public final static String NAME = "SimpleQuestion";
    public final static String COPYRIGHT = "© 2009-2011 by Werner Randelshofer";

    /**
     * Launches the application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //ResourceBundleUtil.setVerbose(true);

        DefaultApplicationModel tam = new SimpleQuestionApplicationModel();
        tam.setCopyright(COPYRIGHT);
        tam.setName(NAME);
        tam.setViewFactory(ch.randelshofer.simplequestion.SimpleQuestionView::new);
        tam.setVersion(Main.class.getPackage().getImplementationVersion());

        Application app;
        if (System.getProperty("os.name").toLowerCase().startsWith("mac os x")) {
            app = new OSXApplication();
        } else if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
            //app = new DefaultMDIApplication();
            app = new SDIApplication();
        } else {
            app = new SDIApplication();
        }
        app.setModel(tam);
        app.launch(args);
    }
}

