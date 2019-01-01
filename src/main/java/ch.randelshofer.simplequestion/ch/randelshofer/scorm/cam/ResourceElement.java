/*
 * @(#)ResourceElement.java 1.2  2006-10-10
 *
 * Copyright (c) 2003 Werner Randelshofer
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

import ch.randelshofer.scorm.AbstractElement;
import ch.randelshofer.util.*;
import ch.randelshofer.xml.DOMs;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.w3c.dom.*;
/**
 * Represents a SCORM CAM 'resource' element.
 * <p>
 * Describes a specific content file.
 * <p>
 * A 'resource' element has a structure as shown below.
 * Square brackets [ ] denote zero or one occurences.
 * Braces { } denote zero or more occurences.
 * <pre>
 * &lt;resource identifier="ID" type="string" adlcp:scormtype="string" [xml:base="string"] [href="URL]&gt;
 * [&lt;metadata&gt;]
 * {&lt;file&gt;}
 * {&lt;dependency&gt;}
 * &lt;/resource&gt;
 * </pre>
 *
 * This implementation ignores the 'metadata' element.
 *
 * Reference:
 * ADL (2001c). Advanced Distributed Learning.
 * Sharable Content Object Reference Model (SCORM(TM)) Version 1.2.
 * The SCORM Content Aggregation Model. October 1, 2001.
 * Internet (2003-01-20): http://www.adlnet.org
 *
 * @author Werner Randelshofer, Staldenmattweg 2, Immensee, CH-6405, Switzerland
 * @version 1.2 2006-10-10 Issue a warning when encountering an XML error, but
 * continue parsing the XML file. Parse with XML namespaces.
 * <br>1.1.5 2003-11-05 Do not export to JavaScript when we have no href.
 * <br>1.1 2003-11-03 Method getConsolidatedHRef added.
 * <br>1.0.1 2003-10-30 HTML output in method toString changed.
 * <br>1.0 2003-10-01 Attribute href is optional.
 * <br>0.27 2003-08-22 Do not ignore the 'dependency' element anymore.
 * <br>0.26 2003-05-19 Method validate() added.
 * <br>0.17 2003-03-16 Naming conventions streamlined with JavaScript code
 * of the LMS.
 * <br>0.12 2003-03-05 Revised
 * <br>0.1 2003-02-02 Created.
 */
public class ResourceElement extends AbstractElement {
    /**
     * This attribute is set by validate().
     */
    private boolean isReferenced = true;
    /**
     * This attribute is set by validate().
     */
    private boolean isHRefValid = true;
    /**
     * identifier (required). An identifier that is unique within the Manifest.
     * Data type = ID.
     */
    private String identifier;
    
    public final static int TYPE_WEBCONTENT = 0;
    /**
     * type (required). A string that identifies the type of resource. This
     * specification defines only type "webcontent".
     */
    private int type = TYPE_WEBCONTENT;
    
    private final static int SCORMTYPE_ASSET = 0;
    private final static int SCORMTYPE_SCO = 1;
    
    /**
     * adlcp:scormtype (required). Defines the type of the SCORM resource.
     * This is an ADL extension to the IMS Content Packaging Information Model.
     * Data Type: Restricted vocabulary of either "sco" or "asset".
     */
    private int adlcpScormtype = SCORMTYPE_ASSET;
    
    /**
     * href (optional). A reference to the "entry point" of this resource.
     * External fully-qualified URIs are also permitted.
     */
    private String href;
    
    /**
     * xml:base (optional). This provides a relative path offset for the
     * content file(s). The usage of this element is defined in the XML Base
     * Working Draft form the W3C.
     */
    private String xmlBase;
    
    /**
     * The list of 'file' element children.
     */
    private LinkedList fileList = new LinkedList();
    /**
     * The list of 'dependecy' element children.
     */
    private LinkedList dependencyList = new LinkedList();
    /**
     * Contains context specific meta-data that is used to describe the resource.
     */
    private MetadataElement metadataElement;
    /**
     * Holds warnings that were generated while parsing the imsmanifest.xml file.
     */
    private LinkedList xmlWarnings = new LinkedList();
    
    
    /** Creates a new instance of ResourceElement */
    public ResourceElement() {
    }
    
