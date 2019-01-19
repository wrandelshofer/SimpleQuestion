/* @(#)CatalogEntryElement.java
 * Copyright © Werner Randelshofer, Switzerland. MIT License.
 */


package ch.randelshofer.scorm.lom;

import ch.randelshofer.scorm.AbstractElement;
import ch.randelshofer.util.*;
import ch.randelshofer.xml.*;
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
 * Represents a SCORM 1.2 LOM 'catalogentry' Element.
 * <p>
 * This data element describes an entry within a catalog (i.e. a listing
 * identification system) assigned to this learning resource. This sub-category
 * shall describe this learning resource according to some known cataloging
 * system so that it may be externally searched for and located according to the
 * methodology of the specified system.
 * <p>
 * A 'catalogentry' element has a structure as shown below.
 * Square brackets [ ] denote zero or one occurences.
 * Braces { } denote zero or more occurences.
 * Text in <b>bold</b> denotes non-terminal symbols.
 * <pre>
 * <b>catalogentry</b> ::= &lt;catalogentry&gt;
 *                  <b>catalog</b>
 *                  <b>entry</b>
 *                  &lt;/catalogentry&gt;
 * </pre>
 * Reference:
 * ADL (2001). Advanced Distributed Learning.
 * Sharable Content Object Reference Model (SCORM(TM)) Version 1.2.
 * The SCORM Content Aggregation Model. October 1, 2001.
 * Internet (2003-01-20): http://www.adlnet.org
 *
 * @author  Werner Randelshofer
 * @version 1.1 2006-10-11 Parse using XML namespaces.
 * <br>1.0.1  2004-01-19  Comments updated.
 * <br>1.0  2004-01-05  Created.
 */
public class CatalogEntryElement extends AbstractElement {
    static final long serialVersionUID = 1L;
    private CatalogElement catalogElement;
    private EntryElement entryElement;
    
    /** Creates a new instance. */
    public CatalogEntryElement() {
    }
    
    /**
     * Parses the specified DOM Element and incorporates its contents into this element.
     * @param elem An XML element with the tag name 'file'.
     */
    public void parse(Element elem)
    throws IOException, ParserConfigurationException, SAXException {
        if (! DOMs.isElement(elem, LOM.NS, "catalogentry")) {
            throw new IOException("'catalogentry' element expected, but found '"+elem.getTagName()+"' element.");
        }
        // Read the child elements
        NodeList nodes = elem.getChildNodes();
        for (int i=0; i < nodes.getLength(); i++) {
            if (nodes.item(i) instanceof Element) {
                Element child = (Element) nodes.item(i);
                
                if (DOMs.isElement(child, LOM.NS, "catalog")) {
                    if (this.catalogElement != null) throw new IOException("'catalog' element may only be specified once whithin a 'catalogentry' element.");
                    this.catalogElement = new CatalogElement();
                    add(catalogElement);
                    this.catalogElement.parse(child);
                } else if (DOMs.isElement(child, LOM.NS, "entry")) {
                    if (this.entryElement != null) throw new IOException("'entry' element may only be specified once whithin a 'catalogentry' element.");
                    this.entryElement = new EntryElement();
                    add(entryElement);
                    this.entryElement.parse(child);
                }
            }
        }
    }
    
    public void dump(StringBuffer buf, int depth) {
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("<html><font size=-1 face=SansSerif>");
        if (! isValid()) buf.append("<font color=red>* </font>");
        buf.append("<b>CatalogEntry</b> ");
        
        buf.append("</font>");
        return buf.toString();
    }
}
