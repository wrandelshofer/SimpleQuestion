/*
 * @(#)FileElement.java 1.2  2006-10-10
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
 * Represents a SCORM 1.2 CAM 'file' element.
 * <p>
 * This element identifies one or more local files that this resource is
 * dependent on.
 * <p>
 * A 'file' element has a structure as shown below.
 * Square brackets [ ] denote zero or one occurences.
 * Braces { } denote zero or more occurences.
 * <pre>
 * &lt;file href="URL"&gt;
 * {&lt;file/&gt;}
 * &lt;/file&gt;
 * </pre>
 *
 * Reference:
 * ADL (2001c). Advanced Distributed Learning.
 * Sharable Content Object Reference Model (SCORM(TM)) Version 1.2.
 * The SCORM Content Aggregation Model. October 1, 2001.
 * Internet (2003-01-20): http://www.adlnet.org
 *
 * @author Werner Randelshofer, Staldenmattweg 2, Immensee, CH-6405, Switzerland
 * @version 1.2 2006-10-10 Parse with XML namespaces. 
 * <br>1.1.1 2004-04-26 Made method getInfo() more verbose.
 * <br>1.1 2003-11-03 Method referencesFile added. HTML output in method
 * toString changed. Remove escapes from unconsolidated HRef.
 * <br>1.0 2003-10-01 Method validate must take base attribute of
 *                    ResourceElement into account.
 * <br>0.26 2003-05-19 Method validate() added.
 * <br>0.1 2003-02-02 Created.
 */
public class FileElement extends AbstractElement {
    /**
     * This attribute is set by validate().
     */
    private boolean isHRefValid = true;
    
    /**
     * href (required). URL of the file. This implies that the file is
     * locally stored within the package.
     */
    private String href;
    
    private LinkedList fileList = new LinkedList();
    
    /** Creates a new instance. */
    public FileElement() {
    }
    
    /**
     * Parses the specified DOM Element and incorporates its contents into this element.
     * @param elem An XML element with the tag name 'file'.
     */
    public void parse(Element elem)
    throws IOException, ParserConfigurationException, SAXException {
        if (! DOMs.isElement(elem, CAM.IMSCP_NS, "file")) {
            throw new IOException("'imscp:file' element expected, but found '"+elem.getTagName()+"' element.");
        }
        
        // Read the attributes
        this.href = DOMs.getAttributeNS(elem, CAM.IMSCP_NS, "href", null);
        
        // Read the child elements
        NodeList nodes = elem.getChildNodes();
        for (int i=0; i < nodes.getLength(); i++) {
            if (nodes.item(i) instanceof Element) {
                Element child = (Element) nodes.item(i);
                if (DOMs.isElement(child, CAM.IMSCP_NS, "file")) {
                    FileElement file = new FileElement();
                    this.fileList.add(file);
                    add(file);
                    file.parse(child);
                }
            }
        }
    }
    
    /**
     * Gets a href relative to the resource element or to relative to
     * the content package or an absolute href.
     */
    public String getHRef() {
        return href;
    }
    
    /**
     * Gets a href relative to
     * the content package or an absolute href.
     */
    public String getConsolidatedHRef() {
        String consolidatedHRef = href;
        if (getParent() != null && href != null) {
            String base = ((ResourceElement) getParent()).getBase();
            if (base != null) {
                consolidatedHRef = (base.charAt(base.length() - 1) == '/')
                ? base + href
                : base + '/' + href;
            }
        }
        
        return Strings.unescapeURL(consolidatedHRef);
    }
    
    /**
     * Dumps the contents of this subtree into the provided string buffer.
     */
    public void dump(StringBuffer buf, int depth) {
        for (int i=0; i < depth; i++) buf.append('.');
        buf.append("<file href=\""+href+"\">\n");
        Iterator iter = fileList.iterator();
        while (iter.hasNext()) {
            ((AbstractElement) iter.next()).dump(buf, depth+1);
        }
        for (int i=0; i < depth; i++) buf.append('.');
        buf.append("</file>\n");
    }
    
    /**
     * Validates this CAM element.
     *
     * @return Returns true if this elements is valid.
     */
    public boolean validate() {
        isValid = super.validate();
        
        if (href != null) {
            Set fileNames = getIMSManifestDocument().getFileNames();
            isHRefValid = fileNames.contains(getConsolidatedHRef());
        } else {
            isHRefValid = true;
        }
        return isValid = isValid && isHRefValid;
    }
    public String getInfo() {
        return (isHRefValid) 
        ? super.getInfo()
        : labels.getString("cam.error")+": "+
                labels.getFormatted("cam.fileIsMissing",href)
        ;
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("<html><font size=-1 face=SansSerif>");
        if (! isValid()) buf.append("<font color=red>* </font>");
        buf.append("<b>File</b> href:");
        if (! isHRefValid) buf.append("<font color=red>");
        buf.append(href);
        if (! isHRefValid) buf.append(" <b>"+labels.getString("cam.noFile")+"</b></font>");
        
        buf.append("</font>");
        return buf.toString();
    }
    
    /**
     * Adds all file names referenced by this FileElement and subtree elements.
     */
    public void addSubtreeFileNames(HashSet fileNames) {
        Enumeration enm = preorderEnumeration();
        while (enm.hasMoreElements()) {
            AbstractElement element = (AbstractElement) enm.nextElement();
            if (element instanceof FileElement) {
                FileElement fileElement = (FileElement) element;
                String href = fileElement.getConsolidatedHRef();
                if (href != null && isHRefValid) fileNames.add(href);
            }
        }
    }

    /**
     * Removes all file names in the set, which are referenced by this
     * CAM Element.
     */
    public void consumeFileNames(Set fileNames) {
        fileNames.remove(getConsolidatedHRef());
    }
}
