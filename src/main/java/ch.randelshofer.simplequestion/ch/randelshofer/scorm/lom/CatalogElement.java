/* @(#)CatalogElement.java
 * Copyright © Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.scorm.lom;

import ch.randelshofer.scorm.AbstractElement;
import ch.randelshofer.xml.DOMs;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Represents a SCORM 1.2 LOM 'catalog' element.
 * <p>
 * This data element describes the name of the catalog (i.e. listing
 * identification system).
 * <p>
 * A 'catalog' element has a structure as shown below.
 * Square brackets [ ] denote zero or one occurences.
 * Braces { } denote zero or more occurences.
 * Text in <b>bold</b> denotes non-terminal symbols.
 * <pre>
 * <b>catalog</b> ::= &lt;catalog&gt;<b>string</b>&lt;/catalog&gt;
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
public class CatalogElement extends AbstractElement {
    private final static long serialVersionUID = 1L;
    /**
     * The text of the element.
     */
    private String text;

    /**
     * Creates a new instance of CatalogElement
     */
    public CatalogElement() {
    }

    /**
     * Parses the specified DOM Element and incorporates its contents into this element.
     *
     * @param elem An XML element with the tag name 'title'.
     */
    public void parse(Element elem)
            throws IOException, ParserConfigurationException, SAXException {
        if (!DOMs.isElement(elem, LOM.NS, "catalog")) {
            throw new IOException("'catlaog' element expected, but found '" + elem.getTagName() + "' element.");
        }

        // Read the text of the element
        this.text = DOMs.getText(elem);
    }

    /**
     * Dumps the contents of this subtree into the provided string buffer.
     */
    public void dump(StringBuffer buf, int depth) {
        for (int i = 0; i < depth; i++) {
            buf.append('.');
        }
        buf.append("<catalog>" + text + "</catalog>\n");
    }

    public String getText() {
        return text;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("<html><font size=-1 face=SansSerif>");
        if (!isValid()) {
            buf.append("<font color=red>* </font>");
        }
        buf.append("<b>Catalog:</b> " + text);
        buf.append("</font>");
        return buf.toString();
    }
}
