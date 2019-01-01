/*
 * @(#)SCORMExporter.java  2.1  2011-06-05
 *
 * Copyright (c) 2006-2011 Hochschule Luzern, Fachstelle Neue Lernmedien,
 * Zentralstrasse 18, Postfach 2858, CH-6002 Lucerne, Switzerland
 * All rights reserved.
 *
 * The copyright of this software is owned by Hochschule Luzern (HSLU).
 * You may not use, copy or modify this software except in accordanc with
 * the license agreement you entered into with HSLU. For details see
 * accompanying license terms.
 */
package ch.randelshofer.gift.export.scorm;

import ch.randelshofer.gift.export.Exporter;
import ch.randelshofer.gift.parser.*;
import ch.randelshofer.gui.ProgressView;
import ch.randelshofer.io.*;
import ch.randelshofer.scorm.*;
import ch.randelshofer.scorm.cam.*;
import ch.randelshofer.text.TemplateEngine;
import ch.randelshofer.util.*;
import ch.randelshofer.zip.*;
import ch.randelshofer.zip.ZipFiles;
import java.util.*;
import java.util.prefs.Preferences;
import java.util.zip.*;
import java.io.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.xml.parsers.ParserConfigurationException;
import nanoxml.*;
import org.jhotdraw.util.ResourceBundleUtil;
import org.xml.sax.SAXException;

/**
 * Exports a collection of <code>Question</code>'s to a SCORM content package.
 *
 * @author Werner Randelshofer
 * @version 2.1 2011-06-06 Text in the prototype pages is now localized.
 * <br>2.0 2008-10-16 Rewrote matching pair question. Added support for
 * "None"-questions.
 * <br>1.4 2007-11-14 Mark cloze questions elements with classes, so
 * that they can be styled using CSS. Changed alignment of checkboxe images and
 * radio button images from absmiddle to absbottom. OId's must not start
 * with a digit.
 * <br>1.3.1 2007-02-02 Special treatment when all answers of a multiple
 * choice question are incorrect.
 * <br>1.3 2006-11-30 Added support for external SCORM resource.
 * <br>1.2.1 2006-11-27 Fixed judging of cloze questions.
 * <br>1.2 2006-11-02 Add support for non-unique matching pairs.
 * <br>1.1.1 2006-10-21 Fixed another judgment bug in cloze questions.
 * Removed alt text from feedback icon in cloze question.
 * <br>1.1 2006-10-15 Fixed judgement bugs in cloze question.
 * <br>1.1 2006-10-08 Removed HTML code from negative feedback text.
 * <br>1.0 2006-07-11 Created.
 */
public class SCORMExporter implements Exporter {

    private ResourceBundleUtil labels;
    private final static XMLElement dom = new XMLElement(null, false, false);
    private HashMap<Object, String> oidMap;
    private int oidLen;
    // FIXME - Find a solution to define the style sheet
    private String stylesheet = "style/style_hslu.css";
    //private String chapter = "";
    /** Random source for encryptClozeText */
    private static Random random = new Random();
    private boolean isPIFdefault;
    private ProgressView progress;
    private File baseDir;
    private String prefix;
    private String xmlPrefix = "_";
    private String organizationName = "Questions";

    /**
     * This class is used in method exportIMSManifest to create deep structures
     * of Item-Elements.
     */
    private static class ItemPathComponent {

        String title;
        XMLElement item;

        public ItemPathComponent(String title, XMLElement item) {
            this.title = title;
            this.item = item;
        }

        public boolean equals(Object o) {
            if (o instanceof ItemPathComponent) {
                ItemPathComponent that = (ItemPathComponent) o;
                return this.title.equals(that.title);
            }
            return false;
        }

        public int hashCode() {
            return title.hashCode();
        }
    }
    /**
     * Key = href
     * Value = content package
     */
    private HashMap<String, File> externalResourceRefs;

    /** Creates a new instance. */
    public SCORMExporter() {
    }

    public SCORMExporter(boolean isPIFdefault) {
        this();
        this.isPIFdefault = isPIFdefault;
    }

    public void exportToPIF(List<Question> questions, File file,
            String title, String stylesheet, Locale locale, String prefix) throws IOException {

        progress = new ProgressView("Exporting SCORM Package Interchange File", "...", 0, questions.size());
        this.stylesheet = stylesheet;
        this.prefix = (prefix == null) ? "" : prefix;
        labels = new ResourceBundleUtil(ResourceBundle.getBundle("ch.randelshofer.gift.Labels", locale));

        // For each question we need two oids: one for the item-Element and
        // one for the resource-Element.
        oidMap = new HashMap();
        oidLen = (int) Math.log10(questions.size() * 2) + 1;
        // The oid that we create for each question will be used for the
        // resource-Element.
        for (Question q : questions) {
            getOid(q);
        }

        // Remove "_scorm.zip" ending from file name. We add it back later.
        String baseName = file.getName();
        if (baseName.toLowerCase().endsWith(".zip")) {
            baseName = baseName.substring(0, baseName.length() - 4);
        }
        if (baseName.toLowerCase().endsWith("_scorm")) {
            baseName = baseName.substring(0, baseName.length() - 6);
        }
        setOrganizationName((title == null || title.length() == 0) ? baseName : title);

        ZipOut zout = null;
        try {
            zout = new ZipOutStream(new FileOutputStream(new File(file.getParentFile(), baseName + "_scorm.zip")));
            zout.putNextEntry(new ZipEntry("imsmanifest.xml"));
            externalResourceRefs = new HashMap<String, File>();
            exportIMSManifest(questions, getOrganizationName(), zout.getOutputStream());
            zout.closeEntry();
            for (Question q : questions) {
                progress.setNote("Exporting " + q.getDescriptiveTitle());
                progress.setProgress(progress.getProgress() + 1);
                exportQuestion(q, zout);
            }
            exportResources(zout);
            exportExternalResources(zout);
        } finally {
            if (zout != null) {
                zout.close();
            }
            progress.close();
        }
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String newValue) {
        organizationName = newValue;
    }

    public void exportToContentPackage(List<Question> questions, File dir,
            String title, String stylesheet, Locale locale, String prefix) throws IOException {

        this.stylesheet = stylesheet;
        this.prefix = (prefix == null) ? "" : prefix;
        labels = new ResourceBundleUtil(ResourceBundle.getBundle("ch.randelshofer.gift.Labels", locale));

        progress = new ProgressView("Exporting SCORM Content Package", "...", 0, questions.size());

        // For each question we need two oids: one for the item-Element and
        // one for the resource-Element.
        oidMap = new HashMap();
        oidLen = (int) Math.log10(questions.size() * 2) + 1;
        // The oid that we create for each question will be used for the
        // resource-Element.
        for (Question q : questions) {
            getOid(q);
        }

        if (dir.exists() && !dir.isDirectory()) {
            dir = dir.getParentFile();
        }
        dir.mkdirs();


        // FIXME - Find a solution to define the base name
        // Remove "_scorm" ending from file name. We add it back later.
        String baseName = dir.getName();
        if (baseName.toLowerCase().endsWith("_scorm")) {
            baseName = baseName.substring(0, baseName.length() - 6);
        }
        setOrganizationName((title == null || title.length() == 0) ? baseName : title);

        ZipOut zout = null;
        try {
            zout = new ZipOutDirectory(dir);
            zout.putNextEntry(new ZipEntry("imsmanifest.xml"));
            externalResourceRefs = new HashMap<String, File>();
            exportIMSManifest(questions, getOrganizationName(), zout.getOutputStream());
            zout.closeEntry();
            for (Question q : questions) {
                progress.setNote("Exporting " + q.getDescriptiveTitle());
                progress.setProgress(progress.getProgress() + 1);
                exportQuestion(q, zout);
            }
            exportResources(zout);
            exportExternalResources(zout);
        } finally {
            if (zout != null) {
                zout.close();
            }
            progress.close();
        }
    }

