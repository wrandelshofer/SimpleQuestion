/* @(#)ValueElement.java
 *
 * Copyright (c) 2001 Werner Randelshofer
 * Staldenmattweg 2, Immensee, CH-6405, Switzerland.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Werner Randelshofer. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Werner Randelshofer.
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
 * Represents a SCORM 1.2 LOM 'value' Element.
 * <p>
 * A 'entry' element has a structure as shown below.
 * Square brackets [ ] denote zero or one occurences.
 * Braces { } denote zero or more occurences.
 * Text in <b>bold</b> denotes non-terminal symbols.
 * <pre>
 * <b>value</b> ::= &lt;value&gt;
 *           <b>langstring</b>
 *           {<b>langstring</b>}
 *           &lt;/value&gt;
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
public class ValueElement extends AbstractElement {
    private final static long serialVersionUID=1L;
    private LinkedList<LangStringElement> langstringList = new LinkedList<>();
    
    /** Creates a new instance. */
    public ValueElement() {
    }
    
    /**
     * Parses the specified DOM Element and incorporates its contents into this element.
     * @param elem An XML element with the tag name 'file'.
     */
    public void parse(Element elem)
    throws IOException, ParserConfigurationException, SAXException {
        if (! DOMs.isElement(elem, LOM.NS, "value")) {
            throw new IOException("'value' element expected, but found '"+elem.getTagName()+"' element.");
        }
        
        // Read the child elements
        NodeList nodes = elem.getChildNodes();
        for (int i=0; i < nodes.getLength(); i++) {
            if (nodes.item(i) instanceof Element) {
                Element child = (Element) nodes.item(i);
                
                if (DOMs.isElement(child, LOM.NS, "langstring")) {
                    LangStringElement item = new LangStringElement();
                    add(item);
                    this.langstringList.add(item);
                    item.parse(child);
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
        buf.append("<b>Value</b> ");
        buf.append("</font>");
        return buf.toString();
    }
}
