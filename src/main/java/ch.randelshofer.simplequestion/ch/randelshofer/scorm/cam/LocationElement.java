/*
 * @(#)LocationElement.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.scorm.cam;

import ch.randelshofer.scorm.AbstractElement;
import ch.randelshofer.util.Strings;
import ch.randelshofer.xml.DOMs;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Set;

/**
 * Represents a SCORM 1.2 CAM 'adclp:location' element.
 * <p>
 * This element describes the location where the meta-data describing the
 * Content Packaging component may be found. This may be a Universal
 * Resource Indicator (URI). This is an ADL namespaced element extension to
 * the IMS Content Packaging Specification. The meta-data creator has two
 * options for expressing meta-data in a Content Package. The creator can
 * either use the <adlcp:location> element to express the location of the
 * meta-data record or place the meta-data inline within the Manifest file.
 * <p>
 * A 'adlcp:location' element has a structure as shown below.
 * Square brackets [ ] denote zero or one occurences.
 * Braces { } denote zero or more occurences.
 * Text in <b>bold</b> denotes non-terminal symbols.
 * <pre>
 * <b>location</b> ::= &lt;adlcp:location&gt;<b>string</b>&lt;/adlcp:location&gt;
 * </pre>
 * <p>
 * Reference:
 * ADL(2001c). Advanced Distributed Learning.
 * Sharable Content Object Reference Model(SCORM(TM)) Version 1.2.
 * The SCORM Content Aggregation Model. October 1, 2001.
 * Internet(2003-01-20): http://www.adlnet.org
 *
 * @author Werner Randelshofer
 * @version 1.1 2006-10-10 Parse with XML namespaces.
 * <br>1.0 5. Januar 2004  Created.
 */
public class LocationElement extends AbstractElement {
    static final long serialVersionUID = 1L;
    private String uri;

    private boolean isLocationValid;

    public String getUri() {
        return uri;
    }

    /**
     * Gets a location relative to
     * the content package or an absolute href.
     */
    public String getConsolidatedURI() {
        return Strings.unescapeURL(uri);
    }

    /**
     * Parses the specified DOM Element and incorporates its contents into this element.
     *
     * @param elem An XML element with the tag name 'file'.
     */
    public void parse(Element elem)
            throws IOException, ParserConfigurationException, SAXException {
        if (!DOMs.isElement(elem, CAM.ADLCP_NS, "location")) {
            throw new IOException("'adlcp:location' element expected, but found '" + elem.getTagName() + "' element.");
        }

        // Read the text of the element
        uri = DOMs.getText(elem);
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

        Set<String> fileNames = getIMSManifestDocument().getFileNames();
        isLocationValid = fileNames.contains(getConsolidatedURI());

        return isValid = isValid && isLocationValid;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("<html><font size=-1 face=SansSerif>");
        if (!isValid()) {
            buf.append("<font color=red>* </font>");
        }
        buf.append("<b>Location</b> ");
        buf.append(uri);

        buf.append("</font>");
        return buf.toString();
    }

    /**
     * Removes all file names in the set, which are referenced by this
     * CAM Element.
     */
    public void consumeFileNames(Set<String> fileNames) {
        fileNames.remove(getConsolidatedURI());
    }
}