    private void exportIMSManifest(List<Question> questions, String name, OutputStream out) throws IOException {
        XMLElement manifest = dom.createElement("manifest");
        manifest.setAttribute("identifier", "_manifest");
        manifest.setAttribute("version", "1.0");
        manifest.setAttribute("xmlns", "http://www.imsproject.org/xsd/imscp_rootv1p1p2");
        manifest.setAttribute("xmlns:adlcp", "http://www.adlnet.org/xsd/adlcp_rootv1p2");
        manifest.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        manifest.setAttribute("xsi:schemaLocation", "http://www.imsglobal.org/xsd/imscp_rootv1p1p2 imscp_rootv1p1p2.xsd\n"
                + "      http://www.imsglobal.org/xsd/imsmd_rootv1p2p1 imsmd_rootv1p2p1.xsd\n"
                + "      http://www.adlnet.org/xsd/adlcp_rootv1p2 adlcp_rootv1p2.xsd");


        // MetaData
        // --------
        XMLElement metadata = dom.createElement("metadata");
        XMLElement schema = dom.createElement("schema");
        schema.setContent("IMS Content");
        metadata.addChild(schema);
        XMLElement schemaversion = dom.createElement("schemaversion");
        schemaversion.setContent("1.2");
        metadata.addChild(schemaversion);
        XMLElement record = dom.createElement("record");
        record.setAttribute("xmlns", "http://www.imsglobal.org/xsd/imsmd_rootv1p2p1");
        XMLElement general = dom.createElement("general");
        XMLElement title = dom.createElement("title");
        XMLElement langstring = dom.createElement("langstring");
        langstring.setAttribute("xml:lang", "de-CH");
        langstring.setContent(name);
        title.addChild(langstring);
        general.addChild(title);
        XMLElement description = dom.createElement("description");
        langstring = dom.createElement("langstring");
        langstring.setAttribute("xml:lang", "de-CH");
        langstring.setContent(name);
        description.addChild(langstring);
        general.addChild(description);
        XMLElement keywords = dom.createElement("keywords");
        langstring = dom.createElement("langstring");
        langstring.setAttribute("xml:lang", "de-CH");
        langstring.setContent(name);
        keywords.addChild(langstring);
        general.addChild(keywords);
        record.addChild(general);
        metadata.addChild(record);
        manifest.addChild(metadata);

        // Organizations
        // -------------
        XMLElement organizations = dom.createElement("organizations");
        organizations.setAttribute("default", "test1");
        XMLElement organization = dom.createElement("organization");
        organization.setAttribute("identifier", "test1");
        organization.setAttribute("structure", "hierarchical");
        title = dom.createElement("title");
        title.setContent(name);
        organization.addChild(title);

        TreePath previousItemPath = new TreePath(new ItemPathComponent(null, null));
        XMLElement chapter = null;
        for (Question q : questions) {
            XMLElement item = dom.createElement("item");
            item.setAttribute("identifier", getXMLid(item));
            item.setAttribute("identifierref", getXMLid(q));

            String[] titlePathComponents = (q.getTitle() == null) ? new String[]{q.getDescriptiveTitle()} : q.getTitle().split(";");
            for (int i = 0; i < titlePathComponents.length; i++) {
                titlePathComponents[i] = titlePathComponents[i].trim();
            }
            TreePath itemPath = new TreePath(new ItemPathComponent(null, null));
            XMLElement parent = organization;
            for (int i = 0; i < titlePathComponents.length - 1; i++) {
                if (i + 1 < previousItemPath.getPathCount()) {
                    ItemPathComponent previousComponent = (ItemPathComponent) previousItemPath.getPathComponent(i + 1);
                    if (previousComponent.title.equals(titlePathComponents[i])) {
                        parent = previousComponent.item;
                        itemPath = itemPath.pathByAddingChild(previousComponent);
                        continue;
                    } else {
                        // clear previous item path
                        previousItemPath = new TreePath(new ItemPathComponent(null, null));
                    }
                }

                XMLElement newParent = dom.createElement("item");
                newParent.setAttribute("identifier", getXMLid(newParent));
                title = dom.createElement("title");
                title.setContent(titlePathComponents[i]);
                newParent.addChild(title);
                parent.addChild(newParent);
                parent = newParent;
                itemPath = itemPath.pathByAddingChild(new ItemPathComponent(titlePathComponents[i], newParent));
            }
            previousItemPath = itemPath;

            title = dom.createElement("title");
            title.setContent(titlePathComponents[titlePathComponents.length - 1]);
            item.addChild(title);
            parent.addChild(item);
        }
        organizations.addChild(organization);
        manifest.addChild(organizations);

        // Resources
        // ---------
        XMLElement resources = dom.createElement("resources");
        // Question Resources
        for (Question q : questions) {
            XMLElement resource = dom.createElement("resource");
            resource.setAttribute("identifier", getXMLid(q));
            resource.setAttribute("adlcp:scormtype", "sco");
            resource.setAttribute("type", "webcontent");
            AnswerListType scormType = getSCORMType(q);
            if (scormType == null) {
                throw new IOException(labels.getFormatted("exporter.unsupportedQuestionType", q.toString()));
            }
            switch (scormType) {
                case EXTERNAL:
                    exportExternalIMSManifestResource(resource, q);
                    break;
                default:
                    String url = getURLid(q) + "_" + q.getDescriptiveURL() + "_sco.html";
                    resource.setAttribute("href", url);
                    XMLElement file = dom.createElement("file");
                    file.setAttribute("href", url);
                    resource.addChild(file);
                    XMLElement dependency = dom.createElement("dependency");
                    dependency.setAttribute("identifierref", "common");
                    resource.addChild(dependency);
                    break;
            }
            resources.addChild(resource);
        }
        // Common Resources
        XMLElement resource = dom.createElement("resource");
        resource.setAttribute("identifier", "common");
        resource.setAttribute("adlcp:scormtype", "asset");
        resource.setAttribute("type", "webcontent");
        ZipIn zin = null;
        try {
            zin = getSCORMTemplates();
            for (ZipEntry entry = zin.getNextEntry(); entry != null; entry = zin.getNextEntry()) {
                if (!entry.isDirectory()) {
                    XMLElement file = dom.createElement("file");
                    file.setAttribute("href", entry.getName());
                    resource.addChild(file);
                }
            }
        } finally {
            if (zin != null) {
                zin.close();
            }
        }
        resources.addChild(resource);

        manifest.addChild(resources);

        // Write to file
        // -------------
        PrintWriter w = new PrintWriter(new OutputStreamWriter(out, "UTF8"));
        w.write("<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>\n");
        manifest.print(w);
        w.flush();
    }

