/*
 * @(#)ItemElement.java 1.2  2006-10-10
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

import javax.swing.tree.TreeNode;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.w3c.dom.*;
/**
 * Represents a SCORM CAM 'item' element.
 * <p>
 * This element describes a node within the organization structure.
 * <p>
 * An 'item' element has a structure as shown below.
 * Square brackets [ ] denote zero or one occurences.
 * Braces { } denote zero or more occurences.
 * <pre>
 * &lt;item identifier="ID" [identifierref="string"] [isvisible="boolean"] [parameters="string"]&gt;
 * [&lt;title&gt;]
 * {&lt;item&gt;}
 * [&lt;metadata&gt;]
 * [&lt;adlcp:prerequisites&gt;]
 * [&lt;adlcp:maxtimeallowed&gt;]
 * [&lt;adlcp:timelimitaction&gt;]
 * [&lt;adlcp:datafromlms&gt;]
 * [&lt;adlcp:masteryscore&gt;]
 * &lt;/item&gt;
 * </pre>
 *
 * This implementation ignores the 'metadata' element and all 'adlcp:' elements.
 *
 * Reference:
 * ADL (2001c). Advanced Distributed Learning.
 * Sharable Content Object Reference Model (SCORM(TM)) Version 1.2.
 * The SCORM Content Aggregation Model. October 1, 2001.
 * Internet (2003-01-20): http://www.adlnet.org
 *
 * @author Werner Randelshofer, Staldenmattweg 2, Immensee, CH-6405, Switzerland
 * @version 1.2 2006-10-10 Parse with XML namespaces.
 * <br>1.1.2 2006-06-01 Encode unicode characters with HTML entities.
 * <br>1.1.1 2004-01-07 Fixed a NullPointerException in method getTitle().
 * <br>1.1 2003-11-03 Support for metadata element added.
 * <br>1.0 2003-10-30 Method getResource() added. HTML output in method
 * toString changed.
 * <br>0.19.4 2003-04-02 Method validate() added.
 * <br>0.17 2003-03-17 Naming conventions streamlined with JavaScript code
 * of TinyLMS.
 * <br>0.12 2003-03-05 Revised.
 * <br>0.1 2003-02-02 Created.
 */
public class ItemElement extends AbstractElement{
    /**
     * This attribute is set by validate().
     */
    private boolean isIdentifierrefValid = true;
    /**
     * identifier (required). An identifier that is unique within the Manifest.
     * Data type = ID.
     */
    private String identifier;
    /**
     * identifierref (optional). A reference to a 'resource' identifier (within
     * the same package) or a (sub)Manifest that is used to resolve the ultimate
     * location of the file. If no identifierref is supplied, it is assumed
     * that there is no content associated with this entry in the organization.
     * This attribute is null if no identifierref was supplied in the XML document.
     */
    private String identifierref;
    /**
     * isvisible(optional). Indicates whether or not the title of the item
     * is displayed by the LMS navigation mechanism. If not present, value is
     * assumed to be "true".
     */
    private boolean isVisible = true;
    /**
     * parameters (optional). Static parameters to be passed to the content
     * file at launch time.
     */
    private String parameters;
    
    /**
     * Describes the title of the element.
     */
    private TitleElement titleElement;
    /**
     * Describes data from lms for the the element.
     */
    private DataFromLMSElement dataFromLMSElement;
    /**
     * Contains context specific meta-data that is used to describe the item.
     */
    private MetadataElement metadataElement;
    
    private LinkedList<ItemElement> itemList = new LinkedList<>();
    
    /** Creates a new instance. */
    public ItemElement() {
    }
    
    /**
     * Parses the specified DOM Element and incorporates its contents into this element.
     * @param elem An XML element with the tag name 'item'.
     */
    public void parse(Element elem)
    throws IOException, ParserConfigurationException, SAXException {
        String attr;
        
        if (! DOMs.isElement(elem, CAM.IMSCP_NS, "item")) {
            throw new IOException("'adlcp:item' element expected, but found '"+elem.getTagName()+"' element.");
        }
        this.identifier = DOMs.getAttributeNS(elem, CAM.IMSCP_NS, "identifier", null);
        this.identifierref = DOMs.getAttributeNS(elem, CAM.IMSCP_NS, "identifierref", null);
        this.isVisible = DOMs.getAttributeNS(elem, CAM.IMSCP_NS, "isvisible", "true").equals("true");
        this.parameters = DOMs.getAttributeNS(elem, CAM.IMSCP_NS, "parameters", null);
        
        // Read the child elements
        NodeList nodes = elem.getChildNodes();
        for (int i=0; i < nodes.getLength(); i++) {
            if (nodes.item(i) instanceof Element) {
                Element child = (Element) nodes.item(i);
                if (DOMs.isElement(child, CAM.IMSCP_NS, "title")) {
                    if (this.titleElement != null) throw new IOException("'title' element may only occur once whithin an 'item' element.");
                    this.titleElement = new TitleElement();
                    add(this.titleElement);
                    this.titleElement.parse(child);
                } else if (DOMs.isElement(child, CAM.IMSCP_NS, "datafromlms")) {
                    if (this.dataFromLMSElement != null) throw new IOException("'datafromlms' element may only occur once whithin an 'item' element.");
                    this.dataFromLMSElement = new DataFromLMSElement();
                    add(this.dataFromLMSElement);
                    this.dataFromLMSElement.parse(child);
                } else if (DOMs.isElement(child, CAM.IMSCP_NS, "item")) {
                    ItemElement item = new ItemElement();
                    add(item);
                    this.itemList.add(item);
                    item.parse(child);
                } else if (DOMs.isElement(child, CAM.IMSCP_NS, "metadata")) {
                    if (this.metadataElement != null) throw new IOException("'metadata' may only be specified once whithin a 'manifest'");
                    this.metadataElement = new MetadataElement();
                    add(metadataElement);
                    this.metadataElement.parse(child);
                }
            }
        }
    }
    
