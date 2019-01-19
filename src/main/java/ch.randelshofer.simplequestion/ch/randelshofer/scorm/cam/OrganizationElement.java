/* @(#)OrganizationElement.java
 *
 * Copyright (c) 2001-2006 Werner Randelshofer
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
import ch.randelshofer.util.IdentifierGenerator;
import ch.randelshofer.util.Strings;
import ch.randelshofer.xml.DOMs;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.tree.TreeNode;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
/**
 * Represents a SCORM CAM 'organization' element.
 * <p>
 * This element describes a particular organization.
 * <p>
 * An 'organization' element has a structure as shown below.
 * Square brackets [ ] denote zero or one occurences.
 * Braces { } denote zero or more occurences.
 * <pre>
 * &lt;organization identifier="ID" [structure="string"]&gt;
 * [&lt;title&gt;]
 * {&lt;item&gt;}
 * [&lt;metadata&gt;]
 * &lt;/organization&gt;
 * </pre>
 * <p>
 * This implementation ignores the metadata element.
 * <p>
 * As of the SCORM 1.2 specification a CAM 'organization' can only have
 * a 'hierarchical' structure. Future versions may support additional structures.
 * <p>
 * This implementation also supports a 'layered' structure. A layered structure
 * consists of a sequence of learning content (rows). The learning content may be
 * presented in multiple layers (columns). A cell (specified by row and column)
 * may contain multiple SCO's.
 * <p>
 * If the structure of the organization is 'layered' then the ItemElement's in
 * this OrganizationElement are interpreted as shown here:
 * <ul>
 * <li>rows: ItemElement's that are direct children of this OrganizationElement
 *   specify the rows (the sequence of learning content).</li>
 * <li>columns: Direct children of the row ItemElement's specify the columns
 *   (the layers of a learning content).</li>
 * <li>cells: Children of the column ItemElement's specify entries in a cell
 *   (a cell contains one or multiple SCO's).</li>
 * </ul>
 *
 *
 * Reference:
 * ADL (2001c). Advanced Distributed Learning.
 * Sharable Content Object Reference Model (SCORM(TM)) Version 1.2.
 * The SCORM Content Aggregation Model. October 1, 2001.
 * Internet (2003-01-20): http://www.adlnet.org
 *
 * @author Werner Randelshofer, Staldenmattweg 2, Immensee, CH-6405, Switzerland
 * @version 1.2 2006-10-10 Parse with XML namespaces.
 * <br>1.1.1 2006-06-01 Encode unicode characters in title with HTML entities.
 * <br>1.1 2002-11-03 Support for Metadata element implemented.
 * <br>1.0 2003-10-30 Method getReferencedResources added. HTML output in
 * method toString changed.
 * <br>0.17 2003-03-16 Method getColumnTitles added.
 * Naming conventions streamlined with JavaScript code of TinyLMS.
 * <br>0.2 2003-02-20 Umlauts were not properly escaped.
 * <br>0.1 2003-02-02 Created.
 */
public class OrganizationElement extends AbstractElement {
    private final static long serialVersionUID=1L;
    /**
     * identifier (required). An identifier provided by an author or authoring
     * tool, that is unique within the Manifest. Data type = ID.
     */
    private String identifier;
    /**
     * structure (optional). Assumes a default value of "hierarchical" such
     * as is common with a tree view or structural representation of data.<br><br>
     *
     * This implementation also supports a 'layered' structure. A layered structure
     * consists of a sequence of learning content (rows). The learning content may be
     * presented in multiple layers (columns). A cell (specified by row and column)
     * may contain multiple SCO's.
     */
    private String structure;
    private TitleElement titleElement;
    private LinkedList<ItemElement> itemList = new LinkedList<>();
    
    /**
     * Contains context specific meta-data that is used to describe the
     * organization.
     */
    private MetadataElement metadataElement;
    
    /** Creates a new instance of OrganizationElement */
    public OrganizationElement() {
    }
    
