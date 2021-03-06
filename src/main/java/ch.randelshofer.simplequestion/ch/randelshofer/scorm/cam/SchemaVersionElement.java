/*
 * @(#)SchemaVersionElement.java
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
 * Represents a SCORM 1.2 CAM 'schemaversion' element.
 * <p>
 * This element describes the version of the schema that defines the
 * meta-data. This element is optional, however if present it must contain
 * the value of "1.2".
 * This element may occurs 0 or 1 time within a &lt;metadata&gt; element.
 * <p>
 * A 'schema' element has a structure as shown below.
 * Square brackets [ ] denote zero or one occurences.
 * Braces { } denote zero or more occurences.
 * Text in <b>bold</b> denotes non-terminal symbols.
 * <pre>
 * <b>schemaversion</b> ::= &lt;schemaversion&gt;<b>string</b>&lt;/schemaversion&gt;
 * </pre>
 * <p>
 * Reference:
 * ADL (2001c). Advanced Distributed Learning.
 * Sharable Content Object Reference Model (SCORM(TM)) Version 1.2.
 * The SCORM Content Aggregation Model. October 1, 2001.
 * Internet (2003-01-20): http://www.adlnet.org
 *
 * @author Werner Randelshofer
 * @version 1.1 2006-10-10 Parse with XML namespaces.
 * <br>1.0 5. Januar 2004  Created.
 */
public class SchemaVersionElement extends AbstractElement {
    private final static long serialVersionUID = 1L;
    private String version;
    private boolean isSchemaversionValid;

    public String getVersion() {
        return version;
    }

    /**
     * Parses the specified DOM Element and incorporates its contents into this element.
     *
     * @param elem An XML element with the tag name 'file'.
     */
    public void parse(Element elem)
            throws IOException, ParserConfigurationException, SAXException {
        if (!DOMs.isElement(elem, CAM.IMSCP_NS, "schemaversion")) {
            throw new IOException("'schemaversion' element expected, but found '" + elem.getTagName() + "' element.");
        }

        // Read the text of the element
        version = DOMs.getText(elem);
    }

    public void dump(StringBuffer buf, int depth) {
    }

    /**
     * Validates this CAM element.
     *
     * @return Returns true if this elements is valid.
     */
    public boolean validate() {
        isValid = super.validate();

        isSchemaversionValid = version != null && version.equals("1.2");

        return isValid = isValid && isSchemaversionValid;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("<html><font size=-1 face=SansSerif>");
        if (!isValid()) {
            buf.append("<font color=red>* </font>");
        }
        buf.append("<b>SchemaVersion</b> ");
        buf.append(version);

        buf.append("</font>");
        return buf.toString();
    }
}
