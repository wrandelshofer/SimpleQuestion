/*
 * @(#)MetaMetadataElement.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */


package ch.randelshofer.scorm.lom;

import ch.randelshofer.scorm.AbstractElement;
import ch.randelshofer.xml.DOMs;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Represents a SCORM 1.2 LOM 'metametadata' Element.
 * <p>
 * This data element describes the information about the meta-data record itself
 * (rather than the learning resource that this record describes).
 * <p>
 * A 'metametadata' element has a structure as shown below.
 * Square brackets [ ] denote zero or one occurences.
 * Braces { } denote zero or more occurences.
 * Text in <b>bold</b> denotes non-terminal symbols.
 * <pre>
 * <b>metametadata</b> ::= &lt;metametadata&gt;
 *                  [<b>identifier</b>]
 *                  {<b>catalogentry</b>}
 *                  {<b>contribute</b>}
 *                  {<b>metadatascheme</b>}
 *                  [<b>language</b>]
 *                  &lt;/metametadata&gt;
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
 * <br>1.0  5. Januar 2004  Created.
 */
public class MetaMetadataElement extends AbstractElement {
    private final static long serialVersionUID = 1L;
    /*
    private GeneralElement generalElement;
    private LifecycleElement lifecycleElement;
    private MetametadataElement metametadataElement;
    private TechnicalElement technicalElement;
    private EducationalElement educationalElement;
    private RightsElement rightsElement;
    private RelationElement relationElement;
    private AnnotationElement annotationElement;
    private ClassificationElement classificationElement;*/

    /**
     * Creates a new instance.
     */
    public MetaMetadataElement() {
    }

    /**
     * Parses the specified DOM Element and incorporates its contents into this element.
     *
     * @param elem An XML element with the tag name 'file'.
     */
    public void parse(Element elem)
            throws IOException, ParserConfigurationException, SAXException {
        if (!DOMs.isElement(elem, LOM.NS, "metametadata")) {
            throw new IOException("'metametadata' element expected, but found '" + elem.getTagName() + "' element.");
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
        buf.append("<b>MetaMetadata</b> ");

        buf.append("</font>");
        return buf.toString();
    }
}
