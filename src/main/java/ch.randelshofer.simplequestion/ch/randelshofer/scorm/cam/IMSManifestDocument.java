/*
 * @(#)IMSManifestDocument.java  1.3  2006-10-10
 *
 * Copyright (c) 2003-2006 Werner Randelshofer
 * Staldenmattweg 2, Immensee, CH-6405, Switzerland.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Werner Randelshofer. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Werner Randelshofer.
 */
package ch.randelshofer.scorm.cam;

import ch.randelshofer.util.*;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.w3c.dom.*;

/**
 * Represents a top-level SCORM 1.2 CAM 'manifest' element.
 * <p>
 * The 'manifest' element is the top-level element of a content package.
 * <p>
 * A 'manifest' element has a structure as shown below.
 * Square brackets [ ] denote zero or one occurences.
 * Braces { } denote zero or more occurences.
 * Text in <b>bold</b> denotes non-terminal symbols.
 * <pre>
 * <b>manifest</b> ::= &lt;manifest identifier=<b>ID</b> 
 *                       [version=<b>string</b>] 
 *                       [xml:base=<b>string</b>]&gt;
 *              [<b>metadata</b>]
 *              <b>organizations</b>
 *              <b>resources</b>
 *              {<b>manifest</b>}
 *              &lt;/manifest&gt;
 * </pre>
 * Reference:
 * ADL (2001). Advanced Distributed Learning.
 * Sharable Content Object Reference Model (SCORM(TM)) Version 1.2.
 * The SCORM Content Aggregation Model. October 1, 2001.
 * Internet (2003-01-20): http://www.adlnet.org
 *
 * @author  Werner Randelshofer
 * @version 1.3 2006-10-10 Parse with XML namespaces.
 * <br>1.2 2006-05-26 Skip hidden files. 
 * <br>1.1.6  2004-01-19  Comments updated. 
 * <br> 1.1.5 2003-11-05 Method exportToJavaScript assigns the CAM to
 * the existing variable LMS.cam instead of creating a new variable named LMSCAM.
 * <br>1.0 2003-10-29 Variable fileNames uses a Set to store the file names
 * instead of a Map. Also changed all corresponding method signatures from Map
 * to Set. 
 * <br>0.20 2003-05-19 Method getContentDirectory added.
 * <br>0.19 2003-03-26 exportToJavaScript revised.
 * <br>0.17 2003-03-18 Naming conventions streamlined with JavaScript code
 * of the LMS. Method getManifestURL added.
 * <br>0.1 2003-02-02 Created.
 */
public class IMSManifestDocument extends ManifestElement {
    private File pifFile;
    private File contentPackageFile;
    /**
     * Stores all filenames in the PIF or in
     * the Content Package as String objects.
     * Each String object represents a path relative to the pifFile or
     * the contentPackageFile.
     * The separator used to delimit path elements is the '/' (slash)
     * character.
     */
    private HashSet fileNames;
    
    /** Creates a new instance of CAMModel */
    public IMSManifestDocument() {
    }
    
    public void setPIFFile(File pif) {
        pifFile = pif;
        contentPackageFile = null;
    }
    public File getPIFFile() {
        return pifFile;
    }
    public void setContentPackage(File dir) {
        contentPackageFile = dir;
        pifFile = null;
    }
    public File getContentPackage() {
        return contentPackageFile;
    }
    public URL getManifestURL()  {
        URL url;
        try {
            if (pifFile != null) {
                url = new URL("jar:"+pifFile.toURL()+"!imsmanifest.xml");
            } else {
                url = new URL(contentPackageFile.toURL(), "imsmanifest.xml");
            }
        } catch (MalformedURLException e) {
            InternalError error = new InternalError("Unable to create URL:");
            error.initCause(e);
            throw error;
        }
        return url;
    }
    
    /**
     * Adds the contents of the XML stream to this model.
     * For peak performance, the input stream should be buffered.
     */
    public void readXML(InputStream in)
    throws IOException, SAXException, ParserConfigurationException {
        // The DOM Document.
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Document doc = factory.newDocumentBuilder().parse(in);
        
        // Read the "manifest" element
        Element elem = doc.getDocumentElement();
        parse(elem);
        
    }
    
    /**
     * Exports this CAM subtree to JavaScript using the specified PrintWriter.
     *
     * @param out The output stream.
     * @param depth The current depth of the tree (used for indention).
     * @param gen The identifier generator used to generate short(er) identifiers
     *  whithin the JavaScript.
     */
    public void exportToJavaScript(PrintWriter out, int depth, IdentifierGenerator gen)
    throws IOException {
        indent(out, depth);
        if (depth == 0) out.print("API.cam = ");
        out.println("new ManifestElement(\""+gen.getIdentifier(getIdentifier(), getIdentifier())+"\",\""+getVersion()+"\",");
        getOrganizationsElement().exportToJavaScript(out, depth + 1, gen);
        out.println();
        indent(out, depth + 1);
        out.println(",");
        getResourcesElement().exportToJavaScript(out, depth + 1, gen);
        out.println();
        indent(out, depth);
        if (depth == 0) out.println(");");
        else out.print(")");
    }
    
    /*
    public void compressIdentifiers() {
        int count = 0;
        HashMap map = new HashMap();
        Enumeration enum = preorderEnumeration();
        while (enum.hasMoreElements()) {
            CAMElement element = (CAMElement) enum.nextElement();
            if (element.getIdentifier() != null) {
                map.put(element.getIdentifier(), Integer.toString(count++, Character.MAX_RADIX));
            }
        }
        enum = preorderEnumeration();
        while (enum.hasMoreElements()) {
            CAMElement element = (CAMElement) enum.nextElement();
            element.updateIdentifiers(map);
        }
    }*/
    
    /**
     * This method returns all filenames in the PIF or in
     * the Content Package as String objects.
     * Each String object represents a path relative to the pifFile or
     * the contentPackageFile.
     * The separator used to delimit path elements is the '/' (slash)
     * character.
     * @return Returns an unmodifiable Set.
     */
    public Set getFileNames() {
        if (fileNames == null) {
            fileNames = new HashSet();
            if (pifFile != null) {
                    ZipInputStream zipin = null;
                try {
                    zipin = new ZipInputStream(
                    new BufferedInputStream(
                    new FileInputStream(pifFile)
                    ));
                    ZipEntry entry = null;
                    while ((entry = zipin.getNextEntry()) != null) {
                        if (! entry.isDirectory()) {
                            fileNames.add(entry.getName());
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (zipin != null) 
                        try { zipin.close(); } catch (IOException e2) {}
                }
            } else if (contentPackageFile != null) {
                addDirectorySubtree(contentPackageFile, contentPackageFile, fileNames);
            }
        }
        return Collections.unmodifiableSet(fileNames);
    }
    private void addDirectorySubtree(File root, File subdir, Set fileNames) {
        String rootName = root.toString();
        File[] dir = subdir.listFiles();
        if (dir != null) {
        for (int i=0; i < dir.length; i++) {
            if (! dir[i].isHidden()) {
            if (dir[i].isDirectory()) {
                addDirectorySubtree(root, dir[i], fileNames);
            } else {
                String relativePath = dir[i].toString().replace(File.separatorChar, '/').substring(rootName.length() + 1);
                fileNames.add(relativePath);
            }
            }
        }
        }
    }
}
