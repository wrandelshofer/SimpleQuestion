/*
 * @(#)CourseModel.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.scorm;

import ch.randelshofer.gui.ProgressIndicator;
import ch.randelshofer.gui.datatransfer.CompositeTransferable;
import ch.randelshofer.gui.tree.MutableTreeModel;
import ch.randelshofer.gui.tree.TreeModels;
import ch.randelshofer.io.LFPrintWriter;
import ch.randelshofer.scorm.cam.IMSManifestDocument;
import ch.randelshofer.scorm.cam.OrganizationElement;
import ch.randelshofer.scorm.cam.OrganizationsElement;
import ch.randelshofer.scorm.cam.ResourceElement;
import ch.randelshofer.text.PlainDocumentBean;
import ch.randelshofer.util.Files;
import ch.randelshofer.util.IdentifierGenerator;
import ch.randelshofer.util.SequentialDispatcher;
import ch.randelshofer.util.Strings;
import ch.randelshofer.xml.DOMs;
import ch.randelshofer.zip.DefaultZipEntryFilter;
import ch.randelshofer.zip.ZipFiles;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

//import java.awt.*;
//import java.awt.event.*;
//import javax.swing.text.*;
//import javax.swing.event.*;

/**
 * The model of a SCORM Course.
 *
 * @author Werner Randelshofer, Staldenmattweg 2, CH-6405 Immensee, Switzerland
 * @version 1.6.3 2006-07-29 writeIndexToHTML sets frame for tree toc to non-
 * scrollab.e
 * <br>1.6.2. 2006-07-10 Use UTF-8 encoding instead of ISO 8859-1.
 * <br>1.6.1 2006-06-01 Encode non-ascii characters with HTML entities
 * in file imsmanifest.js.
 * <br>1.6 2006-05-06 Reworked.
 * <br>1.5 2005-08-01 Replaced ProgressIndicator by ProgressIndicator.
 * <br>1.4.1 2004-06-17 Frameset rows/cols and number of frames created did
 * not match.
 * <br>1.4 2004-06-13 Support for logging improved. JavaScript code for
 * logging supplied by Kent Ogletree. SCO centers now inside browser window, if
 * it has a fixed size.
 * <br>1.3.2 2004-05-26 Method writeIndexHTML wrongly wrote a frameset tag
 * for the TOC frame, when no TOC was wanted.
 * <br>1.3.1 2004-04-22 Fixed wrong file names and directories created by method
 * createCourseFromPIF.
 * <br>1.3 2004-04-16 Property enforceTOC added.
 * <br>1.2 2003-01-06 Support for use of TinyLMS as an SCO added.
 * <br>1.1.5 2003-11-05 Added the following properties to the model:
 * Sequencing, DebugAPI and DebugButton.
 * <br>1.1 2003-11-01 Improved multilanguage support.
 * Diagnostic output to System.out removed. Login with password supported.
 * <br>1.0 2003-08-24 Login behaviour property and locale property added.
 * StudentsTableModel added. Properties are now saved in XML format instead of
 * the format provided by java.util.Properties.
 * <br>0.31 2003-07-23 Constructor calls loadProperties to initialize model.
 * <br>0.30 2003-06-22 Revised.
 * <br>0.28 2003-06-02 Wrong title was generated for the file index.html.
 * Forgot to close files properly in method createCourseFromContentPackage,
 * createCourseFromPIF
 * <br>0.27 2003-05-22 Provided support for fixed width pages. If the
 * page width and height and toc width and navigation bar height are all
 * fixed sizes, add more frames around the page to ensure, that the browser
 * respects these inner sizes.
 * <br>0.26 2003-05-19 Properties inside PIF must be read also.
 * <br>0.20 2003-05-02 Property tocWidth added.
 * <br>0.19.4 2003-04-03 Method validate() added.
 * <br>0.19 2003-03-26 exportToJavaScript() outputs a LMSCAMcolumns object if
 * the organziation structure is STRUCTURE_LAYERED.
 * <br>0.17 2003-03-17 Revised.
 * <br>0.2 2003-03-03 Revised.
 * <br>0.1 2003-02-04 Created.
 */
