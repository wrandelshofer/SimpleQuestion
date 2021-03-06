/*
 * @(#)SimpleQuestionApplicationModel.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */
package ch.randelshofer.simplequestion;

import ch.randelshofer.gift.export.ilias.ILIASQuestionPoolExporter;
import ch.randelshofer.io.ConfigurableFileFilter;
import ch.randelshofer.io.ConfigurableFileFilterAccessory;
import ch.randelshofer.io.ExtensionFileFilter;
import ch.randelshofer.simplequestion.action.SettingsAction;
import ch.randelshofer.simplequestion.action.VerifySyntaxAction;
import ch.randelshofer.teddy.CharacterSetAccessory;
import ch.randelshofer.teddy.action.ToggleLineNumbersAction;
import ch.randelshofer.teddy.action.ToggleLineWrapAction;
import ch.randelshofer.teddy.action.ToggleStatusBarAction;
import org.jhotdraw.app.Application;
import org.jhotdraw.app.DefaultApplicationModel;
import org.jhotdraw.app.MenuBuilder;
import org.jhotdraw.app.View;
import org.jhotdraw.app.action.edit.DuplicateAction;
import org.jhotdraw.app.action.file.ExportFileAction;
import org.jhotdraw.gui.JFileURIChooser;
import org.jhotdraw.gui.URIChooser;
import org.jhotdraw.util.ResourceBundleUtil;

import javax.swing.ActionMap;
import javax.swing.JFileChooser;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

/**
 * SimpleQuestionApplicationModel.
 *
 * @author Werner Randelshofer
 * @version 1.0 2009-09-06 Created.
 */
public class SimpleQuestionApplicationModel extends DefaultApplicationModel {
    public final static long serialVersionUID = 1L;

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
