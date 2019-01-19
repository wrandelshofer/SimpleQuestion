/* @(#)TechnicalElement.java
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
 * Represents a SCORM 1.2 LOM 'technical' Element.
 * <p>
 * This data element groups the technical requirements and characteristics of
 * the learning resource.
 * <p>
 * A 'technical' element has a structure as shown below.
 * Square brackets [ ] denote zero or one occurences.
 * Braces { } denote zero or more occurences.
 * Text in <b>bold</b> denotes non-terminal symbols.
 * <pre>
 * <b>technical</b> ::= &lt;technical&gt;
 *               {<b>format</b>}
 *               [<b>size</b>]
 *               {<b>location</b>}
 *               {<b>requirement</b>}
 *               [<b>installationremarks</b>]
 *               [<b>otherplatformrequirements</b>]
 *               [<b>duration</b>]
 *               &lt;/technical&gt;
 * </pre>
 * Reference:
 * ADL (2001). Advanced Distributed Learning.
 * Sharable Content Object Reference Model (SCORM(TM)) Version 1.2.
 * The SCORM Content Aggregation Model. October 1, 2001.
 * Internet (2003-01-20): http://www.adlnet.org
 *
 * @author Werner Randelshofer
 * @version 1.1 2006-10-11 Parse using XML namespaces.
 * <br>1.0.1 2004-01-19 Comments updated.
 * <br>1.0 5. Januar 2004  Created.
 */
public class TechnicalElement extends AbstractElement {
    private final static long serialVersionUID = 1L;

    /**
     * Creates a new instance.
     */
    public TechnicalElement() {
    }

    /**
     * Parses the specified DOM Element and incorporates its contents into this element.
     *
     * @param elem An XML element with the tag name 'file'.
     */
    public void parse(Element elem)
            throws IOException, ParserConfigurationException, SAXException {
        if (!DOMs.isElement(elem, LOM.NS, "technical")) {
            throw new IOException("'technical' element expected, but found '" + elem.getTagName() + "' element.");
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
        buf.append("<b>Technical</b> ");

        buf.append("</font>");
        return buf.toString();
    }
}