    /**
     * Parses the specified DOM Element and incorporates its contents into this element.
     * @param elem An XML element with the tag name 'resource'.
     */
    public void parse(Element elem)
    throws IOException, ParserConfigurationException, SAXException {
        String attr;
        
        if (! DOMs.isElement(elem, CAM.IMSCP_NS, "resource")) {
            throw new IOException("'imscp:resource' element expected, but found '"+elem.getTagName()+"' element.");
        }
        
        // Read the attributes
        this.identifier = DOMs.getAttributeNS(elem, CAM.IMSCP_NS, "identifier", null);
        attr = DOMs.getAttributeNS(elem, CAM.IMSCP_NS, "type", null);
        if (attr == null || ! attr.equals("webcontent")) {
            // XXX - Localize this string
            xmlWarnings.add("Warning: Attribute 'type' must have the value 'webcontent', but found '"+attr+"'. Assuming value 'webcontent'.");
        }
        this.type = TYPE_WEBCONTENT;
        
        attr = DOMs.getAttributeNS(elem, CAM.ADLCP_NS, "scormtype", "");
        if (attr.equals("") || ! (attr.equals("asset") || attr.equals("sco"))) {
            // XXX - Localize this string
            xmlWarnings.add("Warning: Attribute 'adlcp:scormtype' must have the value 'asset' or 'sco', but found '"+attr+"'. Assuming value 'sco'.");
        }
        this.adlcpScormtype = (attr.equals("asset")) ? SCORMTYPE_ASSET : SCORMTYPE_SCO;
        
        this.href = DOMs.getAttributeNS(elem, CAM.ADLCP_NS, "href", null);
        
        this.xmlBase = DOMs.getAttributeNS(elem, "xml", "base", null);
        
        // Read the child elements
        NodeList nodes = elem.getChildNodes();
        for (int i=0; i < nodes.getLength(); i++) {
            if (nodes.item(i) instanceof Element) {
                Element child = (Element) nodes.item(i);
                if (DOMs.isElement(child, CAM.IMSCP_NS, "metadata")) {
                    if (this.metadataElement != null) {
                        xmlWarnings.add("Warning: The 'imscp:metadata' element may only be specified once whithin a 'resource' element. " +
                                "Ignoring the extreanous 'metadata' element.");
                    } else {
                        this.metadataElement = new MetadataElement();
                        add(metadataElement);
                        this.metadataElement.parse(child);
                    }
                } else if (DOMs.isElement(child, CAM.IMSCP_NS, "file")) {
                    FileElement file = new FileElement();
                    add(file);
                    this.fileList.add(file);
                    file.parse(child);
                } else if (DOMs.isElement(child, CAM.IMSCP_NS, "dependency")) {
                    DependencyElement dependency = new DependencyElement();
                    add(dependency);
                    this.dependencyList.add(dependency);
                    dependency.parse(child);
                }
            }
        }
    }
    
    /**
     * Dumps the contents of this subtree into the provided string buffer.
     */
    public void dump(StringBuffer buf, int depth) {
        for (int i=0; i < depth; i++) buf.append('.');
        buf.append("<resource identifier=\""+identifier+"\" href=\""+href+"\" xml:base=\""+xmlBase+"\" type=\""+type+"\" =\""+adlcpScormtype+"\">\n");
        Iterator iter = fileList.iterator();
        while (iter.hasNext()) {
            ((AbstractElement) iter.next()).dump(buf, depth+1);
        }
        iter = dependencyList.iterator();
        while (iter.hasNext()) {
            ((AbstractElement) iter.next()).dump(buf, depth+1);
        }
        for (int i=0; i < depth; i++) buf.append('.');
        buf.append("</resource>\n");
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
        out.print("new ResourceElement(\""+gen.getIdentifier(getIdentifier())+"\",");
        out.print("\""+getConsolidatedHRef()+"\"");
        out.print((depth == 0) ? ");" : ")");
    }
    /**
     * Returns the identifier that uniquely identifies this resource within the CAM.
     */
    public String getIdentifier() {
        return identifier;
    }
    /**
     * Returns the URL of this resource.
     */
    public String getHRef() {
        return href;
    }
    /**
     * Gets a href relative to
     * the content package or an absolute href.
     */
    /**
     * Gets a href relative to
     * the content package or an absolute href.
     */
    public String getConsolidatedHRef() {
        String consolidatedHRef = href;
        String base = getBase();
        if (base != null && base.length() > 0) {
            consolidatedHRef = (base.charAt(base.length() - 1) == '/')
            ? base + href
                    : base + '/' + href;
        }
        
        return Strings.unescapeURL(consolidatedHRef);
    }
    /**
     * Returns the Base of the files in this resource.
     * Returns null if no base is specified.
     */
    public String getBase() {
        return xmlBase;
    }
    /**
     * Validates this CAM element.
     *
     * @return Returns true if this elements is valid.
     */
    public boolean validate() {
        isValid = super.validate();
        if (getIdentifier() == null) isValid = false;
        if (identifier != null) {
            isReferenced = false;
            Enumeration enm = getIMSManifestDocument().getOrganizationsElement().preorderEnumeration();
            while (enm.hasMoreElements()) {
                AbstractElement node = (AbstractElement) enm.nextElement();
                if (node instanceof ItemElement) {
                    ItemElement item = (ItemElement) node;
                    if (item.getIdentifierref() != null
                            && item.getIdentifierref().equals(identifier)) {
                        isReferenced = true;
                        break;
                    }
                }
            }
            enm = getIMSManifestDocument().getResourcesElement().preorderEnumeration();
            while (enm.hasMoreElements()) {
                AbstractElement node = (AbstractElement) enm.nextElement();
                if (node instanceof DependencyElement) {
                    DependencyElement dependency = (DependencyElement) node;
                    if (dependency.getIdentifierref() != null
                            && dependency.getIdentifierref().equals(identifier)) {
                        isReferenced = true;
                        break;
                    }
                }
            }
            
            // Not being referenced is not considered invalid.
            //if (! isReferenced) isValid = false;
        }
        if (href != null) {
            Set fileNames = getIMSManifestDocument().getFileNames();
            isHRefValid = fileNames.contains(getConsolidatedHRef());
        } else {
            isHRefValid = true;
        }
        return isValid = isValid && isHRefValid && isReferenced && ! hasWarnings();
    }
    /**
     * The return value of this method is unspecified until
     * validate() has been done.
     *
     * @return Returns true if the identifier of this resource is referenced
     * by an ItemElement.
     * XXX should return true also if this resource is referenced by another
     * resource.
     */
    public boolean isReferenced() {
        return isReferenced;
    }
    
