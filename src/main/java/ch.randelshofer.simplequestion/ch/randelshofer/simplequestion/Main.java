/*
 * @(#)Main.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
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
 */
public class Main {

    public final static String NAME = "SimpleQuestion";
    public final static String COPYRIGHT = "© 2009-2020 by Werner Randelshofer, MIT License";

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

