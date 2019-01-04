/*
 * @(#)OrganizationsElement.java  1.1  2006-10-10
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
 * Represents a SCORM 1.2 CAM 'organizations' element.
 * <p>
 * This element describes one or more structures or organizations for this package.
 * <p>
 * An 'organizations' element has a structure as shown below.
 * Square brackets [ ] denote zero or one occurences.
 * Braces { } denote zero or more occurences. 
 * Text in <b>bold</b> denotes non-terminal symbols.
 * <pre>
 * <b>organizations</b> ::= &lt;organizations default="<b>IDREF</b>"&gt;
 *                   {<b>organization</b>}
 *                   &lt;/organizations&gt;
 * </pre>
 *
 * Reference:
 * ADL (2001). Advanced Distributed Learning.
 * Sharable Content Object Reference Model (SCORM(TM)) Version 1.2.
 * The SCORM Content Aggregation Model. October 1, 2001.
 * Internet (2003-01-20): http://www.adlnet.org
 *
 * @author Werner Randelshofer, Staldenmattweg 2, Immensee, CH-6405, Switzerland
 * @version 1.1 2006-10-10 Parse with XML namespaces. 
 * <br>1.0.1 2004-01-19 Comments updated.
 * <br>1.0 2003-10-30 HTML output in method toString changed. 
 * <br>0.18 2003-05-09 Export the default organization only.
 * <br>0.17 2003-03-16 Naming conventions streamlined with JavaScript code
 * of the LMS.
 * <br>0.2 2003-03-03 Revised.
 * <br>0.1 2003-02-02 Created.
 */
public class OrganizationsElement extends AbstractElement {
    private final static long serialVersionUID=1L;
    /**
     * This attribute is set by validate().
     */
    private boolean isDefaultOrganizationValid = true;
    /**
     * Identifies the default organization to use.
     * Data type = IDREF.
     */
    private String defaultOrganization;
    /**
     * The list of organization elements.
     */
    private LinkedList<OrganizationElement> organizationList = new LinkedList<>();
    
    /** Creates a new instance of OrganizationsElement */
    public OrganizationsElement() {
    }
    
    /**
     * Parses the specified DOM Element and incorporates its contents into this element.
     * @param elem An XML element with the tag name 'organizations'.
     */
    public void parse(Element elem)
    throws IOException, ParserConfigurationException, SAXException {
        if (! DOMs.isElement(elem, CAM.IMSCP_NS, "organizations")) {
            throw new IOException("'adlcp:organizations' element expected, but found '"+elem.getTagName()+"' element.");
        }
        this.defaultOrganization = DOMs.getAttributeNS(elem, CAM.IMSCP_NS, "default", null);
        
        // Read the child elements
        NodeList nodes = elem.getChildNodes();
        for (int i=0; i < nodes.getLength(); i++) {
            if (nodes.item(i) instanceof Element) {
                Element child = (Element) nodes.item(i);
                if (DOMs.isElement(child, CAM.IMSCP_NS, "organization")) {
                    OrganizationElement orga = new OrganizationElement();
                    add(orga);
                    this.organizationList.add(orga);
                    orga.parse(child);
                }
            }
        }
    }
    
    /**
     * Dumps the contents of this subtree into the provided string buffer.
     */
    public void dump(StringBuffer buf, int depth) {
        for (int i=0; i < depth; i++) buf.append('.');
        buf.append("<organizations default=\""+defaultOrganization+"\">\n");
        for (OrganizationElement organizationElement : organizationList) {
            ((AbstractElement) organizationElement).dump(buf, depth + 1);
        }
        for (int i=0; i < depth; i++) buf.append('.');
        buf.append("</organizations>\n");
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
        out.println("new OrganizationsElement([");
        Iterator<OrganizationElement> iter = organizationList.iterator();
        while (iter.hasNext()) {
            OrganizationElement elem = ((OrganizationElement) iter.next());
            if (elem.getIdentifier().equals(defaultOrganization)) {
                elem.exportToJavaScript(out, depth + 1, gen);
                break;
            }
            /*
            if (iter.hasNext()){
                out.println(",");
            }*/
        }
        out.println();
        indent(out, depth);
        out.print((depth == 0) ? "]);" : "])");
    }
    
    public String toString() {
        
        StringBuffer buf = new StringBuffer();
        buf.append("<html><font size=-1 face=SansSerif>");
        if (! isValid()) buf.append("<font color=red>* </font>");
        buf.append("<b>Organizations</b> default:");
        if (! isDefaultOrganizationValid()) buf.append("<font color=red>");
        buf.append(defaultOrganization);
        if (! isDefaultOrganizationValid()) buf.append(" <b>NO DEFAULT ORGANIZATION SPECIFIED</b></font>");
        buf.append("</font>");
        return buf.toString();
    }
    
    /**
     * Returns the default organization.
     * Performance penalty: Does a linear search through its child nodes.
     */
    public OrganizationElement getDefaultOrganizationElement() {
        for (int i=0; i < getChildCount(); i++) {
            OrganizationElement elem = (OrganizationElement) getChildAt(i);
            if (elem.getIdentifier().equals(defaultOrganization)) {
                return elem;
            }
        }
        return (OrganizationElement) ((getChildCount() > 0) ? getChildAt(0) : null);
    }
    /**
     * Returns the default organization.
     * Performance penalty: Does a linear search through its child nodes.
     */
    public void setDefaultOrganizationElement(OrganizationElement elem) {
        defaultOrganization = elem.getIdentifier();
    }
    /**
     * Validates this CAM element.
     *
     * @return Returns true if this elements is valid.
     */
    public boolean validate() {
        isValid = super.validate();
        isDefaultOrganizationValid = false;
        for (int i=0; i < getChildCount(); i++) {
            OrganizationElement elem = (OrganizationElement) getChildAt(i);
            if (elem.getIdentifier().equals(defaultOrganization)) {
                isDefaultOrganizationValid = true;
                break;
            }
        }
        if (! isDefaultOrganizationValid) isValid = false;
        return isValid;
    }
    /**
     * The return value of this method is unspecified until
     * validate() has been done.
     *
     * @return Returns true if the 'default' attribute of this element references
     * an OrganizationElement in the subtree of this OrganizationsElement.
     */
    public boolean isDefaultOrganizationValid() {
        return isDefaultOrganizationValid;
    }
}