    public boolean hasWarnings() {
        return xmlWarnings.size() > 0;
    }
    /**
     * Returns a String describing the contents of this resource.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("<html><font size=-1 face=SansSerif>");
        if (! isValid()) buf.append("<font color=red>* </font>");
        buf.append("<b>Resource</b> id:");
        if (! isReferenced()) buf.append("<font color=blue>");
        if (! isIdentifierValid()) buf.append("<font color=red>");
        buf.append(identifier);
        if (! isIdentifierValid()) buf.append(" <b>"+labels.getString("cam.duplicateID")+"</b></font>");
        if (! isReferenced()) buf.append(" <b>"+labels.getString("cam.notReferenced")+"</b></font>");
        /*
        buf.append(" <b>scormtype:</b>");
        buf.append(((adlcpScormtype == SCORMTYPE_ASSET) ? "asset" : "sco"));
         */
        if (href != null) {
            buf.append(" href:");
            if (! isHRefValid) buf.append("<font color=red>");
            buf.append(href);
            if (! isHRefValid) buf.append(" <b>"+labels.getString("cam.noFile")+"</b></font>");
        }
        if (hasWarnings()) {
            buf.append("  <font color=blue><b>"+labels.getString("cam.illegalAttributes")+"</b></font>");
        }
        
        if (xmlBase != null) {
            buf.append(" base:");
            buf.append(xmlBase);
        }
        
        buf.append("</font>");
        return buf.toString();
    }
    
    /**
     * Adds all file names referenced by this resource to the supplied fileNames
     * set. Adds this resource to the exclusion list, and then calls
     * addAllReferencedFileNames on resources on which this ResourceElements
     * depends on.
     * <p>
     * Does nothing if this resource is in the exclusion list.
     */
    public void addReferencedFileNamesTo(HashSet fileNames, HashSet exclusionList) {
        if (! exclusionList.contains(this)) {
            exclusionList.add(this);
            Iterator iter = fileList.iterator();
            while (iter.hasNext()) {
                FileElement fileElement = (FileElement) iter.next();
                fileElement.addSubtreeFileNames(fileNames);
            }
            iter = dependencyList.iterator();
            while (iter.hasNext()) {
                DependencyElement dependencyElement = (DependencyElement) iter.next();
                dependencyElement.getResource().addReferencedFileNamesTo(fileNames, exclusionList);
            }
        }
    }
    public String getInfo() {
        StringBuffer buf = new StringBuffer();
        for (Iterator i = xmlWarnings.iterator(); i.hasNext(); ) {
            if (buf.length() > 0) buf.append('\n');
            buf.append(i.next());
        }
        return (buf.length() == 0)
        ? super.getInfo()
        : buf.toString()
        ;
    }
}
