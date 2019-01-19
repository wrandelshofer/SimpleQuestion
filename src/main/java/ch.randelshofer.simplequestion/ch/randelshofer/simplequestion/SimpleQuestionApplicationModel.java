/* @(#)SimpleQuestionApplicationModel.java
 * 
 * Copyright (c) 2009 Werner Randelshofer
 * Staldenmattweg 2, Immensee, CH-6405, Switzerland.
 * All rights reserved.
 * 
 * The copyright of this software is owned by Werner Randelshofer. 
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * Werner Randelshofer. For details see accompanying license terms.
 */
package ch.randelshofer.simplequestion;

import ch.randelshofer.gift.export.ilias.ILIASQuestionPoolExporter;
import ch.randelshofer.gift.export.scorm.SCORMExporter;
import ch.randelshofer.gift.export.scorm.SCORMExporterAccessory;
import ch.randelshofer.io.ConfigurableFileFilter;
import ch.randelshofer.io.ConfigurableFileFilterAccessory;
import ch.randelshofer.io.DirectoryFileFilter;
import ch.randelshofer.io.ExtensionFileFilter;
import ch.randelshofer.simplequestion.action.SettingsAction;
import ch.randelshofer.simplequestion.action.VerifySyntaxAction;
import ch.randelshofer.teddy.CharacterSetAccessory;
import javax.swing.*;
import org.jhotdraw.app.*;
import java.util.*;
import ch.randelshofer.teddy.action.*;
import java.io.File;
import java.util.prefs.Preferences;
import javax.swing.filechooser.FileFilter;
import org.jhotdraw.app.action.edit.DuplicateAction;
import org.jhotdraw.app.action.file.ExportFileAction;
import org.jhotdraw.gui.JFileURIChooser;
import org.jhotdraw.gui.URIChooser;
import org.jhotdraw.util.*;

/**
 * SimpleQuestionApplicationModel.
 *
 * @author Werner Randelshofer
 * @version 1.0 2009-09-06 Created.
 */
public class SimpleQuestionApplicationModel extends DefaultApplicationModel {
    public final static long serialVersionUID=1L;

    @Override
    public ActionMap createActionMap(Application a, View v) {
        ActionMap m = super.createActionMap(a, v);
        m.put(ch.randelshofer.teddy.action.FindAction.ID, new ch.randelshofer.teddy.action.FindAction(a, v));
        m.put(ToggleLineWrapAction.ID, new ToggleLineWrapAction(a, v));
        m.put(ToggleStatusBarAction.ID, new ToggleStatusBarAction(a, v));
        m.put(ToggleLineNumbersAction.ID, new ToggleLineNumbersAction(a, v));
        //m.put(PrintAction.ID, null);
        m.put(SettingsAction.ID, new SettingsAction(a));
        m.put(ExportFileAction.ID, new ExportFileAction(a, v));
        m.put(VerifySyntaxAction.ID, new VerifySyntaxAction(a, v));

        m.remove(DuplicateAction.ID);
        return m;
    }

    @Override
    protected MenuBuilder createMenuBuilder() {
        return new SimpleQuestionMenuBuilder();
    }


    @Override
    public List<JToolBar> createToolBars(Application app, View p) {
        return Collections.emptyList();
    }

    @Override
    public URIChooser createExportChooser(Application a, View v) {
     ResourceBundleUtil labels;
        labels = new ResourceBundleUtil(ResourceBundle.getBundle("ch/randelshofer/simplequestion/Labels"));
       Preferences prefs;
        prefs = Preferences.userNodeForPackage(SimpleQuestionView.class);


        JFileURIChooser c = new JFileURIChooser();

        LinkedList<FileFilter> list = new LinkedList<FileFilter>();
        ConfigurableFileFilter filter;
        list.add(filter = new ExtensionFileFilter(labels.getString("exportformat.ilias.questionpool"), "zip"));
        filter.putClientProperty("exporter", new ILIASQuestionPoolExporter());

        /*
        list.add(filter = new ExtensionFileFilter(labels.getString("exportformat.scorm.pif"), "zip"));
        filter.putClientProperty("exporter", new SCORMExporter(true));
        filter.setAccessory(new SCORMExporterAccessory(filter));

        list.add(filter = new DirectoryFileFilter(labels.getString("exportformat.scorm.content_package")));
        filter.putClientProperty("exporter", new SCORMExporter(false));
        filter.setAccessory(new SCORMExporterAccessory(filter));
        */

        for (FileFilter f : list) {
            c.addChoosableFileFilter(f);
        }
        String p = prefs.get("projectExportFilter", labels.getString("exportformat.ilias.questionpool"));
        for (FileFilter f : list) {
            if (p.equals(f.getDescription())) {
                c.setFileFilter(f);
            }
        }
        c.setAccessory(new ConfigurableFileFilterAccessory(c));
        c.setApproveButtonText(labels.getString("filechooser.exportApprove.text"));
        c.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        c.setSelectedFile(new File(prefs.get("projectExportFile", System.getProperty("user.home"))));

        return c;

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