    /**
     * Parses the specified DOM Element and incorporates its contents into this element.
     * @param elem An XML element with the tag name 'organization'.
     */
    public void parse(Element elem)
    throws IOException, ParserConfigurationException, SAXException {
        if (! DOMs.isElement(elem, CAM.IMSCP_NS, "organization")) {
            throw new IOException("'adlcp:organization' element expected, but found '"+elem.getTagName()+"' element.");
        }
        this.identifier = DOMs.getAttributeNS(elem, CAM.IMSCP_NS, "identifier", null);
        this.structure = DOMs.getAttributeNS(elem, CAM.IMSCP_NS, "structure", null);
        
        // Read the child elements
        NodeList nodes = elem.getChildNodes();
        for (int i=0; i < nodes.getLength(); i++) {
            if (nodes.item(i) instanceof Element) {
                Element child = (Element) nodes.item(i);
                if (DOMs.isElement(child, CAM.IMSCP_NS, "title")) {
                    if (this.titleElement != null) throw new IOException("'title' element may only be specified once whithin a 'organization' element.");
                    this.titleElement = new TitleElement();
                    add(titleElement);
                    this.titleElement.parse(child);
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
        buf.append("<organization identifier=\""+identifier+"\" structure=\""+structure+"\">\n");
        titleElement.dump(buf, depth+1);
        Iterator<ItemElement> iter = itemList.iterator();
        while (iter.hasNext()) {
            ((AbstractElement) iter.next()).dump(buf, depth+1);
        }
        for (int i=0; i < depth; i++) buf.append('.');
        buf.append("</organization>\n");
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
        String title = getTitle();
        if (title == null) title = "TinyLMS Course";
        out.println("new OrganizationElement(\""+
                gen.getIdentifier(getIdentifier())+"\",\""+
                Strings.escapeHTML(Strings.escapeUnicodeWithHTMLEntities(title))+"\",[");
        
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
    
    public String getIdentifier() {
        return identifier;
    }
    public String getTitle() {
        return (titleElement == null) ? null : titleElement.getTitle();
    }
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("<html><font size=-1 face=SansSerif>");
        if (! isValid()) buf.append("<font color=red>* </font>");
        buf.append("<b>Organization</b> id:");
        if (! isIdentifierValid()) buf.append("<font color=red>");
        buf.append(identifier);
        if (! isIdentifierValid()) buf.append(" <b>DUPLICATE ID</b></font>");
        buf.append("</font>");
        return buf.toString();
    }
    
    /**
     * If the structure of the organization is 'layered' then the organization is
     * structured as follows:
     * - rows: ItemElement's that are direct children of this OrganizationElement
     *   specify the rows.
     * - columns: Direct children of the row ItemElement's specify the columns.
     * - cells: Children of the column ItemElement's specify entries in a cell.
     *
     * This method returns all distinct titles of the column ItemElement's.
     */
    public ArrayList<String> getDistinctColumnTitles() {
        HashSet<String> set = new HashSet<>();
        ArrayList<String> titles = new ArrayList<>();
        Iterator<ItemElement> i = itemList.iterator();
        while (i.hasNext()) {
            ItemElement row = (ItemElement) i.next();
            Iterator<ItemElement> j = row.getItemList().iterator();
            while (j.hasNext()) {
                ItemElement column = (ItemElement) j.next();
                if (! set.contains(column.getTitle())) {
                    set.add(column.getTitle());
                    titles.add(column.getTitle());
                }
            }
        }
        return titles;
    }
    
    public List<ItemElement> getItemList() {
        return Collections.unmodifiableList(itemList);
    }
    
    /**
     * Returns all ResourceElement's that are referenced by this organization.
     */
    public HashSet<ResourceElement> getReferencedResources() {
        ResourcesElement resources = getIMSManifestDocument().getResourcesElement();
        HashSet<ResourceElement> referencedResources = new HashSet<>();
        
        Enumeration<TreeNode> enm = preorderEnumeration();
        while (enm.hasMoreElements()) {
            AbstractElement element = (AbstractElement) enm.nextElement();
            if (element instanceof ItemElement) {
                ItemElement item = (ItemElement) element;
                if (item.getIdentifierref() != null) {
                    ResourceElement resource =(ResourceElement) resources.findChildByIdentifier(item.getIdentifierref());
                    if (resource != null) {
                        referencedResources.add(resource);
                    }
                }
            }
        }
        return referencedResources;
    }
}
