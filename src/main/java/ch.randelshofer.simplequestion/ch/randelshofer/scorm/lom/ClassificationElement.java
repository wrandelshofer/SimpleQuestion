/* @(#)ClassificationElement.java
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
 * Represents a SCORM 1.2 LOM 'classification' Element.
 * <p>
 * The outermost, root element. This element indicates the beginning of the
 * SCORM Meta-data XML record.
 * <p>
 * A 'classification' element has a structure as shown below.
 * Square brackets [ ] denote zero or one occurences.
 * Braces { } denote zero or more occurences.
 * Text in <b>bold</b> denotes non-terminal symbols.
 * <pre>
 * <b>classification</b> ::= &lt;classification&gt;
 *                    [<b>purpose</b>]
 *                    {<b>taxonpath</b>}
 *                    [<b>description</b>]
 *                    {<b>keyword</b>}
 *                    &lt;/classification&gt;
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
public class ClassificationElement extends AbstractElement {
    static final long serialVersionUID = 1L;
    
    /** Creates a new instance. */
    public ClassificationElement() {
    }
    
    /**
     * Parses the specified DOM Element and incorporates its contents into this element.
     * @param elem An XML element with the tag name 'file'.
     */
    public void parse(Element elem)
    throws IOException, ParserConfigurationException, SAXException {
        if (! DOMs.isElement(elem, LOM.NS, "classification")) {
            throw new IOException("'classification' element expected, but found '"+elem.getTagName()+"' element.");
        }
    }

    public void dump(StringBuffer buf, int depth) {
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("<html><font size=-1 face=SansSerif>");
        if (! isValid()) buf.append("<font color=red>* </font>");
        buf.append("<b>Classification</b> ");
        
        buf.append("</font>");
        return buf.toString();
    }
}
