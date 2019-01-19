/* @(#)TinyLMSApp.java
 *
 * Copyright (c) 2005-2006 Werner Randelshofer
 * Staldenmattweg 2, Immensee, CH-6405, Switzerland.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Werner Randelshofer. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Werner Randelshofer.
 */

package ch.randelshofer.scorm;

import java.lang.reflect.*;
import java.io.*;
/**
 * Launcher for the TinyLMS application.
 * If the launcher is invoked without command line parameters, it will start
 * the graphical user interface of TinyLMS: the CourseBuilder.
 * If it is invoked with command line parameters, it will start the batch
 * processor: BatchProcessor.
 *
 * @author  Werner Randelshofer
 * @version 1.5 2006-05-26 Read version from a file.
 * <br>1.4.1 2006-05-06 Reworked.
 * <br>1.0 August 2, 2005 Created.
 */
public class TinyLMSApp {
    private static String version;
    public static String getVersion() {
        if (version == null) {
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(
                        TinyLMSApp.class.getResourceAsStream("version.txt"),
                        "UTF8"));
                version = in.readLine();
            } catch (Throwable e) {
                version = "unknown";
                System.err.println("Warning: TinyLMSApp couldn't find resource \"ch/randelshofer/scorm/version.txt\".");
            } finally {
                if (in != null) try {in.close();} catch (IOException e2) {}
            }
        }
        return version;
    }
    /**
     * Creates a new instance.
     */
    public TinyLMSApp() {
    }
    
    public static void main(String[] args) {
        /*
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.exit(0);
        }*/
        
        // The following code uses reflection to invoke the main method of
        // class CourseBuilder or class BatchProcessor.
        // Reflection is used to avoid unnecessarily loading lots of UI classes
        // just for a batch invocation of TinyLMS.
        
        try {
            Class<?> c;
            if (args.length == 0) {
                c = Class.forName("ch.randelshofer.scorm.CourseBuilder");
                //CourseBuilder.main(args);
            } else {
                c = Class.forName("ch.randelshofer.scorm.BatchProcessor");
                //BatchProcessor.main(args);
            }
            Method m = c.getMethod("main", String[].class);
            m.invoke(null, new Object[] {args});
        } catch (InvocationTargetException e) {
            e.getTargetException().printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
