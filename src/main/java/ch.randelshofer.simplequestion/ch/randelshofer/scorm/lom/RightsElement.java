/*
 * @(#)RightsElement.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.scorm.lom;

import ch.randelshofer.scorm.AbstractElement;
import ch.randelshofer.xml.DOMs;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class RightsElement extends AbstractElement {
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
    public RightsElement() {
    }

    /**
     * Parses the specified DOM Element and incorporates its contents into this element.
     *
     * @param elem An XML element with the tag name 'file'.
     */
    public void parse(Element elem)
            throws IOException, ParserConfigurationException, SAXException {
        if (!DOMs.isElement(elem, LOM.NS, "rights")) {
            throw new IOException("'rights' element expected, but found '" + elem.getTagName() + "' element.");
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
        buf.append("<b>Rights</b> ");

        buf.append("</font>");
        return buf.toString();
    }
}
