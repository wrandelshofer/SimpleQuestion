/*
 * @(#)CoverageElement.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */


package ch.randelshofer.scorm.lom;

import ch.randelshofer.scorm.AbstractElement;
import ch.randelshofer.xml.DOMs;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Represents a SCORM 1.2 LOM 'coverage' Element.
 * <p>
 * This data element describes the aspan or extent of such things as time,
 * culture, geography or region that applies to this learning resource.
 * <p>
 * A 'coverage' element has a structure as shown below.
 * Square brackets [ ] denote zero or one occurences.
 * Braces { } denote zero or more occurences.
 * Text in <b>bold</b> denotes non-terminal symbols.
 * <pre>
 * <b>coverage</b> ::= &lt;coverage&gt;
 *           <b>langstring</b>
 *           {<b>langstring</b>}
 *           &lt;/coverage&gt;
 * </pre>
 * Reference:
 * ADL (2001). Advanced Distributed Learning.
 * Sharable Content Object Reference Model (SCORM(TM)) Version 1.2.
 * The SCORM Content Aggregation Model. October 1, 2001.
 * Internet (2003-01-20): http://www.adlnet.org
 *
 * @author Werner Randelshofer
 * @version 1.1 2006-10-11 Parse using XML namespaces.
 * <br>1.0.1  2004-01-19  Comments updated.
 * <br>1.0  2004-01-05  Created.
 */
public class CoverageElement extends AbstractElement {
    private final static long serialVersionUID = 1L;
    private ArrayList<LangStringElement> langstringList = new ArrayList<>();

    /**
     * Creates a new instance.
     */
    public CoverageElement() {
    }

    /**
     * Parses the specified DOM Element and incorporates its contents into this element.
     *
     * @param elem An XML element with the tag name 'file'.
     */
    public void parse(Element elem)
            throws IOException, ParserConfigurationException, SAXException {
        if (!DOMs.isElement(elem, LOM.NS, "coverage")) {
            throw new IOException("'coverage' element expected, but found '" + elem.getTagName() + "' element.");
        }
        // Read the child elements
        NodeList nodes = elem.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
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
        if (!isValid()) {
            buf.append("<font color=red>* </font>");
        }
        buf.append("<b>Coverage</b> ");

        buf.append("</font>");
        return buf.toString();
    }
}
