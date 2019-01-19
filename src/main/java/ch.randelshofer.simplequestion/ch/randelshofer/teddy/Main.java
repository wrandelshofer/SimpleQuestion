/* @(#)Main.java
 *
 * Copyright (c) 1996-2007 by the original authors of JHotDraw
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

import org.jhotdraw.app.*;

/**
 * Main class.
 *
 * @author Werner Randelshofer.
 * @version $Id: Main.java 527 2009-06-07 14:28:19Z rawcoder $
 */
public class Main {
    public final static String NAME = "JHotDraw Teddy";
    public final static String COPYRIGHT = "© 1996-2009 by the original authors of JHotDraw and all its contributors";
    
    /**
     * Launches the application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        TeddyApplicationModel tam = new TeddyApplicationModel();
        tam.setCopyright(COPYRIGHT);
        tam.setName(NAME);
        tam.setViewFactory(ch.randelshofer.teddy.TeddyView::new);
        tam.setVersion(Main.class.getPackage().getImplementationVersion());
        
        Application app;
        if (System.getProperty("os.name").toLowerCase().startsWith("mac os x")) {
            app = new OSXApplication();
        } else if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
            app = new MDIApplication();
        } else {
            app = new SDIApplication();
        }
        app.setModel(tam);
        app.launch(args);
    }
}
