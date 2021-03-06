/*
 * @(#)SchemaElement.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */


package ch.randelshofer.scorm.cam;

import ch.randelshofer.scorm.AbstractElement;
import ch.randelshofer.xml.DOMs;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Represents a SCORM 1.2 CAM 'schema' Element.
 * <p>
 * This element describes the schema that defines the meta-data.
 * This element may occurs 0 or 1 time within a &lt;metadata&gt; element.
 * <p>
 * A 'schema' element has a structure as shown below.
 * Square brackets [ ] denote zero or one occurences.
 * Braces { } denote zero or more occurences.
 * Text in <b>bold</b> denotes non-terminal symbols.
 * <pre>
 * <b>schema</b> ::= &lt;schema&gt;<b>string</b>&lt;/schema&gt;
 * </pre>
 * <p>
 * Reference:
 * ADL (2001). Advanced Distributed Learning.
 * Sharable Content Object Reference Model (SCORM(TM)) Version 1.2.
 * The SCORM Content Aggregation Model. October 1, 2001.
 * Internet (2003-01-20): http://www.adlnet.org
 *
 * @author Werner Randelshofer
 * @version 1.1 2006-10-10 Parse with XML namespaces.
 * <br>1.0.1  2004-01-19  Comments updated.
 * <br>1.0  5. Januar 2004  Created.
 */
public class SchemaElement extends AbstractElement {
    static final long serialVersionUID = 1L;
    private String description;

    /**
     * Creates a new instance.
     */
    public SchemaElement() {
    }

    public String getDescription() {
        return description;
    }

    /**
     * Parses the specified DOM Element and incorporates its contents into this element.
     *
     * @param elem An XML element with the tag name 'file'.
     */
    public void parse(Element elem)
            throws IOException, ParserConfigurationException, SAXException {
        if (!DOMs.isElement(elem, CAM.IMSCP_NS, "schema")) {
            throw new IOException("'adlcp:schema' element expected, but found '" + elem.getTagName() + "' element.");
        }

        // Read the text of the element
        description = DOMs.getText(elem);
    }

    public void dump(StringBuffer buf, int depth) {
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("<html><font size=-1 face=SansSerif>");
        if (!isValid()) {
            buf.append("<font color=red>* </font>");
        }
        buf.append("<b>Schema</b> ");
        buf.append(description);

        buf.append("</font>");
        return buf.toString();
    }
}