public class CourseModel extends DefaultTreeModel implements MutableTreeModel {
    public final static long serialVersionUID = 1L;
    /**
     * Property change support.
     */
    protected PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);

    /**
     * The enabled state of the model.
     */
    private boolean isEnabled;
    /**
     * This dispatcher is used to perform operations on a
     * background thread.
     */
    private SequentialDispatcher dispatcher;

    /**
     * A CAM supports multiple organization elements. This property specifies
     * which organization should be used for the course.
     */
    private OrganizationElement selectedOrganization;

    public final static int STRUCTURE_HIERARCHICAL = 0;
    public final static int STRUCTURE_LAYERED = 1;
    /**
     * This property overrides the 'structure' attribute in the selected
     * CAM OrganizationElement.
     */
    private int structure = STRUCTURE_HIERARCHICAL;

    public final static int SEQUENCING_AUTOMATIC = 0;
    public final static int SEQUENCING_MANUAL = 1;
    /**
     * This property determines the sequencing behaviour of the LMS.
     */
    private int sequencing = SEQUENCING_AUTOMATIC;
    /**
     * The (CSS) style of the generated course.
     */
    //private int style = 0;

    /**
     * The locale used for localization of the LMS user interface elements.
     */
    private Locale locale = Locale.ENGLISH;

    /**
     * The LMS logs in automatically as guest user.
     */
    public final static int LOGIN_AUTOMATIC = 0;
    /**
     * The LMS allows everybody to log in.
     */
    public final static int LOGIN_UNRESTRICTED = 1;
    /**
     * The LMS only allows known users to log in.
     */
    public final static int LOGIN_RESTRICTED = 2;
    /**
     * The login behaviour of the LMS.
     */
    private int loginMode = LOGIN_AUTOMATIC;
    /**
     * The logging behaviour of the LMS.
     * 0 = turn logging off
     * 1 = log API calls
     * 2 = log API calls and internals of TinyLMS
     */
    private int loglevel = 0;
    /**
     * The debugging button of the LMS.
     */
    private boolean isBugInfoButton = false;
    /**
     * If this is set to true, the LMS always displays the table of contents
     * of a course. If this is set to false, the LMS only displays the table
     * of contents of a course, if the course has more than one SCO.
     */
    private boolean isTOCVisible = false;

    /**
     * If this is set to true, the LMS displays a navigation bar at the bottom
     * of the page. When layered structure is ued, method isNavBarVisible
     * always returns true, regardless of the value of this variable.
     */
    private boolean isNavBarVisible = true;

    /**
     * The width of the Table of contents in the frameset
     * measured in Pixels.
     * The value "*" specifies dynamic width.
     */
    private PlainDocumentBean framesetTocWidth;
    /**
     * The height of a page in the frameset.
     * measured in Pixels.
     * The value "*" specifies dynamic height.
     */
    private PlainDocumentBean framesetPageHeight;
    /**
     * The width of a page in the frameset.
     * measured in Pixels.
     * The value "*" specifies dynamic width.
     */
    private PlainDocumentBean framesetPageWidth;
    /**
     * The height of the navigation bar in the frameset.
     * measured in Pixels.
     * The value "*" specifies dynamic height.
     */
    private PlainDocumentBean framesetNavHeight;

    /**
     * This model holds the student account information.
     */
    private StudentsTableModel usersTableModel;


    /**
     * Creates a new instance of CourseModel
     */
    public CourseModel() {
        super(new DefaultMutableTreeNode());
        usersTableModel = new StudentsTableModel();
        framesetTocWidth = new PlainDocumentBean(propertySupport, "framesetTocWidth");
        framesetPageHeight = new PlainDocumentBean(propertySupport, "framesetTocWidth");
        framesetPageWidth = new PlainDocumentBean(propertySupport, "framesetTocWidth");
        framesetNavHeight = new PlainDocumentBean(propertySupport, "framesetTocWidth");
        try {
            loadProperties(null);
        } catch (IOException e) {
            InternalError t = new InternalError("Unable to initialize model");
            //t.setCause(e);
            throw t;
        }
    }

    public StudentsTableModel getUsersTableModel() {
        return usersTableModel;
    }

    /**
     * Returns the enabled state of the model.
     */
    public boolean isEnabled() {
        return isEnabled;
    }

    /**
     * Sets the enabled state of the model.
     * Notifies property change listeners that the property "enabled" has changed.
     */
    public void setEnabled(boolean newValue) {
        boolean oldValue = isEnabled;
        isEnabled = newValue;
        propertySupport.firePropertyChange("enabled", oldValue, newValue);
    }

    /**
     * Returns the enabled state of the model.
     */
    public boolean isNavBarVisible() {
        return isNavBarVisible || structure == STRUCTURE_LAYERED;
    }

    /**
     * Sets the visible state of the navigation bar.
     * Notifies property change listeners that the property "navBarVisible" has changed.
     */
    public void setNavBarVisible(boolean newValue) {
        if (structure != STRUCTURE_LAYERED) {
            boolean oldValue = isNavBarVisible;
            isNavBarVisible = newValue;
            propertySupport.firePropertyChange("navBarVisible", oldValue, newValue || structure == STRUCTURE_LAYERED);
        }
    }

    /**
     * Returns the value of the "logLevel" property.
     */
    public int getLogLevel() {
        return loglevel;
    }

    /**
     * Sets the value of the "logLevel" property.
     * Notifies property change listeners that the property has changed.
     */
    public void setLogLevel(int newValue) {
        int oldValue = loglevel;
        loglevel = newValue;
        propertySupport.firePropertyChange("logLevel", oldValue, newValue);
    }

    /**
     * Returns the value of the "enforceTOC" property.
     */
    public boolean isTOCVisible() {
        return isTOCVisible || structure == STRUCTURE_LAYERED;
    }

    /**
     * Sets the value of the "enforceTOC" property.
     * Notifies property change listeners that the property has changed.
     */
    public void setTOCVisible(boolean newValue) {
        boolean oldValue = isTOCVisible;
        isTOCVisible = newValue;
        propertySupport.firePropertyChange("enforceTOC", oldValue, newValue || structure == STRUCTURE_LAYERED);
    }

    /**
     * Returns the value of the "bugInfoButton" property.
     */
    public boolean isBugInfoButton() {
        return isBugInfoButton;
    }

    /**
     * Sets the value of the "bugInfoButton" property.
     * Notifies property change listeners that the property has changed.
     */
    public void setBugInfoButton(boolean newValue) {
        boolean oldValue = isEnabled;
        isBugInfoButton = newValue;
        propertySupport.firePropertyChange("bugInfoButton", oldValue, newValue);
    }

    /**
     * Returns the organization structure of the course.
     *
     * @return STRUCTURE_HIERARCHICAL or STRUCTURE_LAYERED.
     */
    public int getStructure() {
        return structure;
    }

    /**
     * Sets the organization structure of the course.
     * This property overrides the 'structure' attribute in the current
     * organization element of the Content Aggregation Model.
     * Notifies property change listeners that the property "structure" has changed.
     *
     * @param newValue STRUCTURE_HIERARCHICAL or STRUCTURE_LAYERED.
     * @throws IllegalArgumentException if newValue is illegal.
     */
    public void setStructure(int newValue) {
        if (newValue < STRUCTURE_HIERARCHICAL || newValue > STRUCTURE_LAYERED) {
            throw new IllegalArgumentException("Illegal structure:" + newValue);
        }
        int oldValue = structure;
        boolean oldNavBarValue = isNavBarVisible();
        structure = newValue;
        propertySupport.firePropertyChange("structure", oldValue, newValue);
        boolean newNavBarValue = isNavBarVisible();
        propertySupport.firePropertyChange("navBarVisible", oldNavBarValue, newNavBarValue);
    }

    /**
     * Returns the sequencing behaviour of the LMS.
     *
     * @return SEQUENCING_AUTOMATIC or SEQUENCING_MANUAL.
     */
    public int getSequencing() {
        return sequencing;
    }

    /**
     * Sets the sequencing behaviour of the LMS.
     *
     * @param newValue SEQUENCING_AUTOMATIC or SEQUENCING_MANUAL.
     * @throws IllegalArgumentException if newValue is illegal.
     */
    public void setSequencing(int newValue) {
        if (newValue != SEQUENCING_AUTOMATIC && newValue != SEQUENCING_MANUAL) {
            throw new IllegalArgumentException("Illegal sequencing:" + newValue);
        }
        int oldValue = sequencing;
        sequencing = newValue;
        propertySupport.firePropertyChange("sequencing", oldValue, newValue);
    }

    /**
     * Returns the login behaviour of the LMS.
     *
     * @return LOGIN_AUTOMATIC, LOGIN_UNRESTRICTED or LOGIN_RESTRICTED.
     */
    public int getLoginMode() {
        return loginMode;
    }

    /**
     * Sets the login behavoiur of the LMS.
     * Notifies property change listeners that the property "loginMode" has
     * changed.
     *
     * @param newValue LOGIN_AUTOMATIC, LOGIN_UNRESTRICTED or LOGIN_RESTRICTED.
     * @throws IllegalArgumentException if newValue is illegal.
     */
    public void setLoginMode(int newValue) {
        if (newValue != LOGIN_AUTOMATIC
                && newValue != LOGIN_UNRESTRICTED
                && newValue != LOGIN_RESTRICTED) {
            throw new IllegalArgumentException("Illegal login behaviour:" + newValue);
        }
        int oldValue = loginMode;
        loginMode = newValue;
        propertySupport.firePropertyChange("loginMode", oldValue, newValue);
    }

    /**
     * Returns the locale of the LMS.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Sets the locale of the LMS.
     * Notifies property change listeners that the property "locale" has
     * changed.
     */
    public void setLocale(Locale newValue) {
        Locale oldValue = locale;
        locale = newValue;
        propertySupport.firePropertyChange("locale", oldValue, newValue);
    }

    /**
     * Returns the width of the table of contents in the frameset.
     */
    public String getFramesetTocWidth() {
        return framesetTocWidth.getText();
    }

    /**
     * Returns the width of the table of contents in the frameset.
     */
    public PlainDocumentBean getFramesetTocWidthDocument() {
        return framesetTocWidth;
    }

    /**
     * Sets the width of the table of contents in the frameset.
     */
    public void setFramesetTocWidth(String newValue) {
        framesetTocWidth.setText(newValue);
    }

    /**
     * Returns the width of a page in the frameset.
     */
    public String getFramesetPageWidth() {
        return framesetPageWidth.getText();
    }

    /**
     * Returns the width of a page in the frameset.
     */
    public PlainDocumentBean getFramesetPageWidthDocument() {
        return framesetPageWidth;
    }

    /**
     * Sets the width of a page in the frameset.
     */
    public void setFramesetPageWidth(String newValue) {
        framesetPageWidth.setText(newValue);
    }

    /**
     * Returns the height of a page in the frameset.
     */
    public String getFramesetPageHeight() {
        return framesetPageHeight.getText();
    }

    /**
     * Returns the width of a page in the frameset.
     */
    public PlainDocumentBean getFramesetPageHeightDocument() {
        return framesetPageHeight;
    }

    /**
     * Sets the height of a page in the frameset.
     */
    public void setFramesetPageHeight(String newValue) {
        framesetPageHeight.setText(newValue);
    }

    /**
     * Returns the height of the navigation bar in the frameset.
     */
    public String getFramesetNavHeight() {
        return framesetNavHeight.getText();
    }

    /**
     * Returns the height of the navigation bar in the frameset.
     */
    public PlainDocumentBean getFramesetNavHeightDocument() {
        return framesetNavHeight;
    }

    /**
     * Sets the height of the navigation bar in the frameset.
     */
    public void setFramesetNavHeight(String newValue) {
        framesetNavHeight.setText(newValue);
    }

    /**
     * Returns the selected organization for this course.
     */
    public OrganizationElement getSelectedOrganization() {
        return selectedOrganization;
    }

    /**
     * Sets the selected organization for this course.
     * Notifies property change listeners that the property "selectedOrganization" has changed.
     *
     * @param newValue must be a member of this CAM tree.
     */
    public void setSelectedOrganization(OrganizationElement newValue) {
        OrganizationElement oldValue = selectedOrganization;
        selectedOrganization = newValue;
        propertySupport.firePropertyChange("selectedOrganization", oldValue, newValue);
    }

    /**
     * Dispatches a runnable on the worker thread of the document model.
     * <p>
     * Note that the worker thread executes the runnables sequentially.
     * <p>
     * Use this method to dispatch tasks which block the user interface
     * until the task has finished. i.e. Loading and Saving a document from
     * a file.
     */
    public void dispatch(Runnable runner) {
        if (dispatcher == null) {
            dispatcher = new SequentialDispatcher();
        }
        dispatcher.dispatch(runner);
    }

    /**
     * Imports the specified Package Interchange File (PIF) into the model.
     */
    public void importPIF(File inputPIF)
            throws IOException, SAXException, ParserConfigurationException {
        clear();

        ZipFile zif = null;
        InputStream in = null;
        try {
            zif = new ZipFile(inputPIF);
            final IMSManifestDocument mani = new IMSManifestDocument();
            ZipEntry entry = zif.getEntry("imsmanifest.xml");
            if (entry == null) {
                throw new FileNotFoundException("Couldn't find the entry 'imsmanifest.xml' in '" + inputPIF.getName() + "'.");
            }
            in = zif.getInputStream(entry);
            mani.readXML(in);
            mani.setPIFFile(inputPIF);
            in.close();
            entry = zif.getEntry("tinylms.xml");
            if (entry != null) {
                loadProperties(in = zif.getInputStream(entry));
            }

            Runnable runner = new Runnable() {
                public void run() {
                    insertNodeInto(mani, (MutableTreeNode) getRoot(), 0);
                    nodeStructureChanged((TreeNode) getRoot());
                    setSelectedOrganization(
                            getIMSManifestDocument()
                                    .getOrganizationsElement()
                                    .getDefaultOrganizationElement()
                    );
                    //setStructure(STRUCTURE_HIERARCHICAL);
                }
            };
            if (SwingUtilities.isEventDispatchThread()) {
                runner.run();
            } else {
                try {
                    SwingUtilities.invokeAndWait(runner);
                } catch (InterruptedException e) {
                } catch (InvocationTargetException e) {
                    throw new InternalError(e.getMessage());
                }
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (zif != null) {
                zif.close();
            }
        }
    }

    /**
     * Imports the specified Directory into the model.
     * The directory must contain a content package.
     */
    public void importContentPackage(File dirFile)
            throws IOException, SAXException, ParserConfigurationException {
        clear();

        File manifestFile = new File(dirFile, "imsmanifest.xml");

        InputStream in = null, in2 = null;
        try {
            final IMSManifestDocument mani = new IMSManifestDocument();
            in = new BufferedInputStream(new FileInputStream(manifestFile));
            mani.readXML(in);
            mani.setContentPackage(dirFile);

            File propertiesFile = new File(dirFile, "tinylms.xml");
            if (propertiesFile.exists()) {
                in2 = new FileInputStream(propertiesFile);
                try {
                    loadProperties(in2);
                } catch (IOException e) {
                    e.printStackTrace();
                    IOException e2 = new IOException("Error loading tinylms.xml.");
                    e2.initCause(e);
                    throw e2;
                }
            }


            Runnable runner = new Runnable() {
                public void run() {
                    insertNodeInto(mani, (MutableTreeNode) getRoot(), 0);
                    nodeStructureChanged((TreeNode) getRoot());
                    if (getIMSManifestDocument().getOrganizationsElement() != null) {
                        setSelectedOrganization(
                                getIMSManifestDocument()
                                        .getOrganizationsElement()
                                        .getDefaultOrganizationElement()
                        );
                    }
                }
            };
            if (SwingUtilities.isEventDispatchThread()) {
                runner.run();
            } else {
                try {
                    SwingUtilities.invokeAndWait(runner);
                } catch (Exception e) {
                    InternalError error = new InternalError(e.getMessage());
                    error.initCause(e);
                    throw error;
                }
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            if (in2 != null) {
                try {
                    in2.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Creates a course into the specified directory.
     */
    public void createCourse(File tgtdir, ProgressIndicator p)
            throws IOException {
        if (getIMSManifestDocument().getPIFFile() != null) {
            createCourseFromPIF(tgtdir, p);
        } else {
            createCourseFromContentPackage(tgtdir, p);
        }
    }

    /**
     * Creates a course into the specified directory.
     */
    public void createPIF(File tgtPif, ProgressIndicator p)
            throws IOException {
        if (getIMSManifestDocument().getPIFFile() != null) {
            createPIFFromPIF(tgtPif, p);
        } else {
            createPIFFromContentPackage(tgtPif, p);
        }
    }

    /**
     * Returns the name of the content package for display in the title bar
     * of a Frame.
     */
    public String getContentPackageName() {
        File f = getIMSManifestDocument().getPIFFile();
        if (f == null) {
            f = getIMSManifestDocument().getContentPackage();
        }
        return f.getName();
    }


    /**
     * Creates a course into the specified directory.
     */
    public void createCourseFromPIF(File tgtdir, ProgressIndicator p)
            throws IOException {
        File inputPIF = ((IMSManifestDocument) getChild(getRoot(), 0)).getPIFFile();

        p.setNote("Creating directories...");

        // Test output directory
        if (!tgtdir.exists()) {
            tgtdir.mkdir();
        } else if (!tgtdir.isDirectory()) {
            throw new IOException("'" + tgtdir + "' is not a directory.");
        }

        // Create a sub-directory named "course" in the output directory
        File courseDirectory = new File(tgtdir, "course");

        InputStream in = null;
        OutputStream out = null;

        // Determine the files that need to be copied
        HashSet<ResourceElement> resources = getSelectedOrganization().getReferencedResources();
        HashSet<String> fileNames = new HashSet<>();
        HashSet<ResourceElement> exclusionList = new HashSet<>();
        for (ResourceElement resource : resources) {
            resource.addReferencedFileNamesTo(fileNames, exclusionList);
        }

        p.setMaximum(3);
        p.setNote("Copying " + fileNames.size() + " course files...");
        int progress = 0;

        // Unzip the PIF file into the course directory.
        ZipFiles.unzip(inputPIF, courseDirectory, new DefaultZipEntryFilter(fileNames, true));

        p.setNote("Creating LMS files...");
        p.setProgress(++progress);

        // Unzip the LMS HTML files into the output directory
        in = null;
        try {
            in = new BufferedInputStream(getClass().getResourceAsStream("/lmshtml.zip"));
            HashSet<String> exclude = new HashSet<>();
            exclude.add("ims_xml.xsd");
            exclude.add("adlcp_rootv1p2.xsd");
            exclude.add("imscp_rootv1p1p2.xsd");
            exclude.add("imsmd_rootv1p2p1.xsd");
            exclude.add("tinylms/lib/lmsparentstub.js");
            ZipFiles.unzip(new ZipInputStream(in), tgtdir, new DefaultZipEntryFilter(exclude, false));
        } finally {
            if (in != null) {
                in.close();
            }
        }

        out = null;
        try {
            out = new FileOutputStream(new File(tgtdir, "imsmanifest.js"));
            writeIMSManifest(out, false);
        } finally {
            if (out != null) {
                out.close();
            }
        }
        out = null;
        try {
            out = new FileOutputStream(new File(tgtdir, "index.html"));
            writeIndexHTML(out, false);
        } finally {
            if (out != null) {
                out.close();
            }
        }

        p.setNote("Done.");
        p.setProgress(++progress);
    }

    /**
     * Creates a course into the specified directory.
     */
    public void createCourseFromContentPackage(File tgtdir, ProgressIndicator p)
            throws IOException {
        IMSManifestDocument manifest = (IMSManifestDocument) getChild(getRoot(), 0);
        File contentPackageDirectory = manifest.getContentPackage();

        p.setNote("Creating directories...");

        // Test output directory
        if (!tgtdir.exists()) {
            tgtdir.mkdirs();
        } else if (!tgtdir.isDirectory()) {
            throw new IOException("'" + tgtdir + "' is not a directory.");
        }

        // Create a sub-directory named "course" in the output directory
        File courseDirectory = new File(tgtdir, "course");

        // Determine the files that need to be copied
        HashSet<ResourceElement> resources = getSelectedOrganization().getReferencedResources();
        HashSet<String> fileNames = new HashSet<>();
        HashSet<ResourceElement> exclusionList = new HashSet<>();
        for (ResourceElement resource : resources) {
            resource.addReferencedFileNamesTo(fileNames, exclusionList);
        }

        p.setMaximum(fileNames.size() + 2);
        p.setNote("Copying " + fileNames.size() + " course files...");
        int progress = 0;

        // Copy the files from the content package directory into the course
        // directory.
        for (String fileName : fileNames) {
            String filename = (fileName).replace('/', File.separatorChar);
            File source = new File(contentPackageDirectory, filename);
            File target = new File(courseDirectory, filename);
            Files.copyFile(source, target);
            p.setProgress(++progress);
        }

        p.setNote("Creating LMS files...");
        p.setProgress(++progress);


        // Unzip the LMS HTML files into the output directory
        try (InputStream in = new BufferedInputStream(getClass().getResourceAsStream("/lmshtml.zip"))) {
            HashSet<String> exclude = new HashSet<>();
            exclude.add("ims_xml.xsd");
            exclude.add("adlcp_rootv1p2.xsd");
            exclude.add("imscp_rootv1p1p2.xsd");
            exclude.add("imsmd_rootv1p2p1.xsd");
            exclude.add("tinylms/lib/lmsparentstub.js");
            ZipFiles.unzip(new ZipInputStream(in), tgtdir, new DefaultZipEntryFilter(exclude, false));
        }

        try (OutputStream out = new FileOutputStream(new File(tgtdir, "imsmanifest.js"))) {
            writeIMSManifest(out, false);
        }
        try (OutputStream out = new FileOutputStream(new File(tgtdir, "index.html"))) {
            writeIndexHTML(out, false);
        }
        p.setNote("Done.");
        p.setProgress(++progress);
    }

    /**
     * Creates a course into the specified directory.
     */
    public void createPIFFromPIF(File outputPIF, ProgressIndicator p)
            throws IOException {
        File inputPIF = ((IMSManifestDocument) getChild(getRoot(), 0)).getPIFFile();

        // Create the outputPIF
        try (ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outputPIF)))) {

            // Determine the files that need to be copied
            HashSet<ResourceElement> resources = getSelectedOrganization().getReferencedResources();
            HashSet<String> fileNames = new HashSet<>();
            HashSet<ResourceElement> exclusionList = new HashSet<>();
            for (ResourceElement resource : resources) {
                resource.addReferencedFileNamesTo(fileNames, exclusionList);
            }

            // Rezip files from inputPIF to the outputPIF
            try (ZipInputStream in = new ZipInputStream(new BufferedInputStream(new FileInputStream(inputPIF)))) {
                ZipFiles.rezip(
                        in,
                        out,
                        "course",
                        new DefaultZipEntryFilter(fileNames, true)
                );
            }

            // Unzip the LMS HTML files into the output directory
            try (ZipInputStream in = new ZipInputStream(new BufferedInputStream(getClass().getResourceAsStream("/lmshtml.zip")))) {
                ZipFiles.rezip(in, out, "");
            }

            out.putNextEntry(new ZipEntry("imsmanifest.js"));
            writeIMSManifest(out, true);
            out.closeEntry();

            out.putNextEntry(new ZipEntry("imsmanifest.xml"));
            writeParentManifest(out, fileNames);
            out.closeEntry();

            out.putNextEntry(new ZipEntry("index.html"));
            writeIndexHTML(out, true);
            out.closeEntry();
        }
    }

    /**
     * Creates a pif file from a content package.
     */
    public void createPIFFromContentPackage(File outputPif, ProgressIndicator p)
            throws IOException {
        IMSManifestDocument manifest = (IMSManifestDocument) getChild(getRoot(), 0);
        File contentPackageDirectory = manifest.getContentPackage();
        int progress = 0;
        try (ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outputPif)))) {

            // Determine the files that need to be copied
            HashSet<ResourceElement> resources = getSelectedOrganization().getReferencedResources();
            HashSet<String> fileNames = new HashSet<>();
            HashSet<ResourceElement> exclusionList = new HashSet<>();
            for (ResourceElement resource : resources) {
                (resource).addReferencedFileNamesTo(fileNames, exclusionList);
            }

            // Copy the files from the content package directory into the course
            // directory.
            p.setMaximum(fileNames.size() + 2);
            p.setNote("Copying " + fileNames.size() + " course files...");
            progress = 0;
            // XXX - To do
            //copyDirectoryTree(contentPackageDirectory, courseDirectory, fileNames);
            // Copy the files from the content package directory into the course
            // directory.
            for (String entryName : fileNames) {
                String filename = entryName.replace('/', File.separatorChar);
                File source = new File(contentPackageDirectory, filename);
                //File target = new File(courseDirectory, filename);
                //Files.copyFile(source, target);
                ZipFiles.zip(source, out, "course/" + entryName);
                p.setProgress(++progress);
            }


            p.setNote("Creating LMS files...");
            p.setProgress(++progress);
            // Unzip the LMS HTML files into the output directory

            try (InputStream in = new ZipInputStream(new BufferedInputStream(getClass().getResourceAsStream("/lmshtml.zip")))) {
                ZipFiles.rezip((ZipInputStream) in, out, "");
            }

            out.putNextEntry(new ZipEntry("imsmanifest.js"));
            writeIMSManifest(out, true);
            out.closeEntry();

            out.putNextEntry(new ZipEntry("imsmanifest.xml"));
            writeParentManifest(out, fileNames);
            out.closeEntry();

            out.putNextEntry(new ZipEntry("index.html"));
            writeIndexHTML(out, true);
            out.closeEntry();
        }
        p.setNote("Done.");
        p.setProgress(++progress);
    }

    /**
     * Converts the provided string into a valid HTML frameset size.
     *
     * @param str The String to be converted. May contain either a positive
     *            numeric value greater than 0 or a "*".
     * @return The converted String.
     */
    private String toValidFramesetSize(String str) {
        try {
            int i = Integer.decode(str.trim()).intValue();
            return (i > 0) ? Integer.toString(i) : "*";
        } catch (NumberFormatException e) {
            return "*";
        }
    }

    /**
     * Create the index.html file
     */
    private void writeIndexHTML(OutputStream out, boolean isSCORMAdapter)
            throws IOException {

        // Create a JavaScript version of the imsmanifest.xml file
        PrintWriter w = null;
        try {
            // Determine language specific items.
            String languageSuffix = getLocale().getLanguage();

            w = new LFPrintWriter(
                    new OutputStreamWriter(
                            new BufferedOutputStream(out),
                            "utf-8"
                    ));
            w.print(
                    "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Frameset//EN\" \"http://www.w3.org/TR/html4/frameset.dtd\">\n" +
                            "<!--\n" +
                            " * @(#)index.html " + TinyLMSApp.getVersion() + "\n" +
                            " *\n" +
                            " * Generated with TinyLMS " + TinyLMSApp.getVersion() + "\n" +
                            " * Copyright (c) 2003-2006 Werner Randelshofer\n" +
                            " * Staldenmattweg 2, Immensee, CH-6405, Switzerland\n" +
                            " * All rights reserved.\n" +
                            " *\n" +
                            " * This software is the confidential and proprietary information of\n" +
                            " * Werner Randelshofer. (\"Confidential Information\").  You shall not\n" +
                            " * disclose such Confidential Information and shall use it only in\n" +
                            " * accordance with the terms of the license agreement you entered into\n" +
                            " * with Werner Randelshofer.\n" +
                            "-->\n" +
                            "<html>\n" +
                            "<head>\n" +
                            "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n" +
                            "<title>"
            );
            String title = getSelectedOrganization().getTitle();
            w.print((title == null) ? "TinyLMS" : title);
            w.print(
                    "</title>\n" +
                            "<script language=\"JavaScript\" src=\"tinylms/lib/listlogger.js\" type=\"text/JavaScript\"></script>\n" +
                            "<script language=\"JavaScript\" src=\"tinylms/lib/collections.js\" type=\"text/JavaScript\"></script>\n" +
                            "<script language=\"JavaScript\" src=\"tinylms/lib/cookies.js\" type=\"text/JavaScript\"></script>\n" +
                            "<script language=\"JavaScript\" src=\"tinylms/lib/sha1.js\" type=\"text/JavaScript\"></script>\n"
            );
            if (isSCORMAdapter) {
                w.print(
                        "<script language=\"JavaScript\" src=\"tinylms/lib/lmsparentstub.js\" type=\"text/JavaScript\"></script>\n" +
                                "<script language=\"JavaScript\" type=\"text/JavaScript\">\n" +
                                "<!--\n" +
                                "parentstub.initialize();\n" +
                                "//-->\n" +
                                "</script>\n"
                );
            }
            w.print(
                    "<script language=\"JavaScript\" src=\"tinylms/lib/lmscam.js\" type=\"text/JavaScript\"></script>\n" +
                            "<script language=\"JavaScript\" src=\"tinylms/lib/lmsapi.js\" type=\"text/JavaScript\"></script>\n" +
                            "<script language=\"JavaScript\" src=\"tinylms/lib/lmslabels_" + languageSuffix + ".js\" type=\"text/JavaScript\"></script>\n" +
                            "<script language=\"JavaScript\" src=\"imsmanifest.js\" type=\"text/JavaScript\"></script>\n" +
                            "<script language=\"JavaScript\" type=\"text/JavaScript\">API.start();</script>\n" +
                            "</head>\n" +
                            "\n");

            // Make sure that these property contain valid frameset sizes.
            setFramesetTocWidth(toValidFramesetSize(getFramesetTocWidth()));
            setFramesetNavHeight(toValidFramesetSize(getFramesetNavHeight()));
            setFramesetPageWidth(toValidFramesetSize(getFramesetPageWidth()));
            setFramesetPageHeight(toValidFramesetSize(getFramesetPageHeight()));


            // Get page dimensions
            // Page dimensions can contain "*" characters to indicate
            // dynamic sizes. We use the value -1 to specify this.
            int pageWidth = -1, pageHeight = -1, tocWidth = -1, navHeight = -1;
            try {
                pageWidth = Integer.decode(getFramesetPageWidth()).intValue();
            } catch (NumberFormatException e) {
            }
            try {
                pageHeight = Integer.decode(getFramesetPageHeight()).intValue();
            } catch (NumberFormatException e) {
            }
            try {
                tocWidth = Integer.decode(getFramesetTocWidth()).intValue();
            } catch (NumberFormatException e) {
            }
            try {
                navHeight = Integer.decode(getFramesetNavHeight()).intValue();
            } catch (NumberFormatException e) {
            }

            // If there is only one item in the organization,
            // we do not provide a table of contents.
            // This can be overriden by setting isTOCVisible to true.
            if (!isTOCVisible
                    && getSelectedOrganization().getItemList().size() == 1
                    && getSelectedOrganization().getItemList().get(0).getItemList().size() == 0) {
                w.print(MessageFormat.format(
                        "<frameset {0} cols=\"{1}\" frameborder=\"NO\" border=\"0\" framespacing=\"0\" {5}>\n"
                                + "{4}{3}{4}\n"
                                + "{2}"
                                //+"  <frame src=\"tinylms/lmsempty.html\" name=\"lmsTOCFrame\" scrolling=\"NO\">\n"
                                + "  <frame src=\"{6}\" name=\"lmsContentFrame\">"
                                + "{2}\n"
                                + "{4}{3}{4}\n"
                                + "</frameset>\n"
                                + "<noframes><body{7}>\n"
                                + "</body></noframes>\n"
                                + "</html>\n"
                        ,
                        new Object[]{
                                (pageHeight == -1) ? "" : "rows=\"*," + getFramesetPageHeight() + ",*\"", // rows
                                (pageWidth == -1) ? "" + getFramesetPageWidth() : "*," + getFramesetPageWidth() + ",*", // cols
                                (pageWidth == -1 || tocWidth == -1) ? "" : "<frame src=\"tinylms/lmsbackground.html\" scrolling=\"NO\" noresize>", // border frames
                                (pageHeight == -1) ? "" : "<frame src=\"tinylms/lmsbackground.html\" scrolling=\"NO\" noresize>", // border frames
                                (pageHeight == -1 || pageWidth == -1 || tocWidth == -1) ? "" : "<frame src=\"tinylms/lmsbackground.html\" scrolling=\"NO\" noresize>", // border frames
                                (loginMode == LOGIN_AUTOMATIC) ? "onLoad=\"API.login('guest','');\"" : "",
                                (loginMode == LOGIN_AUTOMATIC) ? "tinylms/lmsempty.html" : "tinylms/lmslogin_" + languageSuffix + ".html",
                                (isSCORMAdapter) ? "  onUnload=\"parentstub.abnormalExit()\"" : "",
                        }
                ));
            } else if (getStructure() == STRUCTURE_HIERARCHICAL) {
                if (isNavBarVisible()) {
                    w.print(MessageFormat.format(
                            "<frameset {0} cols=\"{1}\"  frameborder=\"NO\" border=\"0\" framespacing=\"0\" {6}>\n"
                                    + "  {5}{4}{4}{5}\n"
                                    + "  {3}\n"
                                    + "  <frame src=\"tinylms/lmstreetoc.html\" name=\"lmsTOCFrame\" scrolling=\"NO\">\n"
                                    + "  <frameset rows=\"{2}\" frameborder=\"NO\" border=\"0\" framespacing=\"0\">\n"
                                    + "    <frame src=\"{7}\" name=\"lmsContentFrame\">\n"
                                    + "    <frame src=\"tinylms/lmstreenav.html\" name=\"lmsNavFrame\" scrolling=\"NO\">\n"
                                    + "  </frameset>\n"
                                    + "  {3}\n"
                                    + "  {5}{4}{4}{5}\n"
                                    + "</frameset>\n"
                                    + "<noframes><body>\n"
                                    + "</body></noframes>\n"
                                    + "</html>\n"
                            ,
                            new Object[]{
                                    (pageHeight == -1 || navHeight == -1) ? "" : "rows=\"*," + (pageHeight + navHeight) + ",*\"", // outer rows
                                    (pageWidth == -1 || tocWidth == -1) ? getFramesetTocWidth() + "," + getFramesetPageWidth() : "*," + getFramesetTocWidth() + "," + getFramesetPageWidth() + ",*", // outer cols
                                    getFramesetPageHeight() + "," + getFramesetNavHeight(), // inner rows
                                    (pageWidth == -1 || tocWidth == -1) ? "" : "<frame src=\"tinylms/lmsbackground.html\" scrolling=\"NO\" noresize>", // border frames
                                    (pageHeight == -1 || navHeight == -1) ? "" : "<frame src=\"tinylms/lmsbackground.html\" scrolling=\"NO\" noresize>", // border frames
                                    (pageHeight == -1 || navHeight == -1 || pageWidth == -1 || tocWidth == -1) ? "" : "<frame src=\"tinylms/lmsbackground.html\" scrolling=\"NO\" noresize>", // border frames
                                    (loginMode == LOGIN_AUTOMATIC) ? "onLoad=\"API.login('guest','');\"" : "",
                                    (loginMode == LOGIN_AUTOMATIC) ? "tinylms/lmsempty.html" : "tinylms/lmslogin_" + languageSuffix + ".html",
                            }
                    ));
                } else {
                    w.print(MessageFormat.format(
                            "<frameset {0} cols=\"{1}\" frameborder=\"NO\" border=\"0\" framespacing=\"0\" {5}>\n"
                                    + "{4}{3}{3}{4}\n"
                                    + "{2}\n"
                                    + "  <frame src=\"tinylms/lmstreetoc.html\" name=\"lmsTOCFrame\" scrolling=\"AUTO\">\n"
                                    + "  <frame src=\"{6}\" name=\"lmsContentFrame\">\n"
                                    + "{2}\n"
                                    + "{4}{3}{3}{4}\n"
                                    + "</frameset>\n"
                                    + "<noframes><body>\n"
                                    + "</body></noframes>\n"
                                    + "</html>\n"
                            ,
                            new Object[]{
                                    (pageHeight == -1) ? "" : "rows=\"*," + getFramesetPageHeight() + ",*\"", // rows
                                    (pageWidth == -1 || tocWidth == -1) ? getFramesetTocWidth() + "," + getFramesetPageWidth() : "*," + getFramesetTocWidth() + "," + getFramesetPageWidth() + ",*", // cols
                                    (pageWidth == -1 || tocWidth == -1) ? "" : "<frame src=\"tinylms/lmsbackground.html\" scrolling=\"NO\" noresize>", // border frames
                                    (pageHeight == -1) ? "" : "<frame src=\"tinylms/lmsbackground.html\" scrolling=\"NO\" noresize>", // border frames
                                    (pageHeight == -1 || pageWidth == -1 || tocWidth == -1) ? "" : "<frame src=\"tinylms/lmsbackground.html\" scrolling=\"NO\" noresize>", // border frames
                                    (loginMode == LOGIN_AUTOMATIC) ? "onLoad=\"API.login('guest','');\"" : "",
                                    (loginMode == LOGIN_AUTOMATIC) ? "tinylms/lmsempty.html" : "tinylms/lmslogin_" + languageSuffix + ".html",
                            }
                    ));
                }
            } else {// Implicit: else if (getStructure() == STRUCTURE_LAYERED) {
                w.print(MessageFormat.format(
                        "<frameset {0} cols=\"{1}\"  frameborder=\"NO\" border=\"0\" framespacing=\"0\" {6}>\n"
                                + "  {5}{4}{4}{5}\n"
                                + "  {3}\n"
                                + "  <frame src=\"tinylms/lmslayertoc.html\" name=\"lmsTOCFrame\" scrolling=\"AUTO\">\n"
                                + "  <frameset rows=\"{2}\" frameborder=\"NO\" border=\"0\" framespacing=\"0\">\n"
                                + "    <frame src=\"{7}\" name=\"lmsContentFrame\">\n"
                                + "    <frame src=\"tinylms/lmslayernav.html\" name=\"lmsNavFrame\" scrolling=\"NO\">\n"
                                + "  </frameset>\n"
                                + "  {3}\n"
                                + "  {5}{4}{4}{5}\n"
                                + "</frameset>\n"
                                + "<noframes><body>\n"
                                + "</body></noframes>\n"
                                + "</html>\n"
                        ,
                        new Object[]{
                                (pageHeight == -1 || navHeight == -1) ? "" : "rows=\"*," + (pageHeight + navHeight) + ",*\"", // outer rows
                                (pageWidth == -1 || tocWidth == -1) ? getFramesetTocWidth() + "," + getFramesetPageWidth() : "*," + getFramesetTocWidth() + "," + getFramesetPageWidth() + ",*", // outer cols
                                getFramesetPageHeight() + "," + getFramesetNavHeight(), // inner rows
                                (pageWidth == -1 || tocWidth == -1) ? "" : "<frame src=\"tinylms/lmsbackground.html\" scrolling=\"NO\" noresize>", // border frames
                                (pageHeight == -1 || navHeight == -1) ? "" : "<frame src=\"tinylms/lmsbackground.html\" scrolling=\"NO\" noresize>", // border frames
                                (pageHeight == -1 || navHeight == -1 || pageWidth == -1 || tocWidth == -1) ? "" : "<frame src=\"tinylms/lmsbackground.html\" scrolling=\"NO\" noresize>", // border frames
                                (loginMode == LOGIN_AUTOMATIC) ? "onLoad=\"API.login('guest','');\"" : "",
                                (loginMode == LOGIN_AUTOMATIC) ? "tinylms/lmsempty.html" : "tinylms/lmslogin_" + languageSuffix + ".html",
                        }
                ));
            }
            w.flush();
            if (w.checkError()) {
                throw new IOException("unable to write index.html");
            }
        } finally {
            if (w != null) {
                w.flush();
            }
        }

        // Create the index.html file
    }

    /**
     * Exports the CAM to JavaScript using the specified PrintWriter.
     */
    private void writeIMSManifest(OutputStream outputStream, boolean isSCORMAdapter)
            throws IOException {
        PrintWriter out = new LFPrintWriter(
                new OutputStreamWriter(
                        new BufferedOutputStream(
                                outputStream
                        )
                )
        );

        out.println("/*");
        out.println(" * @(#)imsmanifest.js " + TinyLMSApp.getVersion());
        out.println(" *");
        out.println(" * Copyright (c) 2003-2006 Werner Randelshofer");
        out.println(" * Staldenmattweg 2, Immensee, CH-6405, Switzerland");
        out.println(" * All rights reserved.");
        out.println(" *");
        out.println(" * This software is the confidential and proprietary information of");
        out.println(" * Werner Randelshofer. (\"Confidential Information\").  You shall not");
        out.println(" * disclose such Confidential Information and shall use it only in");
        out.println(" * accordance with the terms of the license agreement you entered into");
        out.println(" * with Werner Randelshofer.");
        out.println(" */");
        out.println("/**");
        out.println(" * This file represents the Java Script version of a Content Aggregation Model (CAM).");
        out.println(" *");
        out.println(" * Reference:");
        out.println(" * ADL (2001a). Advanced Distributed Learning.");
        out.println(" * Sharable Content Object Reference Model (SCORM) Version 1.2.");
        out.println(" * Internet (2003-01-20): http://www.adlnet.org");
        out.println(" *");
        out.println(" * ADL (2001b). Advanced Distributed Learning.");
        out.println(" * SCORM 1.2 Runtime Environment.");
        out.println(" * Internet (2003-01-20): http://www.adlnet.org");
        out.println(" *");
        out.println(" * IMPORTANT! This file MUST be plain ASCII.");
        out.println(" * All non-ASCII characters MUST be encoded using HEXADECIMAL HTML entities.");
        out.println(" * Other encodings are not supported and will lead into incorrect display of the");
        out.println(" * labels.");
        out.println(" *");
        out.println(" * @author Werner Randelshofer, Staldenmattweg 2, Immensee, CH-6405, Switzerland");
        out.println(" * @version " + TinyLMSApp.getVersion());
        out.println(" */");
        out.println();
        out.println("// Content Aggregation Model");
        out.println("// -------------------------");

        IMSManifestDocument mani = getIMSManifestDocument();
        OrganizationsElement organizations = mani.getOrganizationsElement();
        OrganizationElement defaultOrganization = organizations.getDefaultOrganizationElement();
        organizations.setDefaultOrganizationElement(selectedOrganization);
        mani.exportToJavaScript(out, 0, new IdentifierGenerator());
        organizations.setDefaultOrganizationElement(defaultOrganization);

        out.println();
        out.println("// LMS Configuration");
        out.println("// -----------------");
        out.println();
        out.println("// Users");
        switch (loginMode) {
            case LOGIN_AUTOMATIC:
                out.println();
                out.println("var userArray = [");
                out.println("['guest', new User('guest',null,'Guest, Guest')]");
                out.println("];");
                out.println("API.userMap = new Map();");
                out.println("API.userMap.importFromArray(userArray);");
                break;
            case LOGIN_UNRESTRICTED:
                out.println();
                out.println("API.userMap = new Map();");
                break;
            case LOGIN_RESTRICTED:
                out.println();
                out.println("var userArray = [");
                for (int i = 0; i < usersTableModel.getRowCount(); i++) {
                    StudentModel user = usersTableModel.getRow(i);
                    if (i != 0) {
                        out.println(",");
                    }
                    out.print(
                            "  ['" + user.getID()
                                    + "', new User('" + user.getID() + "'"
                                    + "," + ((user.getPasswordDigest() == null) ? "null" : "'" + user.getPasswordDigest() + "'")
                                    + ",'" + ((user.getLastName() == null) ? user.getID() : user.getLastName())
                                    + ((user.getFirstName() == null) ? "" : "," + user.getFirstName() + ((user.getMiddleInitial() == null) ? "" : " " + user.getMiddleInitial())) + "'"
                                    + ")]"
                    );
                }
                out.println();
                out.println("];");
                out.println("API.userMap = new Map();");
                out.println("API.userMap.importFromArray(userArray);");
                break;
        }

        out.println();
        out.println("// Organization Structure");
        if (structure == STRUCTURE_LAYERED) {
            out.println("API.organizationStructure = API.STRUCTURE_LAYERED;");
            out.println();
            out.println("// Column Headers");
            ArrayList<String> columnTitles = selectedOrganization.getDistinctColumnTitles();
            out.println();
            out.println("API.camColumnNames = [");
            Iterator<String> i = columnTitles.iterator();
            while (i.hasNext()) {
                String title = i.next();
                out.print("\"" + Strings.escapeHTML(Strings.escapeUnicodeWithHTMLEntities(title)) + "\"");
                if (i.hasNext()) {
                    out.print(',');
                }
            }
            out.println("];");
        } else {
            out.println("API.organizationStructure = API.STRUCTURE_HIERARCHICAL;");
            out.println();
            out.println("// Column Headers");
            out.println("API.camColumnNames = null;");
        }
        if (!isNavBarVisible()) {
            out.println();
            out.println("// Tree buttons");
            out.println("API.isTreeButtonsVisible = true;");
        }

        out.println();
        out.println("// Sequencing");
        out.println("API.isAutomaticSequencing = " + (sequencing == SEQUENCING_AUTOMATIC) + ";");
        out.println();
        out.println("// Version info");
        out.println("API.version = '" + TinyLMSApp.getVersion() + "';");
        out.println();
        out.println("// Use of TinyLMS as a SCORM to SCORM Adapter");
        out.println("API.setSCORMAdapter(" + isSCORMAdapter + ");");
        out.println();
        out.println("// Debugging options");
        switch (loglevel) {
            case 0:
                break;
            case 1:
                out.println("API.loglevel = logger.API_SUCCESS;");
                out.println("logger.level = API.loglevel;");
                out.println("API.showDebugButtons = true;");
                break;
            case 2:
                out.println("API.loglevel = logger.INTERNAL;");
                out.println("logger.level = API.loglevel;");
                out.println("API.showDebugButtons = true;");
                break;
        }
        out.println("API.showBugInfoButton = " + isBugInfoButton + ";");

        out.flush();
    }

    public IMSManifestDocument getIMSManifestDocument() {
        return (((TreeNode) getRoot()).getChildCount() == 0) ? null : (IMSManifestDocument) ((TreeNode) getRoot()).getChildAt(0);
    }

    /**
     * Exports an imsmanifext.xml document, which can be used to use the
     * exported course as a content package which can be imported into a
     * parent LMS.
     */
    private void writeParentManifest(OutputStream outputStream, HashSet<String> courseFileNames)
            throws IOException {
        PrintWriter out = new LFPrintWriter(
                new OutputStreamWriter(
                        new BufferedOutputStream(
                                outputStream
                        )
                )
        );

        out.println("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n");

        out.println("<manifest identifier=\"TinyLMS\" version=\"1.2 2004-01-06\"");
        out.println("    xmlns=\"http://www.imsproject.org/xsd/imscp_rootv1p1p2\"");
        out.println("    xmlns:adlcp=\"http://www.adlnet.org/xsd/adlcp_rootv1p2\"");
        out.println("    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
        out.println("    xsi:schemaLocation=\"http://www.imsproject.org/xsd/imscp_rootv1p1p2 imscp_rootv1p1p2.xsd");
        out.println("        http://www.imsglobal.org/xsd/imsmd_rootv1p2p1 imsmd_rootv1p2p1.xsd");
        out.println("        http://www.adlnet.org/xsd/adlcp_rootv1p2 adlcp_rootv1p2.xsd\">");
        out.println("");
        out.println("   <metadata/>\n");
        out.println("");
        out.println("   <organizations default=\"org1\">");
        out.println("       <organization identifier=\"org1\">");
        out.println("           <title>Tiny LMS</title>");
        out.println("           <item identifier=\"item1\" identifierref=\"sco1\">");
        out.println("               <title>Tiny LMS</title>");
        out.println("           </item>");
        out.println("       </organization>");
        out.println("   </organizations>");
        out.println("");
        out.println("  	<resources>");
        out.println("       <resource identifier=\"sco1\" href=\"index.html\" adlcp:scormtype=\"sco\" type=\"webcontent\">");
        out.println("           <file href=\"index.html\"/>");
        out.println("           <file href=\"index.html\"/>");
        out.println("           <dependency identifierref=\"course\"/>");
        out.println("           <dependency identifierref=\"tinylms\"/>");
        out.println("       </resource>");


        out.println("       <resource identifier=\"course\" adlcp:scormtype=\"asset\" type=\"webcontent\">");
        for (String courseFileName : courseFileNames) {
            out.println("	        <file href=\"course/" + courseFileName + "\"/>");
        }
        out.println("       </resource>");

        HashSet<String> exclusionList = new HashSet<>(Arrays.asList("adlcp_rootv1p2.xsd",
                "ims_xml.xsd",
                "imscp_rootv1p1p2.xsd",
                "imsmd_rootv1p2p1.xsd"));
        out.println("       <resource identifier=\"tinylms\" adlcp:scormtype=\"asset\" type=\"webcontent\">");
        try (ZipInputStream in = new ZipInputStream(new BufferedInputStream(getClass().getResourceAsStream("/lmshtml.zip")))) {
            ZipEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                //We do not have to replace slashes in the entry name by
                //the file separator char, because we are writing an URL
                //entry.getName().replace('/', File.separatorChar));
                if (!entry.isDirectory()) {
                    out.println("	        <file href=\"" + entry.getName() + "\"/>");
                }
            }
        }
        out.println("           <file href=\"imsmanifest.js\"/>");
        out.println("       </resource>");


        out.println("   </resources>");
        out.println("</manifest>");

        out.flush();
    }

    /**
     * Removes all children from the root node.
     */
    public void clear() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) getRoot();
        for (int i = getChildCount(root) - 1; i > -1; i--) {
            removeNodeFromParent((MutableTreeNode) getChild(root, i));
        }
    }

    /**
     * Validates all nodes of the true.
     *
     * @return Returns true if all nodes are valid.
     */
    public boolean validate() {
        return getIMSManifestDocument().validateSubtree();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(propertyName, listener);
    }

    /**
     * Writes the contents of the DocumentModel into the output stream.
     * For peak performance, the output stream should be buffered.
     * <p>
     * Format:
     * <pre>
     * &lt;TinyLMS version="1"&gt;
     * &lt;organization structure=("hierarchical"|"layered")
     *                  sequencinge=("automatic"|"manual")/&gt;
     * &lt;authentication login=("automatic"|"unrestricted"|"restricted"/&gt;
     * &lt;debugging api=("true|false") infoButton=("true|false")/&gt;
     * &lt;locale&gt;&lt;/locale&gt;
     * &lt;layout&gt;
     *   &lt;toc width="*"/&gt;
     *   &lt;nav height="*" visible=("true"|"false")/&gt;
     *   &lt;page width="*" height="*"/&gt;
     * &lt;/layout&gt;
     * &lt;users&gt;
     *   &lt;student id=""&gt;
     *     &lt;firstName&gt;&lt;/firstName&gt;
     *     &lt;lastName&gt;&lt;/lastName&gt;
     *     &lt;middleInitial&gt;&lt;/middleInitial&gt;
     *     &lt;passwordDigest&gt;&lt;/passwordDigest&gt;
     *   &lt;/student&gt;
     * &lt;/students&gt;
     * &lt;/TinyLMS&gt;
     * </pre>
     */
    public void saveProperties(OutputStream out)
            throws IOException {
        try {
            String value;

            // Create the DOM Document. Our Markup language
            // is TinyLMS.
            DOMImplementation domImpl = DocumentBuilderFactory.newInstance().newDocumentBuilder().getDOMImplementation();
            org.w3c.dom.Document doc = domImpl.createDocument(null, "TinyLMS", domImpl.createDocumentType("TinyLMS", "TinyLmsPublicID", "TinyLMSSystemID"));
            org.w3c.dom.Element docRoot = doc.getDocumentElement();
            docRoot.setAttribute("version", "2");
            org.w3c.dom.Element elem, elem2, elem3;

            // ------------------------------------
            // Write Organization structure
            elem = doc.createElement("organization");
            elem.setAttribute("structure", (structure == STRUCTURE_HIERARCHICAL) ? "hierarchical" : "layered");
            elem.setAttribute("sequencing", (sequencing == SEQUENCING_AUTOMATIC) ? "automatic" : "manual");
            docRoot.appendChild(elem);

            // ------------------------------------
            // Write Authentication login mode
            elem = doc.createElement("authentication");
            value = (loginMode == LOGIN_AUTOMATIC) ? "automatic" : ((loginMode == LOGIN_UNRESTRICTED) ? "unrestricted" : "restricted");
            elem.setAttribute("login", value);
            docRoot.appendChild(elem);

            // ------------------------------------
            // Write Debugging mode
            elem = doc.createElement("debugging");
            elem.setAttribute("loglevel", Integer.toString(loglevel));
            elem.setAttribute("infoButton", Boolean.toString(isBugInfoButton));
            docRoot.appendChild(elem);


            // ------------------------------------
            // Write Locale
            elem = doc.createElement("locale");
            elem.appendChild(doc.createTextNode(locale.toString()));
            docRoot.appendChild(elem);

            // ------------------------------------
            // Write Layout
            elem = doc.createElement("layout");

            elem2 = doc.createElement("toc");
            elem2.setAttribute("width", getFramesetTocWidth());
            elem.appendChild(elem2);

            elem2 = doc.createElement("nav");
            elem2.setAttribute("height", getFramesetNavHeight());
            elem2.setAttribute("visible", isNavBarVisible() ? "true" : "false");
            elem.appendChild(elem2);

            elem2 = doc.createElement("page");
            elem2.setAttribute("width", getFramesetPageWidth());
            elem2.setAttribute("height", getFramesetPageHeight());
            elem.appendChild(elem2);

            docRoot.appendChild(elem);

            // ------------------------------------
            // Write Students
            elem = doc.createElement("users");

            for (int i = 0; i < usersTableModel.getRowCount(); i++) {
                StudentModel student = usersTableModel.getRow(i);
                elem2 = doc.createElement("student");
                elem2.setAttribute("id", student.getID());

                if (student.getFirstName() != null) {
                    elem3 = doc.createElement("firstName");
                    elem3.appendChild(doc.createTextNode(student.getFirstName()));
                    elem2.appendChild(elem3);
                }
                if (student.getLastName() != null) {
                    elem3 = doc.createElement("lastName");
                    elem3.appendChild(doc.createTextNode(student.getLastName()));
                    elem2.appendChild(elem3);
                }
                if (student.getMiddleInitial() != null) {
                    elem3 = doc.createElement("middleInitial");
                    elem3.appendChild(doc.createTextNode(student.getMiddleInitial()));
                    elem2.appendChild(elem3);
                }
                if (student.getPasswordDigest() != null) {
                    elem3 = doc.createElement("passwordDigest");
                    elem3.appendChild(doc.createTextNode(student.getPasswordDigest()));
                    elem2.appendChild(elem3);
                }
                elem.appendChild(elem2);
            }
            docRoot.appendChild(elem);

            // Write the document to the stream
            TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(out));
            out.flush();

        } catch (ParserConfigurationException e) {
            throw new IOException(e.toString());
        } catch (TransformerException e) {
            throw new IOException(e.toString());
        }

        // All changes are saved.
        //isSaved = true;
        //fireUndoableEdit(UndoRedoManager.DISCARD_ALL_EDITS);
    }

    /**
     * Reads the contents of the DocumentModel from the input stream.
     * For peak performance, the input stream should be buffered.
     * <p>
     * Format:
     * <pre>
     * &lt;TinyLMS version="1"&gt;
     * &lt;organization structure=("hierarchical"|"layered")
     *                  sequencinge=("automatic"|"manual")/&gt;
     * &lt;authentication login=("automatic"|"unrestricted"|"restricted"/&gt;
     * &lt;debugging api=("true|false") infoButton=("true|false")/&gt;
     * &lt;locale&gt;&lt;/locale&gt;
     * &lt;layout&gt;
     *   &lt;toc width="*"/&gt;
     *   &lt;nav height="*" visible=("true|false")/&gt;
     *   &lt;page width="*" height="*"/&gt;
     * &lt;/layout&gt;
     * &lt;users&gt;
     *   &lt;student id=""&gt;
     *     &lt;firstName&gt;&lt;/firstName&gt;
     *     &lt;lastName&gt;&lt;/lastName&gt;
     *     &lt;middleInitial&gt;&lt;/middleInitial&gt;
     *     &lt;passwordDigest&gt;&lt;/passwordDigest&gt;
     *   &lt;/student&gt;
     * &lt;/students&gt;
     * &lt;/TinyLMS&gt;
     * </pre>
     */
    public void loadProperties(InputStream in)
            throws IOException {
        try {
            // The DOM Document.
            org.w3c.dom.Element root, elem, elem2;
            String attrValue, text;
            Node node;

            // Read Document root
            if (in != null) {
                root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in).getDocumentElement();
            } else {
                root = null;
            }
            if (root != null) {
                if (!root.getTagName().equals("TinyLMS")) {
                    throw new IOException("Unsupported document type: " + root.getNodeName());
                }
                attrValue = DOMs.getAttribute(root, "version", "");
                if (!(attrValue.equals("") || attrValue.equals("1") || attrValue.equals("2"))) {
                    throw new IOException("Unsupported document version: " + attrValue +
                            ". Please get a newer version of TinyLMS.");
                }
            }
            // Read Organization element
            // -------------------------
            attrValue = DOMs.getElementAttribute(root, "organization", "structure", "hierarchical");
            setStructure(
                    (attrValue.equals("layered"))
                            ? STRUCTURE_LAYERED
                            : STRUCTURE_HIERARCHICAL
            );
            attrValue = DOMs.getElementAttribute(root, "organization", "sequencing", "automatic");
            setSequencing(
                    (attrValue.equals("automatic"))
                            ? SEQUENCING_AUTOMATIC
                            : SEQUENCING_MANUAL
            );

            // Read Authentication element
            // -------------------------
            attrValue = DOMs.getElementAttribute(root, "authentication", "login", "automatic");
            if (attrValue.equals("unrestricted")) {
                setLoginMode(LOGIN_UNRESTRICTED);
            } else if (attrValue.equals("restricted")) {
                setLoginMode(LOGIN_RESTRICTED);
            } else {
                setLoginMode(LOGIN_AUTOMATIC);
            }

            // Read Debugging element
            // -------------------------
            attrValue = DOMs.getElementAttribute(root, "debugging", "loglevel", "0");
            try {
                setLogLevel(Math.min(3, Math.max(0, Integer.decode(attrValue).intValue())));
            } catch (NumberFormatException e) {
                setLogLevel(0);
            }
            attrValue = DOMs.getElementAttribute(root, "debugging", "infoButton", "false");
            setBugInfoButton(Boolean.valueOf(attrValue).booleanValue());

            // Read Locale element
            // -------------------------
            text = DOMs.getText(DOMs.getElement(root, "locale"), "en");
            setLocale(new Locale(text, ""));

            // Read Layout element
            // -------------------------
            elem = DOMs.getElement(root, "layout");
            elem2 = DOMs.getElement(elem, "toc");

            setTOCVisible(DOMs.getAttribute(elem2, "enforceToc", "false").equals("true"));
            setTOCVisible(DOMs.getAttribute(elem2, "visible", "true").equals("true"));
            setFramesetTocWidth(DOMs.getAttribute(elem2, "width", "192"));

            elem2 = DOMs.getElement(elem, "nav");
            setFramesetNavHeight(DOMs.getAttribute(elem2, "height", "18"));
            setNavBarVisible(DOMs.getAttribute(elem2, "visible", "true").equals("true"));

            elem2 = DOMs.getElement(elem, "page");
            setFramesetPageHeight(DOMs.getAttribute(elem2, "height", "*"));
            setFramesetPageWidth(DOMs.getAttribute(elem2, "width", "*"));

            // Read Student elements
            // -------------------------
            usersTableModel.clear();
            org.w3c.dom.Element[] students = DOMs.getElements(DOMs.getElement(root, "users"), "student");
            for (int i = 0; i < students.length; i++) {
                StudentModel student = new StudentModel();
                student.setID(DOMs.getAttribute(students[i], "id", ""));
                student.setFirstName(DOMs.getElementText(students[i], "firstName", null));
                student.setLastName(DOMs.getElementText(students[i], "lastName", null));
                student.setMiddleInitial(DOMs.getElementText(students[i], "middleInitial", null));
                student.setPasswordDigest(DOMs.getElementText(students[i], "passwordDigest", null));
                usersTableModel.add(student);
            }

            //isSaved = true;
            //fireUndoableEdit(UndoRedoManager.DISCARD_ALL_EDITS);
        } catch (ParserConfigurationException e) {
            throw new IOException(e.toString());
        } catch (SAXException e) {
            throw new IOException(e.toString());
        }
    }


    public void add(MutableTreeNode parent, int index, MutableTreeNode node) {
    }

    public void createNodeAt(Object type, MutableTreeNode parent, int index) {
        throw new IllegalStateException();
    }

    public Transferable exportTransferable(MutableTreeNode[] nodes) {
        CompositeTransferable t = new CompositeTransferable();
        t.add(TreeModels.createHTMLTransferable(this, nodes));
        t.add(TreeModels.createPlainTransferable(this, nodes));
        return t;
    }

    public Action[] getNodeActions(MutableTreeNode[] nodes) {
        return new Action[0];
    }

    public Object getCreatableNodeType(Object parent) {
        return null;
    }

    public Object[] getCreatableNodeTypes(Object parent) {
        return new Object[0];
    }

    public int importTransferable(Transferable t, int action, MutableTreeNode parent, int index) throws UnsupportedFlavorException, IOException {
        return 0;
    }

    public boolean isNodeAddable(MutableTreeNode parent, int index) {
        return false;
    }

    public boolean isNodeEditable(MutableTreeNode node) {
        return false;
    }

    public boolean isImportable(DataFlavor[] transferFlavors, int action, MutableTreeNode parent, int index) {
        return false;
    }

    public boolean isNodeRemovable(MutableTreeNode node) {
        return false;
    }

    public boolean remove(MutableTreeNode node) {
        return false;
    }

    public void setValue(MutableTreeNode node, Object value) {
        throw new IllegalStateException();
    }

}
