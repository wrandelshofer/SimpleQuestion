/* @(#)AggregationLevelElement.java
 * Copyright © Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.scorm.lom;

import ch.randelshofer.scorm.AbstractElement;
import ch.randelshofer.xml.DOMs;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
/**
 * Represents a SCORM 1.2 LOM 'aggregationlevel' Element.
 * <p>
 * This data element describes the functional granularity of this learning 
 * resource. The vocabularies defined for this element are restricted 
 * vocabularies.
 * <p>
 * An 'aggregationlevel' element has a structure as shown below.
 * Square brackets [ ] denote zero or one occurences.
 * Braces { } denote zero or more occurences.
 * Text in <b>bold</b> denotes non-terminal symbols.
 * <pre>
 * <b>aggregationlevel</b> ::= &lt;aggregationlevel&gt;
 *                     <b>vocabulary</b>
 *                     &lt;/aggregationlevel&gt;
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
public class AggregationLevelElement extends AbstractElement {
    private final static long serialVersionUID=1L;
    private VocabularyElement vocabularyElement;
    private SourceElement sourceElement;
    private ValueElement valueElement;
    
    /** Creates a new instance. */
    public AggregationLevelElement() {
    }
    
    /**
     * Parses the specified DOM Element and incorporates its contents into this element.
     * @param elem An XML element with the tag name 'file'.
     */
    public void parse(Element elem)
    throws IOException, ParserConfigurationException, SAXException {
        if (! DOMs.isElement(elem, LOM.NS, "aggregationlevel")) {
            throw new IOException("'aggregationlevel' element expected, but found '"+elem.getTagName()+"' element.");
        }
        
        // Read the child elements
        NodeList nodes = elem.getChildNodes();
        for (int i=0; i < nodes.getLength(); i++) {
            if (nodes.item(i) instanceof Element) {
                Element child = (Element) nodes.item(i);
                if (DOMs.isElement(child, LOM.NS, "vocabulary")) {
                    if (vocabularyElement != null) throw new IOException("'vocabulary' element may only be specified once whithin a 'catalogentry' element.");
                    vocabularyElement = new VocabularyElement();
                    add(vocabularyElement);
                    vocabularyElement.parse(child);
                } else if (DOMs.isElement(child, LOM.NS, "source")) {
                    if (sourceElement != null) throw new IOException("'source' element may only be specified once whithin a 'catalogentry' element.");
                    sourceElement = new SourceElement();
                    add(sourceElement);
                    sourceElement.parse(child);
                } else if (DOMs.isElement(child, LOM.NS, "value")) {
                    if (valueElement != null) throw new IOException("'value' element may only be specified once whithin a 'catalogentry' element.");
                    valueElement = new ValueElement();
                    add(valueElement);
                    valueElement.parse(child);
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
        buf.append("<b>AggregationLevel</b> ");
        
        buf.append("</font>");
        return buf.toString();
    }
}