    public static ZipIn getSCORMTemplates() {
        Preferences prefs = Preferences.userNodeForPackage(SCORMExporter.class);
        String choice = prefs.get("scormTemplateChoice", "internal");
        if (choice.equals("file")) {
            try {
                return new ZipInStream(new FileInputStream(prefs.get("scormTemplateFile", "")));
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        } else if (choice.equals("directory")) {
            File dir = new File(prefs.get("scormTemplateDirectory", ""));
            if (dir.exists()) {
                return new ZipInDirectory(dir);
            }
        }
        return new ZipInStream(SCORMExporter.class.getResourceAsStream("/scormtemplates.zip"));
    }

    private void exportExternalIMSManifestResource(XMLElement resource, Question q) throws IOException {
        ExternalAnswer ea = (ExternalAnswer) ((AnswerList) q.getBody().get(0)).answers().get(0);

        // Parse the external reference
        String externalReference = ea.getExternalReference();
        if (!externalReference.startsWith("scorm:")) {
            throw new IOException("Illegal scheme:" + externalReference);
        }
        String[] parts = externalReference.substring(6).split("\\?");
        String path = parts[0];
        String[] queries = parts[1].split(";");
        HashMap<String, String> queryMap = new HashMap<String, String>();
        for (String query : queries) {
            String[] queryKeyValue = query.split("=");
            queryMap.put(queryKeyValue[0], queryKeyValue[1]);
        }

        // Fetch the content package
        File contentPackage = new File(baseDir, path);
        if (!contentPackage.exists()) {
            contentPackage = new File(baseDir.getParent(), path);
            if (!contentPackage.exists()) {
                throw new IOException("External SCORM Content Package not found: " + contentPackage);
            }
        }
        if (!queryMap.containsKey("id")) {
            throw new IOException("Id parameter missing: " + externalReference);
        }

        CourseModel cm = new CourseModel();
        try {
            if (contentPackage.isDirectory()) {
                cm.importContentPackage(contentPackage);
            } else {
                cm.importPIF(contentPackage);
            }
        } catch (SAXException ex) {
            IOException e = new IOException("Error parsing external SCORM Content Package: " + contentPackage);
            e.initCause(ex);
            throw e;
        } catch (ParserConfigurationException ex) {
            IOException e = new IOException("Error parsing external SCORM Content Package: " + contentPackage);
            e.initCause(ex);
            throw e;
        }

        if (cm.getIMSManifestDocument() == null) {
            throw new IOException("Missing file imsmanifest.xml in SCORM Content Package: " + contentPackage);
        }
        ResourcesElement resources = cm.getIMSManifestDocument().getResourcesElement();
        if (resources == null) {
            throw new IOException("Missing resources element in imsmanifest.xml in SCORM Content Package: " + contentPackage);
        }

        ResourceElement externalResource = resources.findResource(queryMap.get("id"));
        if (externalResource == null) {
            throw new IOException("Missing resource with identifier=\""
                    + queryMap.get("id")
                    + "\"in imsmanifest.xml in SCORM Content Package: " + contentPackage);
        }

        // Resolve all dependencies of the external resource element
        HashSet<String> dependencies = new HashSet<String>();
        HashSet<String> hrefs = new HashSet<String>();
        resolveExternalResourceDependency(contentPackage, resources, externalResource, dependencies, hrefs);

        // Write the resource element
        resource.setAttribute("href", externalResource.getHRef());
        ArrayList<String> sortedFiles = new ArrayList(hrefs);
        Collections.sort(sortedFiles);
        for (String href : sortedFiles) {
            XMLElement file = dom.createElement("file");
            file.setAttribute("href", href);
            resource.addChild(file);

            externalResourceRefs.put(href, contentPackage);
        }
    }

    private void resolveExternalResourceDependency(
            File contentPackage,
            ResourcesElement externalResources,
            ResourceElement externalResource, HashSet<String> dependencies, HashSet<String> hrefs) throws IOException {
        if (!dependencies.contains(externalResource.getIdentifier())) {
            dependencies.add(externalResource.getIdentifier());

            for (int i = 0, n = externalResource.getChildCount(); i < n; i++) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) externalResource.getChildAt(i);
                if (node instanceof FileElement) {
                    FileElement externalFileElement = (FileElement) node;
                    hrefs.add(externalFileElement.getHRef());
                } else if (node instanceof DependencyElement) {
                    DependencyElement externalDependency = (DependencyElement) node;
                    ResourceElement nextExternalResource = externalResources.findResource(externalDependency.getIdentifierref());
                    if (nextExternalResource == null) {
                        throw new IOException("Missing resource with identifier=\""
                                + externalDependency.getIdentifierref()
                                + "\" in imsmanifest.xml in SCORM Content Package:<br>" + contentPackage);
                    }
                    resolveExternalResourceDependency(contentPackage, externalResources, nextExternalResource, dependencies, hrefs);
                }
            }
        }
    }

    private void exportQuestion(Question question, ZipOut out) throws IOException {
        switch (getSCORMType(question)) {
            case SINGLE_CHOICE:
                exportSingleChoiceQuestion(question, out);
                break;
            case MULTIPLE_CHOICE:
                exportMultipleChoiceQuestion(question, out);
                break;
            case MATCHING_PAIR:
                exportMatchingPairQuestion(question, out);
                break;
            case CLOZE:
                exportClozeQuestion(question, out);
                break;
            case BOOL:
                exportBooleanQuestion(question, out);
                break;
            case EXTERNAL:
                exportExternalQuestion(question, out);
                break;
            case NONE:
                exportNoneQuestion(question, out);
                break;
        }
    }

    private void exportSingleChoiceQuestion(Question q, ZipOut out) throws IOException {
        // Get HTML Prototype
        Reader in = null;
        String htmlPrototype;
        try {
            in = new InputStreamReader(getClass().getResourceAsStream("prototype_scorm_sc.html"), "UTF8");
            htmlPrototype = Streams.toString(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }

        // Get Answer list
        AnswerList answerList = (AnswerList) q.getBody().get(1);

        // Generate HTML code for answer list
        StringBuilder htmlAnswer = new StringBuilder();
        for (int i = 1, n = answerList.answers().size(); i <= n; i++) {
            ChoiceAnswer ca = (ChoiceAnswer) answerList.answers().get(i - 1);
            htmlAnswer.append("<p class=\"spacingHalf\">");
            htmlAnswer.append("<span name=\"G01choice" + i + "\" style=\"margin-left:-30px\">");
            htmlAnswer.append("<a name=\"G01choice" + i + "Inp\" href=\"#\"\n");
            htmlAnswer.append("     onClick=\"G01.e['choice" + i + "'].update('onclick');return false\"\n");
            htmlAnswer.append("     onMouseOver=\"G01.e['choice" + i + "'].update('onmouseover');\"\n");
            htmlAnswer.append("     onMouseOut=\"G01.e['choice" + i + "'].update('onmouseout');\"\n");
            htmlAnswer.append("     onMouseDown=\"G01.e['choice" + i + "'].update('onmousedown');\"> ");
            htmlAnswer.append("<img src=\"question_images/radio.gif\" "
                    + //"alt=\"Choice "+i+"\" " + // Don't provide alt text, because IE screws it up!
                    "name=\"G01choice" + i + "Btn\" border=0 align=\"absbottom\">");
            htmlAnswer.append("</a>");
            htmlAnswer.append(encodeHTMLText(ca.getText()));
            htmlAnswer.append("</span>");
            htmlAnswer.append("</p>");
        }

        StringBuilder resultProcessing = new StringBuilder();
        StringBuilder unknownResponseProcessing = new StringBuilder();
        resultProcessing.append("function newG01() {\n");
        resultProcessing.append("    G01 = new MM_interaction('G01',0,0,0,null,0,1,0,'','','c','',0);\n");
        for (int i = 1, n = answerList.answers().size(); i <= n; i++) {
            ChoiceAnswer ca = (ChoiceAnswer) answerList.answers().get(i - 1);
            resultProcessing.append("    G01.add('ibtn','choice" + i + "',0,1," + (ca.isCorrect() || ca.getWeight() > 0 ? '1' : '0') + ",0,1,'sdhSDH');\n");
        }
        resultProcessing.append("    G01.init();\n");
        resultProcessing.append("    G01.am('segm','Segment: Check Time_',1,0);\n");
        resultProcessing.append("    G01.am('cond','Time At Limit_','G01.timeAtLimit == true',0);\n");
        resultProcessing.append("    G01.am('actn','Popup Message','MM_popupMsg(\\'You are out of time\\')','pm');\n");
        resultProcessing.append("    G01.am('actn','Set Interaction Properties: Disable Interaction','MM_setIntProps(\\'G01.setDisabled(true);\\')','sp');\n");
        resultProcessing.append("    G01.am('end');\n");
        resultProcessing.append("    G01.am('segm','Segment: Correctness_',1,0);\n");
        resultProcessing.append("    G01.am('cond','Correct_01','G01.correct == true',0);\n");
        resultProcessing.append("    G01.am('actn','Call JavaScript','MM_callJS(\\'question_positiveFeedback()\\')','');\n");
        resultProcessing.append("    G01.am('end');\n");
        resultProcessing.append("    G01.am('cond','Incorrect_','G01.correct == (false)',0);\n");
        resultProcessing.append("    G01.am('actn','Call JavaScript','MM_callJS(\\'question_clearFeedbackText()\\')','');\n");
        for (int i = 1, n = answerList.answers().size(); i <= n; i++) {
            ChoiceAnswer ca = (ChoiceAnswer) answerList.answers().get(i - 1);
            String feedbackText = (ca.getFeedbackText() != null) ? ca.getFeedbackText() : (ca.isCorrect() ? labels.getString("feedback.multipleChoice.mustBeChecked") : labels.getString("feedback.multipleChoice.mustNotBeChecked"));
            String str = "    G01.am('actn','Call JavaScript','MM_callJS(\\'question_addFeedbackText("
                    + "\\\\\\'choice" + i + "\\\\\\',"
                    + "G01.e[\\\\\\'choice"
                    + i + "\\\\\\'].isCorrect != G01.e[\\\\\\'choice"
                    + i + "\\\\\\'].value,\\\\\\'"
                    + encodeJavaScriptStringLiteral(feedbackText)
                    + "\\\\\\')\\')','');\n";
            resultProcessing.append(str);
            unknownResponseProcessing.append(str);
        }
        resultProcessing.append("    G01.am('actn','Call JavaScript','MM_callJS(\\'question_negativeFeedback()\\')','');\n");
        resultProcessing.append("    G01.am('end');\n");
        resultProcessing.append("    G01.am('cond','Unknown Response_','G01.knownResponse == false',0);\n");
        resultProcessing.append("    G01.am('actn','Call JavaScript','MM_callJS(\\'question_clearFeedbackText()\\')','');\n");
        resultProcessing.append(unknownResponseProcessing.toString());
        unknownResponseProcessing.setLength(0);
        /*
        for (int i=1, n = answerList.answers().size(); i <= n; i++) {
        ChoiceAnswer caj = (ChoiceAnswer) answerList.answers().get(i - 1);
        String feedbackText = (caj.getFeedbackText() != null) ? caj.getFeedbackText() : caj.getText();
        resultProcessing.append("G01.am('actn','Call JavaScript','MM_callJS(\\'question_addFeedbackText(G01.e[\\\\\\'choice" +
        i+"\\\\\\'].isCorrect != G01.e[\\\\\\'choice" +
        i+"\\\\\\'].value,\\\\\\'"+
        encodeJavaScriptStringLiteral("<span class=\"feedbackNegativeBullet\">"+i+".</span> "+feedbackText)+"\\\\\\')\\')','');\n");
        }*/
        resultProcessing.append("    G01.am('actn','Call JavaScript','MM_callJS(\\'question_negativeFeedback()\\')','');\n");
        resultProcessing.append("    G01.am('end');\n");
        resultProcessing.append("    G01.am('segm','Segment: Check Tries_',1,0);\n");
        resultProcessing.append("    G01.am('cond','Tries At Limit_','G01.triesAtLimit == true',0);\n");
        resultProcessing.append("    G01.am('actn','Set Interaction Properties: Disable Interaction','MM_setIntProps(\\'G01.setDisabled(true);\\')','sp');\n");
        resultProcessing.append("    G01.am('actn','Call JavaScript','MM_callJS(\\'question_showCorrectAnswer()\\')','');\n");
        resultProcessing.append("    G01.am('end');\n");
        resultProcessing.append("}\n");
        resultProcessing.append("  if (window.newG01 == null) window.newG01 = newG01;\n");
        resultProcessing.append("  if (!window.MM_initIntFns) window.MM_initIntFns = ''; window.MM_initIntFns += 'newG01();';\n");


        // Formatting parameters of the prototype
        String html = TemplateEngine.process(htmlPrototype,//
                "chapter", getChapterTitle(q),
                "title", getPageTitle(q),
                "middot",getPageTitle(q).length() > 0 ? "&middot;" : "",
                "stylesheet",stylesheet,
                "question",encodeHTMLText((String) q.getBody().get(0)),
                "answer",htmlAnswer.toString(),
                "resultProcessing",resultProcessing.toString(),
                "footer",(q.getId() != null) ? q.getId() : "",//
                //
                "instructions.title",labels.getString("instructions.title"),
                "instructions.task",labels.getString("instructions.task.singleChoice"),
                "instructions.correctAnswerTitle",labels.getString("instructions.correctAnswerTitle"),
                "instructions.correctAnswerTask",labels.getString("instructions.correctAnswerTask"),
                "instructions.incorrectAnswerTitle",labels.getString("instructions.incorrectAnswerTitle"),
                "instructions.incorrectAnswerTask",labels.getString("instructions.incorrectAnswerTask"),
                "judge",labels.getString("button.judge"),
                "skip",labels.getString("button.skip"),
                "reset",labels.getString("button.reset"),
                "next",labels.getString("button.next")//
                );

        out.putNextEntry(new ZipEntry(getURLid(q) + "_" + q.getDescriptiveURL() + "_sco.html"));
        out.write(html.getBytes("UTF8"));

        //System.out.println(html);

        out.closeEntry();
    }

    private void exportMultipleChoiceQuestion(Question q, ZipOut out) throws IOException {
        // Get HTML Prototype
        Reader in = null;
        String htmlPrototype;
        try {
            in = new InputStreamReader(getClass().getResourceAsStream("prototype_scorm_mc.html"), "UTF8");
            htmlPrototype = Streams.toString(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }

        // Get Answer list
        AnswerList answerList = (AnswerList) q.getBody().get(1);

        // Generate HTML code for answer list
        StringBuilder htmlAnswer = new StringBuilder();
        for (int i = 1, n = answerList.answers().size(); i <= n; i++) {
            ChoiceAnswer ca = (ChoiceAnswer) answerList.answers().get(i - 1);

            htmlAnswer.append("<p class=\"spacingHalf\">");
            htmlAnswer.append("<span name=\"G01choice" + i + "\" style=\"margin-left:-30px\"> ");
            htmlAnswer.append("<a name=\"G01choice" + i + "Inp\" href=\"#\"\n");
            htmlAnswer.append("     onClick=\"G01.e['choice" + i + "'].update('onclick');return false\"\n");
            htmlAnswer.append("     onMouseOver=\"G01.e['choice" + i + "'].update('onmouseover');\"\n");
            htmlAnswer.append("     onMouseOut=\"G01.e['choice" + i + "'].update('onmouseout');\"\n");
            htmlAnswer.append("     onMouseDown=\"G01.e['choice" + i + "'].update('onmousedown');\"> ");
            htmlAnswer.append("<img src=\"question_images/check.gif\" "
                    + /*"alt=\"Choice "+i+*/ // Don't use alt text. IE incorrectly displays it on mouse over!
                    "name=\"G01choice" + i + "Btn\" border=0 "
                    + "align=\"absbottom\">");
            htmlAnswer.append("</a>");
            htmlAnswer.append(encodeHTMLText(ca.getText()));
            htmlAnswer.append("</span>");
            htmlAnswer.append("</p>");
        }

        // Determine whether all answers are incorrect
        boolean allAnswersAreIncorrect = true;
        for (int i = 0, n = answerList.answers().size(); i < n; i++) {
            ChoiceAnswer ca = (ChoiceAnswer) answerList.answers().get(i);
            if (ca.isCorrect() || ca.getWeight() > 0) {
                allAnswersAreIncorrect = false;
                break;
            }
        }

        // Generate Javascript code for result processing
        StringBuilder resultProcessing = new StringBuilder();
        StringBuilder unknownResponseProcessing = new StringBuilder();
        resultProcessing.append("function newG01() {\n");
        resultProcessing.append("    G01 = new MM_interaction('G01',0,1,1," + ((allAnswersAreIncorrect) ? "1" : "0") + ",0,1,0,'','','c','',0);\n");
        for (int i = 1, n = answerList.answers().size(); i <= n; i++) {
            ChoiceAnswer ca = (ChoiceAnswer) answerList.answers().get(i - 1);
            resultProcessing.append("    G01.add('ibtn','choice" + i + "',0,1," + (ca.isCorrect() || ca.getWeight() > 0 ? '1' : '0') + ",0,1,'sdhSDH');\n");
        }
        resultProcessing.append("    G01.init();\n");
        resultProcessing.append("    G01.am('segm','Segment: Check Time_',1,0);\n");
        resultProcessing.append("    G01.am('cond','Time At Limit_','G01.timeAtLimit == true',0);\n");
        resultProcessing.append("    G01.am('actn','Popup Message','MM_popupMsg(\\'You are out of time\\')','pm');\n");
        resultProcessing.append("    G01.am('actn','Set Interaction Properties: Disable Interaction','MM_setIntProps(\\'G01.setDisabled(true);\\')','sp');\n");
        resultProcessing.append("    G01.am('end');\n");
        resultProcessing.append("    G01.am('segm','Segment: Correctness_',1,0);\n");
        resultProcessing.append("    G01.am('cond','Correct_01','G01.correct == true',0);\n");
        resultProcessing.append("    G01.am('actn','Call JavaScript','MM_callJS(\\'question_positiveFeedback()\\')','');\n");
        resultProcessing.append("    G01.am('end');\n");
        resultProcessing.append("    G01.am('cond','Incorrect_','G01.correct == (false)',0);\n");
        resultProcessing.append("    G01.am('actn','Call JavaScript','MM_callJS(\\'question_clearFeedbackText()\\')','');\n");
        for (int i = 1, n = answerList.answers().size(); i <= n; i++) {
            ChoiceAnswer ca = (ChoiceAnswer) answerList.answers().get(i - 1);
            String feedbackText = (ca.getFeedbackText() != null) ? ca.getFeedbackText() : (ca.isCorrect() || ca.getWeight() > 0 ? labels.getString("feedback.multipleChoice.mustBeChecked") : labels.getString("feedback.multipleChoice.mustNotBeChecked"));
            String str = "    G01.am('actn','Call JavaScript','MM_callJS(\\'question_addFeedbackText("
                    + "\\\\\\'choice" + i + "\\\\\\',"
                    + "G01.e[\\\\\\'choice"
                    + i + "\\\\\\'].isCorrect != G01.e[\\\\\\'choice"
                    + i + "\\\\\\'].value,\\\\\\'"
                    + encodeJavaScriptStringLiteral(/*"<span class=\"feedbackNegativeBullet\">"+i+".</span> "+*/feedbackText) + "\\\\\\')\\')','');\n";
            resultProcessing.append(str);
            unknownResponseProcessing.append(str);
        }
        resultProcessing.append("    G01.am('actn','Call JavaScript','MM_callJS(\\'question_negativeFeedback()\\')','');\n");
        resultProcessing.append("    G01.am('end');\n");
        resultProcessing.append("    G01.am('cond','Unknown Response_','G01.knownResponse == false',0);\n");
        resultProcessing.append("    G01.am('actn','Call JavaScript','MM_callJS(\\'question_clearFeedbackText()\\')','');\n");
        resultProcessing.append(unknownResponseProcessing.toString());
        unknownResponseProcessing.setLength(0);
        /*
        for (int i=1, n = answerList.answers().size(); i <= n; i++) {
        ChoiceAnswer caj = (ChoiceAnswer) answerList.answers().get(i - 1);
        String feedbackText = (caj.getFeedbackText() != null) ? caj.getFeedbackText() : caj.getText();
        resultProcessing.append("G01.am('actn','Call JavaScript','MM_callJS(\\'question_addFeedbackText(G01.e[\\\\\\'choice" +
        i+"\\\\\\'].isCorrect != G01.e[\\\\\\'choice" +
        i+"\\\\\\'].value,\\\\\\'"+
        encodeJavaScriptStringLiteral("<span class=\"feedbackNegativeBullet\">"+i+".</span> "+feedbackText)+"\\\\\\')\\')','');\n");
        }*/
        resultProcessing.append("    G01.am('actn','Call JavaScript','MM_callJS(\\'question_negativeFeedback()\\')','');\n");
        resultProcessing.append("    G01.am('end');\n");
        resultProcessing.append("    G01.am('segm','Segment: Check Tries_',1,0);\n");
        resultProcessing.append("    G01.am('cond','Tries At Limit_','G01.triesAtLimit == true',0);\n");
        resultProcessing.append("    G01.am('actn','Set Interaction Properties: Disable Interaction','MM_setIntProps(\\'G01.setDisabled(true);\\')','sp');\n");
        resultProcessing.append("    G01.am('actn','Call JavaScript','MM_callJS(\\'question_showCorrectAnswer()\\')','');\n");
        resultProcessing.append("    G01.am('end');\n");
        resultProcessing.append("}\n");
        resultProcessing.append("  if (window.newG01 == null) window.newG01 = newG01;\n");
        resultProcessing.append("  if (!window.MM_initIntFns) window.MM_initIntFns = ''; window.MM_initIntFns += 'newG01();';\n");


        // Formatting parameters of the prototype
        String html = TemplateEngine.process(htmlPrototype,//
                "chapter", getChapterTitle(q),
                "title", getPageTitle(q),
                "middot",getPageTitle(q).length() > 0 ? "&middot;" : "",
                "stylesheet",stylesheet,
                "question",encodeHTMLText((String) q.getBody().get(0)),
                "answer",htmlAnswer.toString(),
                "resultProcessing",resultProcessing.toString(),
                "footer",(q.getId() != null) ? q.getId() : "",//
                //
                "instructions.title",labels.getString("instructions.title"),
                "instructions.task",labels.getString("instructions.task.singleChoice"),
                "instructions.correctAnswerTitle",labels.getString("instructions.correctAnswerTitle"),
                "instructions.correctAnswerTask",labels.getString("instructions.correctAnswerTask"),
                "instructions.incorrectAnswerTitle",labels.getString("instructions.incorrectAnswerTitle"),
                "instructions.incorrectAnswerTask",labels.getString("instructions.incorrectAnswerTask"),
                "judge",labels.getString("button.judge"),
                "skip",labels.getString("button.skip"),
                "reset",labels.getString("button.reset"),
                "next",labels.getString("button.next")//
                );

        out.putNextEntry(new ZipEntry(getURLid(q) + "_" + q.getDescriptiveURL() + "_sco.html"));
        out.write(html.getBytes("UTF8"));

        //System.out.println(html);

        out.closeEntry();
    }

    private void exportMatchingPairQuestion(Question q, ZipOut out) throws IOException {
        // Get HTML Prototype
        Reader in = null;
        String htmlPrototype;
        try {
            in = new InputStreamReader(getClass().getResourceAsStream("prototype_scorm_MatchingPair.html"), "UTF8");
            htmlPrototype = Streams.toString(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }

        // Get Answer list
        AnswerList answerList = (AnswerList) q.getBody().get(1);

        // Create an array of shuffled indices
        // We use this to randomly place the matching pair keys on the page
        int[] shuffledIndices = new int[answerList.answers().size()];
        for (int i = 0, n = shuffledIndices.length; i < n; i++) {
            shuffledIndices[i] = i;
        }
        ArrayUtil.shuffle(shuffledIndices);

        // Generate HTML code for drag and drop divs and for the value divs.
        StringBuilder dragDivs = new StringBuilder();
        StringBuilder dropDivs = new StringBuilder();
        StringBuilder valueDivs = new StringBuilder();

        // Determine maximal length of the key texts
        int maxKeyTextLength = 0;
        for (int i = 1, n = answerList.answers().size(); i <= n; i++) {
            MatchingPairAnswer mpa = (MatchingPairAnswer) answerList.answers().get(i - 1);
            maxKeyTextLength = Math.max(maxKeyTextLength, mpa.getKey().length());
        }
        // Determine maximal length of the value texts
        int maxValueTextLength = 0;
        for (int i = 1, n = answerList.answers().size(); i <= n; i++) {
            MatchingPairAnswer mpa = (MatchingPairAnswer) answerList.answers().get(i - 1);
            maxValueTextLength = Math.max(maxValueTextLength, mpa.getValue().length());
        }

        // Compute horizontal coordinates, widths and spacings
        int keyWidth;
        int valueWidth;
        int dragX;
        int dropX;
        int valueX;

        if (maxValueTextLength < 3) {
            keyWidth = 200;
            valueWidth = 20;
            dragX = 0;
            dropX = 260;
            valueX = 460;
        } else if (maxValueTextLength < 4) {
            keyWidth = 190;
            valueWidth = 40;
            dragX = 0;
            dropX = 250;
            valueX = 440;
        } else {
            keyWidth = 140;
            valueWidth = 140;
            dragX = 0;
            dropX = 200;
            valueX = 340;
        }


        // Compute vertical coordinates, heights and spacings
        int top;
        int height;
        int dy;
        if (answerList.answers().size() < 4) {
            top = 80;
            dy = 400 / answerList.answers().size();
            height = 100;
            dy = Math.max((dy / 20) * 20, 120);
        } else if (answerList.answers().size() < 5) {
            top = 80;
            dy = 400 / answerList.answers().size();
            height = 80;
            dy = Math.max((dy / 20) * 20, 100);
        } else if (answerList.answers().size() < 6) {
            top = 80;
            dy = 400 / answerList.answers().size();
            height = 60;
            dy = Math.max((dy / 20) * 20, 80);
        } else if (answerList.answers().size() < 7) {
            top = 80;
            dy = 400 / answerList.answers().size();
            height = 40;
            dy = Math.max((dy / 20) * 20, 60);
        } else {
            top = 80;
            dy = 400 / answerList.answers().size();
            height = 20;
            dy = Math.max((dy / 20) * 20, 40);
        }


        for (int i = 1, n = answerList.answers().size(); i <= n; i++) {
            MatchingPairAnswer mpa = (MatchingPairAnswer) answerList.answers().get(i - 1);

            int dropY = top + (i - 1) * dy;
            int dragY = top + shuffledIndices[i - 1] * dy;

            dragDivs.append("  <div id=\"G01Drag" + i + "\" ");
            dragDivs.append("style=\"position:absolute; ");
            dragDivs.append("left:" + dragX + "px; top:" + dragY + "px; width:" + keyWidth + "px; height:" + height + "px;\">");
            dragDivs.append("<div class=\"dragText\">");
            dragDivs.append(encodeHTMLText(mpa.getKey()));
            dragDivs.append("</div></div>\n");

            dropDivs.append("  <div id=\"G01Drop" + i + "\" ");
            dropDivs.append("style=\"position:absolute; ");
            dropDivs.append("left:" + dropX + "px; top:" + dropY + "px; width:" + keyWidth + "px; height:" + height + "px;\">");
            dropDivs.append("</div>\n");

            valueDivs.append("  <div class=\"dropText\" ");
            valueDivs.append("style=\"position:absolute; ");
            valueDivs.append("left:" + valueX + "px; top:" + dropY + "px; width:" + valueWidth + "px; height:" + height + "px; ");
            valueDivs.append("z-index:0\">");
            valueDivs.append("<div>\n");
            valueDivs.append(encodeHTMLText(mpa.getValue()));
            valueDivs.append("</div>\n</div>\n");
        }

        // Generate feedback text
        HashMap<MatchingPairAnswer, String> feedbackTextMap = new HashMap<MatchingPairAnswer, String>();
        for (int i = 1, n = answerList.answers().size(); i <= n; i++) {
            MatchingPairAnswer ca = (MatchingPairAnswer) answerList.answers().get(i - 1);
            HashSet<String> matches = new HashSet<String>();
            matches.add(ca.getValue());
            for (int j = 1; j <= n; j++) {
                if (j != i) {
                    MatchingPairAnswer cai = (MatchingPairAnswer) answerList.answers().get(i - 1);
                    MatchingPairAnswer caj = (MatchingPairAnswer) answerList.answers().get(j - 1);
                    if (cai.getValue().equals(caj.getValue())
                            || cai.getKey().equals(caj.getKey())) {
                        matches.add(caj.getValue());
                    }
                }
            }
            if (matches.size() == 1) {
                feedbackTextMap.put(ca, ca.getFeedbackText() != null ? ca.getFeedbackText() : labels.getFormatted("feedback.matchingPair", ca.getKey(), ca.getValue()));
            } else {
                feedbackTextMap.put(ca, labels.getFormatted("feedback.matchingPair", ca.getKey(), ArrayUtil.implode(matches.toArray(), ", ")));
            }
        }

        // Generate Javascript code for result processing
        StringBuilder resultProcessing = new StringBuilder();
        for (int i = 1, n = answerList.answers().size(); i <= n; i++) {
            MatchingPairAnswer ca = (MatchingPairAnswer) answerList.answers().get(i - 1);
            String feedbackText = feedbackTextMap.get(ca);
            resultProcessing.append("  Question.addPair('G01Drag" + i + "','G01Drop" + i + "','" + encodeJavaScriptStringLiteral(feedbackText) + "');\n");
        }

        // Generate pairs for drag objects which match with multiple drop targets
        for (int i = 1, n = answerList.answers().size(); i <= n; i++) {
            for (int j = 1; j <= n; j++) {
                if (j != i) {
                    MatchingPairAnswer cai = (MatchingPairAnswer) answerList.answers().get(i - 1);
                    MatchingPairAnswer caj = (MatchingPairAnswer) answerList.answers().get(j - 1);
                    if (cai.getValue().equals(caj.getValue())
                            || cai.getKey().equals(caj.getKey())) {
                        String feedbackText = feedbackTextMap.get(cai);
                        resultProcessing.append("  Question.addPair('G01Drag" + i + "','G01Drop" + j + "','" + encodeJavaScriptStringLiteral(feedbackText) + "');\n");
                    }
                }
            }
        }


        String html = TemplateEngine.process(htmlPrototype,//
                "chapter", getChapterTitle(q),
                "title", getPageTitle(q),
                "middot",getPageTitle(q).length() > 0 ? "&middot;" : "",
                "stylesheet",stylesheet,
                "question",encodeHTMLText((String) q.getBody().get(0)),
                "dropFields",dropDivs.toString() + dragDivs.toString(),
                "resultProcessing",resultProcessing.toString(),
                "dragFields",valueDivs.toString(),
                "footer",(q.getId() != null) ? q.getId() : "",//
                //
                "instructions.title",labels.getString("instructions.title"),
                "instructions.task",labels.getString("instructions.task.singleChoice"),
                "instructions.correctAnswerTitle",labels.getString("instructions.correctAnswerTitle"),
                "instructions.correctAnswerTask",labels.getString("instructions.correctAnswerTask"),
                "instructions.incorrectAnswerTitle",labels.getString("instructions.incorrectAnswerTitle"),
                "instructions.incorrectAnswerTask",labels.getString("instructions.incorrectAnswerTask"),
                "judge",labels.getString("button.judge"),
                "skip",labels.getString("button.skip"),
                "reset",labels.getString("button.reset"),
                "next",labels.getString("button.next")//
                );

        out.putNextEntry(new ZipEntry(getURLid(q) + "_" + q.getDescriptiveURL() + "_sco.html"));
        out.write(html.getBytes("UTF8"));

        //System.out.println(html);

        out.closeEntry();
    }

    /**
     * Exports a cloze question.
     */
    private void exportClozeQuestion(Question q, ZipOut out) throws IOException {
        // Get HTML Prototype
        Reader in = null;
        String htmlPrototype;
        try {
            in = new InputStreamReader(getClass().getResourceAsStream("prototype_scorm_cloze.html"), "UTF8");
            htmlPrototype = Streams.toString(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }

        // Generate HTML code for cloze text
        StringBuilder htmlCloze = new StringBuilder();
        int count = 0;
        for (Object o : q.getBody()) {
            if (o instanceof String) {
                htmlCloze.append(encodeHTMLText((String) o));
            } else if (o instanceof AnswerList) {
                AnswerList al = (AnswerList) o;
                count++;
                htmlCloze.append("<form name=\"G01elem" + count + "\" onSubmit=\"return false\" "
                        + "style=\"display:inline\"\n>");
                switch (al.getType()) {
                    case NUMERIC:
                    case CLOZE:
                        htmlCloze.append("<input class=\"clozeInput\" name=\"G01elem" + count + "Inp\" "
                                + "type=\"text\" onBlur=\"G01.e['elem" + count + "'].update()\" "
                                + "onFocus=\"G01.e['elem" + count + "'].focus()\" value=\"\"\n>");
                        break;
                    case BOOL:
                        htmlCloze.append("<select class=\"clozeSelect\" name=\"G01elem" + count + "Inp\" "
                                + "onBlur=\"G01.e['elem" + count + "'].update()\" "
                                + "onFocus=\"G01.e['elem" + count + "'].focus()\" >\n");
                        htmlCloze.append("  <option value=\"\"></option>\n");
                        htmlCloze.append("  <option class=\"clozeOption\" value=\"" + labels.getString("exporter.booleanAnswerTrue")
                                + "\">" + labels.getString("exporter.booleanAnswerTrue") + "</option>");
                        htmlCloze.append("  <option class=\"clozeOption\" value=\"" + labels.getString("exporter.booleanAnswerFalse")
                                + "\">" + labels.getString("exporter.booleanAnswerFalse") + "</option>");
                        htmlCloze.append("</select>");
                        break;
                    case SINGLE_CHOICE:
                        htmlCloze.append("<select class=\"clozeSelect\" name=\"G01elem" + count + "Inp\" "
                                + "onBlur=\"G01.e['elem" + count + "'].update()\" "
                                + "onFocus=\"G01.e['elem" + count + "'].focus()\" >\n");
                        htmlCloze.append("  <option value=\"\"></option>\n");
                        for (Answer a : al.answers()) {
                            ChoiceAnswer ca = (ChoiceAnswer) a;
                            htmlCloze.append("  <option class=\"clozeOption\" value=\"" + ca.getText()
                                    + "\">" + ca.getText() + "</option>\n");
                        }
                        htmlCloze.append("</select>");
                        break;
                    default:
                        throw new IOException("Unsupported answer type in cloze question " + ((AnswerList) o).getType());
                }
                htmlCloze.append("</form>");
                htmlCloze.append("<img name=\"G01elem" + count + "Img\" "
                        + "src=\"question_images/cloze.gif\" border=0 "
                        + "align=\"absbottom\" "
                        + //"alt=\"Element "+count+"\""+ // Don't use alt, because IE wrongly displays it in the tool tip
                        "\n>");
            }
        }

        // Generate Javascript code for result processing
        StringBuilder resultProcessing = new StringBuilder();
        // We need to do some of the result processing twice:
        StringBuilder resultProcessing2 = new StringBuilder();
        String rp;
        resultProcessing.append("function newG01() {\n");
        resultProcessing.append("    G01 = new MM_interaction('G01',0,0,1,0,0,1,0,'','','f','',0);\n");
        count = 0;
        for (Object o : q.getBody()) {
            if (o instanceof AnswerList) {
                AnswerList al = (AnswerList) o;
                count++;
                switch (al.getType()) {
                    case BOOL:
                        resultProcessing.append("    G01.add('text','elem" + count + "','');\n");
                        BooleanAnswer ba = (BooleanAnswer) al.answers().getFirst();
                        resultProcessing2.append("    G01.add('textComp','elem" + count + "','Response1','"
                                + encodeJavaScriptStringLiteral(encryptClozeText(labels.getString("exporter.booleanAnswerTrue")))
                                + "',"
                                + (ba.isTrue() ? "1" : "0")
                                + ",0,0,1);\n");
                        resultProcessing2.append("    G01.add('textComp','elem" + count + "','Response2','"
                                + encodeJavaScriptStringLiteral(encryptClozeText(labels.getString("exporter.booleanAnswerFalse")))
                                + "',"
                                + (ba.isTrue() ? "0" : "1")
                                + ",0,0,1);\n");
                        break;
                    case SINGLE_CHOICE:
                    case NUMERIC:
                    case CLOZE:
                        resultProcessing.append("    G01.add('text','elem" + count + "','');\n");
                        int aCount = 0;
                        for (Answer a : al.answers()) {
                            aCount++;
                            if (a instanceof ChoiceAnswer) {
                                ChoiceAnswer ca = (ChoiceAnswer) a;
                                if (ca.isCorrect()) {
                                    resultProcessing2.append("    G01.add('textComp','elem" + count + "','Response"
                                            + aCount
                                            + "','"
                                            + encodeJavaScriptStringLiteral(encryptClozeText(ca.getText()))
                                            + "',"
                                            + (ca.isCorrect() ? "1" : "0")
                                            + ",0,0,1);\n");
                                }
                            } else if (a instanceof NumberAnswer) {
                                NumberAnswer na = (NumberAnswer) a;
                                resultProcessing2.append("G01.add('textComp','elem" + count + "','Response"
                                        + aCount
                                        + "','"
                                        + encodeJavaScriptStringLiteral(encryptClozeText(na.getNumberAsString()))
                                        + "',1,0,0,1);\n");
                            } else if (a instanceof IntervalAnswer) {
                                IntervalAnswer sa = (IntervalAnswer) a;
                                resultProcessing2.append("    G01.add('textComp','elem" + count + "','Response"
                                        + aCount
                                        + "','"
                                        + encodeJavaScriptStringLiteral(encryptClozeText(sa.getMinAsString()))
                                        + "',1,0,0,1);\n");
                            } else {
                                throw new IOException("Unsupported answer type in cloze question " + a);
                            }
                        }
                        break;
                    default:
                        throw new IOException("Unsupported answer type in cloze question " + ((AnswerList) o).getType());
                }
            }
        }
        resultProcessing.append(resultProcessing2.toString());
        resultProcessing2.setLength(0);
        resultProcessing.append("    G01.init();\n");
        resultProcessing.append("    G01.am('segm','Segment: Check Time_',1,1);\n");
        resultProcessing.append("    G01.am('cond','Time At Limit_','G01.timeAtLimit == true',0);\n");
        resultProcessing.append("    G01.am('actn','Popup Message','MM_popupMsg(\\'You are out of time\\')','pm');\n");
        resultProcessing.append("    G01.am('actn','Set Interaction Properties: Disable Interaction','MM_setIntProps(\\'G01.setDisabled(true);\\')','sp');\n");
        resultProcessing.append("    G01.am('end');\n");
        resultProcessing.append("    G01.am('segm','Segment: Correctness_',1,0);\n");
        resultProcessing.append("    G01.am('cond','Correct_01','G01.correct == true',0);\n");
        resultProcessing.append("    G01.am('actn','Call JavaScript','MM_callJS(\\'question_positiveFeedback()\\')','');\n");
        resultProcessing.append("    G01.am('end');\n");
        resultProcessing.append("    G01.am('cond','Incorrect_01','G01.correct == false',0);\n");
        resultProcessing.append("    G01.am('actn','Call JavaScript','MM_callJS(\\'question_clearFeedbackText()\\')','');\n");
        count = 0;
        for (Object o : q.getBody()) {
            if (o instanceof AnswerList) {
                AnswerList al = (AnswerList) o;
                count++;
                switch (al.getType()) {
                    case BOOL: {
                        BooleanAnswer ba = (BooleanAnswer) al.answers().getFirst();
                        String feedback = (ba.getFeedbackText() != null) ? /*"<span class=\"feedbackNegativeBullet\">"+count+".</span> "+*/ ba.getFeedbackText() : labels.getString(
                                ba.isTrue() ? "exporter.booleanAnswerTrue" : "exporter.booleanAnswerFalse");
                        rp = "    G01.am('actn','Call JavaScript',"
                                + "'MM_callJS(\\'question_addFeedbackText(\\\\\\'elem" + count
                                + "\\\\\\',null,\\\\\\'"
                                + encodeJavaScriptStringLiteral(feedback)
                                + "\\\\\\')\\')','');\n";
                        resultProcessing.append(rp);
                        resultProcessing2.append(rp);
                        break;
                    }
                    case SINGLE_CHOICE:
                    case NUMERIC:
                    case CLOZE: {
                        int aCount = 0;
                        String unknownResponseFeedback = null;
                        // Provide feedback for known answers
                        for (Answer a : al.answers()) {
                            aCount++;
                            rp = null;
                            if (a instanceof ChoiceAnswer) {
                                ChoiceAnswer ca = (ChoiceAnswer) a;
                                if (ca.isCorrect()) {
                                    // Provide feedback for the correct answer and unknown answers
                                    if (unknownResponseFeedback == null) {
                                        unknownResponseFeedback = /*"<span class=\"feedbackNegativeBullet\">"+count+".</span> "+*/ ((ca.getFeedbackText() != null) ? ca.getFeedbackText() : ca.getText());
                                    }
                                } else if (ca.getFeedbackText() != null) {
                                    rp = "    G01.am('actn','Call JavaScript','MM_callJS(\\'question_addFeedbackText(\\\\\\'elem"
                                            + count
                                            + "\\\\\\',\\\\\\'Response"
                                            + aCount
                                            + "\\\\\\',\\\\\\'"
                                            + encodeJavaScriptStringLiteral(
                                            /*"<span class=\"feedbackNegativeBullet\">"+count+".</span> "+*/ca.getFeedbackText())
                                            + "\\\\\\')\\')','');\n";
                                }
                            } else if (a instanceof NumberAnswer) {
                                NumberAnswer na = (NumberAnswer) a;
                                if (unknownResponseFeedback == null) {
                                    unknownResponseFeedback = /*"<span class=\"feedbackNegativeBullet\">1.</span> "+*/ ((na.getFeedbackText() != null) ? na.getFeedbackText() : na.getNumberAsString());
                                } else if (na.getFeedbackText() != null) {
                                    rp = "    G01.am('actn','Call JavaScript','MM_callJS(\\'question_addFeedbackText(\\\\\\'elem"
                                            + count
                                            + "\\\\\\',\\\\\\'Response"
                                            + aCount
                                            + "\\\\\\',\\\\\\'"
                                            + encodeJavaScriptStringLiteral(
                                            /*"<span class=\"feedbackNegativeBullet\">"+count+".</span> "+*/na.getFeedbackText())
                                            + "\\\\\\')\\')','');\n";
                                }
                            } else if (a instanceof IntervalAnswer) {
                                IntervalAnswer sa = (IntervalAnswer) a;
                                if (unknownResponseFeedback == null) {
                                    unknownResponseFeedback = /*"<span class=\"feedbackNegativeBullet\">1.</span> "+*/ ((sa.getFeedbackText() != null) ? sa.getFeedbackText() : sa.getMinAsString());
                                } else if (sa.getFeedbackText() != null) {
                                    rp = "    G01.am('actn','Call JavaScript','MM_callJS(\\'question_addFeedbackText(\\\\\\'elem"
                                            + count
                                            + "\\\\\\',\\\\\\'Response"
                                            + aCount
                                            + "\\\\\\',\\\\\\'"
                                            + encodeJavaScriptStringLiteral(
                                            /*"<span class=\"feedbackNegativeBullet\">"+count+".</span> "+*/sa.getFeedbackText())
                                            + "\\\\\\')\\')','');\n";
                                }
                            } else {
                                throw new IOException("Unsupported answer type in cloze question " + a);
                            }
                            if (rp != null) {
                                resultProcessing.append(rp);
                                resultProcessing2.append(rp);
                            }
                        }

                        rp = "    G01.am('actn','Call JavaScript',"
                                + "'MM_callJS(\\'question_addFeedbackText(\\\\\\'elem" + count
                                + "\\\\\\',null,\\\\\\'"
                                + encodeJavaScriptStringLiteral(unknownResponseFeedback)
                                + "\\\\\\')\\')','');\n";
                        resultProcessing.append(rp);
                        resultProcessing2.append(rp);

                        break;
                    }
                    default:
                        throw new IOException("Unsupported answer type in cloze question " + ((AnswerList) o).getType());
                }
            }
        }
        resultProcessing.append("    G01.am('actn','Call JavaScript','MM_callJS(\\'question_negativeFeedback()\\')','');\n");
        resultProcessing.append("    G01.am('end');\n");
        resultProcessing.append("    G01.am('cond','Unknown Response_','G01.knownResponse == false',0);\n");
        resultProcessing.append("    G01.am('actn','Call JavaScript','MM_callJS(\\'question_clearFeedbackText()\\')','');\n");
        resultProcessing.append(resultProcessing2);
        resultProcessing.append("    G01.am('actn','Call JavaScript','MM_callJS(\\'question_negativeFeedback()\\')','');\n");
        resultProcessing.append("    G01.am('end');\n");
        resultProcessing.append("    G01.am('segm','Segment: Check Tries_',1,1);\n");
        resultProcessing.append("    G01.am('cond','Tries At Limit_','G01.triesAtLimit == true',0);\n");
        resultProcessing.append("    G01.am('actn','Set Interaction Properties: Disable Interaction','MM_setIntProps(\\'G01.setDisabled(true);\\')','sp');\n");
        resultProcessing.append("    G01.am('actn','Call JavaScript','MM_callJS(\\'question_showCorrectAnswer()\\')','');\n");
        resultProcessing.append("    G01.am('end');\n");
        resultProcessing.append("  }\n");
        resultProcessing.append("  if (window.newG01 == null) window.newG01 = newG01;\n");
        resultProcessing.append("  if (!window.MM_initIntFns) window.MM_initIntFns = ''; window.MM_initIntFns += 'newG01();';\n");


        // Formatting parameters of the prototype
        String html = TemplateEngine.process(htmlPrototype,//
                "chapter", getChapterTitle(q),
                "title", getPageTitle(q),
                "middot",getPageTitle(q).length() > 0 ? "&middot;" : "",
                "stylesheet",stylesheet,
                "clozeText",htmlCloze.toString(),
                "resultProcessing",resultProcessing.toString(),
                //
                "instructions.title",labels.getString("instructions.title"),
                "instructions.task",labels.getString("instructions.task.singleChoice"),
                "instructions.correctAnswerTitle",labels.getString("instructions.correctAnswerTitle"),
                "instructions.correctAnswerTask",labels.getString("instructions.correctAnswerTask"),
                "instructions.incorrectAnswerTitle",labels.getString("instructions.incorrectAnswerTitle"),
                "instructions.incorrectAnswerTask",labels.getString("instructions.incorrectAnswerTask"),
                "judge",labels.getString("button.judge"),
                "skip",labels.getString("button.skip"),
                "reset",labels.getString("button.reset"),
                "next",labels.getString("button.next")//
                );

        out.putNextEntry(new ZipEntry(getURLid(q) + "_" + q.getDescriptiveURL() + "_sco.html"));
        out.write(html.getBytes("UTF8"));

        //System.out.println(html);

        out.closeEntry();
    }

    private void exportExternalQuestion(Question q, ZipOut out) throws IOException {
        // Get Answer list
        AnswerList answerList = (AnswerList) q.getBody().get(0);
        ExternalAnswer ea = (ExternalAnswer) answerList.answers().get(0);

    }

    private void exportBooleanQuestion(Question q, ZipOut out) throws IOException {
        // Get HTML Prototype
        // We use the same HTML prototype like the one for Multiple-Choice Single-Answer questions
        Reader in = null;
        String htmlPrototype;
        try {
            in = new InputStreamReader(getClass().getResourceAsStream("prototype_scorm_sc.html"), "UTF8");
            htmlPrototype = Streams.toString(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }

        // Get Answer list
        AnswerList answerList = (AnswerList) q.getBody().get(1);

        // Generate HTML code for answer list
        StringBuilder htmlAnswer = new StringBuilder();
        BooleanAnswer ba = (BooleanAnswer) answerList.answers().get(0);
        for (int i = 1; i <= 2; i++) {
            htmlAnswer.append("<p class=\"spacingHalf\">");
            htmlAnswer.append("<span name=\"G01choice" + i + "\" style=\"margin-left:-30px\">");
            htmlAnswer.append("<a name=\"G01choice" + i + "Inp\" href=\"#\"\n");
            htmlAnswer.append("     onClick=\"G01.e['choice" + i + "'].update('onclick');return false\"\n");
            htmlAnswer.append("     onMouseOver=\"G01.e['choice" + i + "'].update('onmouseover');\"\n");
            htmlAnswer.append("     onMouseOut=\"G01.e['choice" + i + "'].update('onmouseout');\"\n");
            htmlAnswer.append("     onMouseDown=\"G01.e['choice" + i + "'].update('onmousedown');\"> ");
            htmlAnswer.append("<img src=\"question_images/radio.gif\" name=\"G01choice" + i + "Btn\" border=0 "
                    + "align=\"absbottom\">");
            htmlAnswer.append("</a>");
            htmlAnswer.append(labels.getString((i == 1) ? "exporter.booleanAnswerTrue" : "exporter.booleanAnswerFalse"));
            htmlAnswer.append("</span>");
            htmlAnswer.append("</p>");
        }

        // Generate Javascript code for result processing
        StringBuilder resultProcessing = new StringBuilder();
        StringBuilder unknownResponseProcessing = new StringBuilder();
        resultProcessing.append("function newG01() {\n");
        resultProcessing.append("    G01 = new MM_interaction('G01',0,0,0,null,0,1,0,'','','c','',0);\n");
        if (ba.isTrue()) {
            for (int i = 1; i <= 2; i++) {
                resultProcessing.append("    G01.add('ibtn','choice" + i + "',0,1," + ((i == 1) ? '1' : '0') + ",0,1,'sdhSDH');\n");
            }
        } else {
            for (int i = 1; i <= 2; i++) {
                resultProcessing.append("    G01.add('ibtn','choice" + i + "',0,1," + ((i == 2) ? '1' : '0') + ",0,1,'sdhSDH');\n");
            }
        }
        // FIXME - Result processing must be the same as with single choice question!
        resultProcessing.append("    G01.init();\n");
        resultProcessing.append("    G01.am('segm','Segment: Check Time_',1,1);\n");
        resultProcessing.append("    G01.am('cond','Time At Limit_','G01.timeAtLimit == true',0);\n");
        resultProcessing.append("    G01.am('actn','Popup Message','MM_popupMsg(\\'You are out of time\\')','pm');\n");
        resultProcessing.append("    G01.am('actn','Set Interaction Properties: Disable Interaction','MM_setIntProps(\\'G01.setDisabled(true);\\')','sp');\n");
        resultProcessing.append("    G01.am('end');\n");
        resultProcessing.append("    G01.am('segm','Segment: Correctness_',1,0);\n");
        resultProcessing.append("    G01.am('cond','Correct_01','G01.correct == true',0);\n");
        resultProcessing.append("    G01.am('actn','Call JavaScript','MM_callJS(\\'question_positiveFeedback()\\')','');\n");
        resultProcessing.append("    G01.am('end');\n");
        resultProcessing.append("    G01.am('cond','Incorrect_','G01.correct == (false)',0);\n");
        resultProcessing.append("    G01.am('actn','Call JavaScript','MM_callJS(\\'question_clearFeedbackText()\\')','');\n");
        String feedbackText = (ba.getFeedbackText() != null) ? ba.getFeedbackText() : (ba.isTrue() ? labels.getString("feedback.multipleChoice.mustBeChecked") : labels.getString("feedback.multipleChoice.mustNotBeChecked"));
        String str = "    G01.am('actn','Call JavaScript','MM_callJS(\\'question_addFeedbackText("
                + "\\\\\\'choice1\\\\\\',"
                + "G01.e[\\\\\\'choice1\\\\\\'].isCorrect != G01.e[\\\\\\'choice1\\\\\\'].value,\\\\\\'"
                + encodeJavaScriptStringLiteral(feedbackText)
                + "\\\\\\')\\')','');\n";
        resultProcessing.append(str);
        unknownResponseProcessing.append(str);
        feedbackText = (ba.getFeedbackText() != null) ? ba.getFeedbackText() : (!ba.isTrue() ? labels.getString("feedback.multipleChoice.mustBeChecked") : labels.getString("feedback.multipleChoice.mustNotBeChecked"));
        str = "    G01.am('actn','Call JavaScript','MM_callJS(\\'question_addFeedbackText("
                + "\\\\\\'choice2\\\\\\',"
                + "G01.e[\\\\\\'choice2\\\\\\'].isCorrect != G01.e[\\\\\\'choice2\\\\\\'].value,\\\\\\'"
                + encodeJavaScriptStringLiteral(feedbackText)
                + "\\\\\\')\\')','');\n";
        resultProcessing.append(str);
        unknownResponseProcessing.append(str);
        resultProcessing.append("    G01.am('actn','Call JavaScript','MM_callJS(\\'question_negativeFeedback()\\')','');\n");
        resultProcessing.append("    G01.am('end');\n");
        resultProcessing.append("    G01.am('cond','Unknown Response_','G01.knownResponse == false',0);\n");
        resultProcessing.append("    G01.am('actn','Call JavaScript','MM_callJS(\\'question_clearFeedbackText()\\')','');\n");
        resultProcessing.append(unknownResponseProcessing);
        unknownResponseProcessing.setLength(0);
        resultProcessing.append("    G01.am('actn','Call JavaScript','MM_callJS(\\'question_negativeFeedback()\\')','');\n");
        resultProcessing.append("    G01.am('end');\n");
        resultProcessing.append("    G01.am('segm','Segment: Check Tries_',1,0);\n");
        resultProcessing.append("    G01.am('cond','Tries At Limit_','G01.triesAtLimit == true',0);\n");
        resultProcessing.append("    G01.am('actn','Set Interaction Properties: Disable Interaction','MM_setIntProps(\\'G01.setDisabled(true);\\')','sp');\n");
        resultProcessing.append("    G01.am('actn','Call JavaScript','MM_callJS(\\'question_showCorrectAnswer()\\')','');\n");
        resultProcessing.append("    G01.am('end');\n");
        resultProcessing.append("}\n");
        resultProcessing.append("  if (window.newG01 == null) window.newG01 = newG01;\n");
        resultProcessing.append("  if (!window.MM_initIntFns) window.MM_initIntFns = ''; window.MM_initIntFns += 'newG01();';\n");


        // Formatting parameters of the prototype
        String html = TemplateEngine.process(htmlPrototype,//
                "chapter", getChapterTitle(q),
                "title", getPageTitle(q),
                "middot",getPageTitle(q).length() > 0 ? "&middot;" : "",
                "stylesheet",stylesheet,
                "question",encodeHTMLText((String) q.getBody().get(0)),
                "answer",htmlAnswer.toString(),
                "resultProcessing",resultProcessing.toString(),
                "footer",(q.getId() != null) ? q.getId() : "",//
                //
                "instructions.title",labels.getString("instructions.title"),
                "instructions.task",labels.getString("instructions.task.singleChoice"),
                "instructions.correctAnswerTitle",labels.getString("instructions.correctAnswerTitle"),
                "instructions.correctAnswerTask",labels.getString("instructions.correctAnswerTask"),
                "instructions.incorrectAnswerTitle",labels.getString("instructions.incorrectAnswerTitle"),
                "instructions.incorrectAnswerTask",labels.getString("instructions.incorrectAnswerTask"),
                "judge",labels.getString("button.judge"),
                "skip",labels.getString("button.skip"),
                "reset",labels.getString("button.reset"),
                "next",labels.getString("button.next")//
                );

        out.putNextEntry(new ZipEntry(getURLid(q) + "_" + q.getDescriptiveURL() + "_sco.html"));
        out.write(html.getBytes("UTF8"));

        //System.out.println(html);

        out.closeEntry();
    }

    private void exportNoneQuestion(Question q, ZipOut out) throws IOException {
        // Get HTML Prototype
        Reader in = null;
        String htmlPrototype;
        try {
            in = new InputStreamReader(getClass().getResourceAsStream("prototype_scorm_None.html"), "UTF8");
            htmlPrototype = Streams.toString(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }


        // Formatting parameters of the prototype
        String html = TemplateEngine.process(htmlPrototype,//
                "chapter", getChapterTitle(q),
                "title", getPageTitle(q),
                "middot",getPageTitle(q).length() > 0 ? "&middot;" : "",
                "stylesheet",stylesheet,
                "question",encodeHTMLText((String) q.getBody().get(0)),
                "footer",(q.getId() != null) ? q.getId() : "",//
                //
                "instructions.title",labels.getString("instructions.title"),
                "instructions.task",labels.getString("instructions.task.singleChoice"),
                "instructions.correctAnswerTitle",labels.getString("instructions.correctAnswerTitle"),
                "instructions.correctAnswerTask",labels.getString("instructions.correctAnswerTask"),
                "instructions.incorrectAnswerTitle",labels.getString("instructions.incorrectAnswerTitle"),
                "instructions.incorrectAnswerTask",labels.getString("instructions.incorrectAnswerTask"),
                "judge",labels.getString("button.judge"),
                "skip",labels.getString("button.skip"),
                "reset",labels.getString("button.reset"),
                "next",labels.getString("button.next")//
                );

        out.putNextEntry(new ZipEntry(getURLid(q) + "_" + q.getDescriptiveURL() + "_sco.html"));
        out.write(html.getBytes("UTF8"));

        //System.out.println(html);

        out.closeEntry();
    }

    private void exportResources(ZipOut out) throws IOException {
        ZipIn zin = null;
        try {
            zin = getSCORMTemplates();
            ZipFiles.rezip(zin, out, null);
        } finally {
            if (zin != null) {
                zin.close();
            }
        }
    }

    private void exportExternalResources(ZipOut out) throws IOException {
        // Remove external resources that conflict with the templates
        ZipIn zin = null;
        try {
            zin = getSCORMTemplates();
            for (ZipEntry entry = zin.getNextEntry(); entry != null; entry = zin.getNextEntry()) {
                if (externalResourceRefs.containsKey(entry.getName())) {
                    externalResourceRefs.remove(entry.getName());
                }
            }
        } finally {
            if (zin != null) {
                zin.close();
            }
        }

        // Sort external resource refs by content packages
        HashMap<File, HashSet<String>> refsByContentPackage = new HashMap<File, HashSet<String>>();
        for (Map.Entry<String, File> entry : externalResourceRefs.entrySet()) {
            HashSet<String> hrefs;
            if (refsByContentPackage.containsKey(entry.getValue())) {
                hrefs = refsByContentPackage.get(entry.getValue());
            } else {
                hrefs = new HashSet<String>();
                refsByContentPackage.put(entry.getValue(), hrefs);
            }
            hrefs.add(entry.getKey());
        }

        // Export external resources
        for (Map.Entry<File, HashSet<String>> entry : refsByContentPackage.entrySet()) {
            if (entry.getKey().isDirectory()) {
                for (String href : entry.getValue()) {
                    ZipFiles.zip(new File(entry.getKey(), href), out, href);
                }
            } else {
                try {
                    zin = new ZipInStream(new FileInputStream(entry.getKey()));
                    ZipFiles.rezip(zin, out, null, new DefaultZipEntryFilter(entry.getValue(), true));
                } finally {
                    if (zin != null) {
                        zin.close();
                    }
                }
            }
        }
    }

    private String getXMLid(Object o) {
        return xmlPrefix + getOid(o);
    }

    private String getURLid(Object o) {
        return prefix + getOid(o);
    }

    private String getOid(Object o) {
        if (oidMap.containsKey(o)) {
            return oidMap.get(o);
        } else {
            String oid = Integer.toString(oidMap.size() + 1);
            oid = ("0000000000" + oid).substring(10 + oid.length() - Math.max(oid.length(), oidLen));
            oidMap.put(o, oid);
            return oid;
        }
    }

    /**
     * Returns the question for use with SCORM.
     * Returns null, if there is no suitable question type for SCORM.
     */
    public AnswerListType getSCORMType(Question q) {
        LinkedList<Object> body = q.getBody();

        /*
        // The body must consist of at least two elements
        if (body.size() < 2) {
        return AnswerListType.NONE;
        }*/

        // If the body contains one text element, or a text and a answerList
        // element, we can create the corresponding question type for
        // SCORM in most cases.
        if (body.size() == 1 && (body.getFirst() instanceof String)) {
            return AnswerListType.NONE;
        } else if (body.size() == 2
                && (body.getFirst() instanceof String)
                && (body.getLast() instanceof AnswerList)) {
            AnswerList al = (AnswerList) body.getLast();
            switch (al.getType()) {
                case BOOL:
                    return AnswerListType.BOOL;
                case NUMERIC:
                    return AnswerListType.CLOZE;
                case CLOZE:
                    return AnswerListType.CLOZE;
                case MATCHING_PAIR:
                    return AnswerListType.MATCHING_PAIR;
                case MULTIPLE_CHOICE:
                    return AnswerListType.MULTIPLE_CHOICE;
                case SINGLE_CHOICE:
                    return AnswerListType.SINGLE_CHOICE;
                case EXTERNAL:
                    return AnswerListType.EXTERNAL;
                default:
                    return null;
            }
        }

        // If the answer list is not at the end of the body, or if the
        // body contains multiple answer lists, in most cases, we can
        // create the CLOZE type.
        AnswerListType type = null;
        for (Object o : body) {
            if (o instanceof AnswerList) {
                AnswerList al = (AnswerList) o;
                switch (al.getType()) {
                    case EXTERNAL:
                        return AnswerListType.EXTERNAL;
                    case BOOL:
                        if (type == null || type == AnswerListType.CLOZE) {
                            type = AnswerListType.CLOZE;
                        } else {
                            return null;
                        }
                        break;
                    case NUMERIC:
                    case CLOZE:
                        if (type == null || type == AnswerListType.CLOZE) {
                            type = AnswerListType.CLOZE;
                        } else {
                            return null;
                        }
                        break;
                    case MATCHING_PAIR:
                        if (type == null) {
                            type = AnswerListType.MATCHING_PAIR;
                        } else {
                            return null;
                        }
                        break;
                    case MULTIPLE_CHOICE:
                        if (type == null || type == AnswerListType.CLOZE) {
                            type = AnswerListType.CLOZE;
                        } else {
                            return null;
                        }
                        break;
                    case SINGLE_CHOICE:
                        if (type == null || type == AnswerListType.CLOZE) {
                            type = AnswerListType.CLOZE;
                        } else {
                            return null;
                        }
                        break;
                    default:
                        return null;
                }
            }
        }
        return type;
    }

    private String encodeJavaScriptStringLiteral(String str) {
        str = Strings.replace(str, "\\", "\\\\\\\\\\\\\\\\");
        str = Strings.replace(str, "\n", "<br>");
        str = Strings.replace(str, "'", "\\\\\\\\\\\\\\\'");

        return str;
    }

    private String encodeHTMLText(String str) {
        str = Strings.replace(str, "&", "&amp;");
        str = Strings.replace(str, "\"", "&quot;");
        //str = Strings.replace(str,"\'","&apos;");
        str = Strings.replace(str, "<", "&lt;");
        str = Strings.replace(str, ">", "&gt;");
        str = Strings.replace(str, "\n", "<br>");

        return str;
    }

    private static String decryptClozeText(String str) {
        String[] parts = str.split("-", 4);
        if (parts.length != 4 || !parts[2].equals("###")) {
            throw new IllegalArgumentException("Can't decrypt " + str);
        }

        int stride = 1 + Integer.valueOf(parts[0])
                - Integer.valueOf(new StringBuilder(parts[1]).reverse().toString());

        StringBuilder buf = new StringBuilder();
        for (int i = 0, n = parts[3].length(); i < n; i += stride) {
            buf.append(parts[3].charAt(i));
        }
        return buf.toString();
    }

    private static String encryptClozeText(String str) {
        StringBuilder buf = new StringBuilder();

        int stride = random.nextInt(5) + 1;
        int keyOffset = random.nextInt(89) + stride + 10;

        buf.append(Integer.toString(keyOffset + stride));
        buf.append('-');
        buf.append(new StringBuilder(Integer.toString(keyOffset)).reverse());
        buf.append("-###-");

        for (int i = 0, n = str.length(); i < n; i++) {
            buf.append(str.charAt(i));
            for (int j = 0; j < stride; j++) {
                switch (random.nextInt(3)) {
                    case 0:
                        buf.append((char) (random.nextInt(26) + 'a'));
                        break;
                    case 1:
                        buf.append((char) (random.nextInt(26) + 'A'));
                        break;
                    case 2:
                        buf.append((char) (random.nextInt(10) + '0'));
                        break;
                }
            }
        }

        return buf.toString();
    }

    /**
     * @param cff Must have a client property named "stylesheet".
     * @param baseDir is used to locate files referenced by the GIFT file.
     */
    @Override
    public void export(List<Question> questions, File file, ConfigurableFileFilter cff, File baseDir) throws IOException {
        this.baseDir = baseDir;
        if (isPIFdefault) {
            exportToPIF(questions, file,
                    (String) cff.getClientProperty("title"),
                    (String) cff.getClientProperty("stylesheet"),
                    (Locale) cff.getClientProperty("locale"),
                    (String) cff.getClientProperty("prefix"));
        } else {
            exportToContentPackage(questions, file,
                    (String) cff.getClientProperty("title"),
                    (String) cff.getClientProperty("stylesheet"),
                    (Locale) cff.getClientProperty("locale"),
                    (String) cff.getClientProperty("prefix"));
        }
    }

    private String getChapterTitle(Question q) {
        String title = q.getTitle();
        if (title != null) {
            String[] titleParts = title.split(";");
            if (titleParts.length > 1) {
                title = titleParts[titleParts.length - 2].trim();
            }
        } else {
            title = q.getDescriptiveTitle();
        }
        return (title == null) ? "" : title;
    }

    private String getPageTitle(Question q) {
        String title = q.getTitle();
        if (title != null) {
            String[] titleParts = title.split(";");
            if (titleParts.length > 1) {
                title = titleParts[titleParts.length - 1].trim();
            } else {
                title = null;
            }
        }
        // FIXME - We should return null here, and we then should omit the page
        // title from the HTML output that we generate.
        return (title == null) ? "" : title;
    }
}