    /**
     * Dumps the contents of this subtree into the provided string buffer.
     */
    public void dump(StringBuffer buf, int depth) {
        for (int i=0; i < depth; i++) buf.append('.');
        buf.append("<item identifier=\""+identifier+"\" identifierref=\""+identifierref+"\" isvisible=\""+isVisible+"\" parameters=\""+parameters+"\">\n");
        titleElement.dump(buf, depth+1);
        Iterator<ItemElement> iter = itemList.iterator();
        while (iter.hasNext()) {
            ((AbstractElement) iter.next()).dump(buf, depth+1);
        }
        for (int i=0; i < depth; i++) buf.append('.');
        buf.append("</item>\n");
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
        out.print("new ItemElement(\""+
                gen.getIdentifier(getIdentifier())+"\",\""+
                Strings.escapeHTML(Strings.escapeUnicodeWithHTMLEntities(getTitle()))+
                "\"");
        if (getIdentifierref() == null) {
            out.print(",null");
        } else {
            out.print(",\""+gen.getIdentifier(getIdentifierref())+"\"");
        }
        if (getParameters() == null) {
            out.print(",null");
        } else {
            out.print(",\""+getParameters()+"\"");
        }
        if (getDataFromLMSElement() == null) {
            out.print(",null");
        } else {
            out.print(",\""+getDataFromLMSElement().getDataFromLMS()+"\"");
        }
        if (itemList.size() == 0) {
            out.print((depth == 0) ? ",[]);" : ",[])");
        } else {
            out.println(",[");
            
            Iterator<ItemElement> iter = itemList.iterator();
            while (iter.hasNext()) {
                ((ItemElement) iter.next()).exportToJavaScript(out, depth + 1, gen);
                if (iter.hasNext()){
                    out.println(",");
                }
            }
            out.println();
            indent(out, depth);
            out.print((depth == 0) ? "]);" : "])");
        }
    }
    
    public String getIdentifier() {
        return identifier;
    }
    public String getTitle() {
        return (titleElement == null) ? null : titleElement.getTitle();
    }
    public String getIdentifierref() {
        return identifierref;
    }
    public String getParameters() {
        return parameters;
    }
    
    public DataFromLMSElement getDataFromLMSElement() {
        return dataFromLMSElement;
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        
        buf.append("<html><font size=-1 face=SansSerif>");
        if (! isValid()) buf.append("<font size=-1 color=red face=SansSerif>* </font>");
        buf.append("<b>Item</b> id:");
        if (! isIdentifierValid()) buf.append("<font color=red>");
        buf.append(identifier);
        if (! isIdentifierValid()) buf.append(" <b>DUPLICATE ID</b></font>");
        if (identifierref != null) {
            buf.append(" idref:");
            if (! isIdentifierrefValid()) buf.append("<font color=red>");
            buf.append(identifierref);
            if (! isIdentifierrefValid()) buf.append(" <b>NO RESOURCE</b></font>");
        }
        if (! isVisible) buf.append(" visible:"+isVisible);
        if (parameters != null) {
            buf.append(" parameters:\"");
            buf.append(parameters);
            buf.append('\"');
        }
        buf.append("</font>");
        return buf.toString();
    }
    
    public List<ItemElement> getItemList() {
        return Collections.unmodifiableList(itemList);
    }
    
    /**
     * Validates this CAM element.
     *
     * @return Returns true if this elements is valid.
     */
    public boolean validate() {
        isValid = super.validate();
        if (getIdentifier() == null) isValid = false;
        if (identifierref != null) {
            isIdentifierrefValid = false;
            Enumeration<TreeNode> enm = getIMSManifestDocument().getResourcesElement().preorderEnumeration();
            while (enm.hasMoreElements()) {
                AbstractElement node = (AbstractElement) enm.nextElement();
                if (node.getIdentifier() != null && node.getIdentifier().equals(identifierref)) {
                    isIdentifierrefValid = true;
                    break;
                }
            }
            if (! isIdentifierrefValid) isValid = false;
        }
        return isValid;
    }
    
    /**
     * The return value of this method is unspecified until
     * validate() has been done.
     *
     * @return Returns true if the identifierref of this element references
     * a ResourceElement in this tree.
     */
    public boolean isIdentifierrefValid() {
        return isIdentifierrefValid;
    }
    
    public ResourceElement getResource() {
        return (getIdentifierref() == null)
        ? null
                : getIMSManifestDocument().getResourcesElement().findResource(getIdentifierref());
    }
}
