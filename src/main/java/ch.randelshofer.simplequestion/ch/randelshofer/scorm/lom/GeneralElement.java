/*
 * @(#)GeneralElement.java
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
import java.util.LinkedList;

/**
 * Represents a SCORM 1.2 LOM 'general 'Element.
 * <p>
 * This data element describes the general information that describes the
 * learning resource as a whole.
 * <p>
 * A 'general' element has a structure as shown below.
 * Square brackets [ ] denote zero or one occurences.
 * Braces { } denote zero or more occurences.
 * Text in <b>bold</b> denotes non-terminal symbols.
 * <pre>
 * <b>general</b> ::= &lt;general&gt;
 *            [<b>identifier</b>]
 *            [<b>title</b>]
 *            {<b>catalogentry</b>}
 *            {<b>language</b>}
 *            {<b>description</b>}
 *            {<b>keyword</b>}
 *            {<b>coverage</b>}
 *            [<b>structure</b>]
 *            [<b>aggregationlevel</b>]
 *            &lt;/general&gt;
 * </pre>
 * Reference:
 * ADL (2001). Advanced Distributed Learning.
 * Sharable Content Object Reference Model (SCORM(TM)) Version 1.2.
 * The SCORM Content Aggregation Model. October 1, 2001.
 * Internet (2003-01-20): http://www.adlnet.org
 *
 * @author Werner Randelshofer
 * @version 1.1 2006-10-11 Parse using XML namespaces.
 * <br>1.0.1 2004-01-19  Comments updated.
 * <br>1.0 5. Januar 2004  Created.
 */
public class GeneralElement extends AbstractElement {
    private final static long serialVersionUID = 1L;

    private IdentifierElement identifierElement;
    private TitleElement titleElement;
    private LinkedList<CatalogEntryElement> catalogEntryList = new LinkedList<>();
    private LinkedList<LanguageElement> languageList = new LinkedList<>();
    private LinkedList<DescriptionElement> descriptionList = new LinkedList<>();
    private LinkedList<KeywordElement> keywordList = new LinkedList<>();
    private LinkedList<CoverageElement> coverageList = new LinkedList<>();
    private StructureElement structureElement;
    private AggregationLevelElement aggregationLevelElement;

    /**
     * Creates a new instance.
     */
    public GeneralElement() {
    }

    /**
     * Parses the specified DOM Element and incorporates its contents into this element.
     *
     * @param elem An XML element with the tag name 'file'.
     */
    public void parse(Element elem)
            throws IOException, ParserConfigurationException, SAXException {
        if (!DOMs.isElement(elem, LOM.NS, "general")) {
            throw new IOException("'general' element expected, but found '" + elem.getTagName() + "' element.");
        }
        // Read the child elements
        NodeList nodes = elem.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            if (nodes.item(i) instanceof Element) {
                Element child = (Element) nodes.item(i);

                if (DOMs.isElement(child, LOM.NS, "identifier")) {
                    if (identifierElement != null) {
                        throw new IOException("'identifier' element may only be specified once whithin a 'organization' element.");
                    }
                    add(identifierElement = new IdentifierElement());
                    identifierElement.parse(child);
                } else if (DOMs.isElement(child, LOM.NS, "title")) {
                    if (titleElement != null) {
                        throw new IOException("'title' element may only be specified once whithin a 'organization' element.");
                    }
                    add(titleElement = new TitleElement());
                    titleElement.parse(child);
                } else if (DOMs.isElement(child, LOM.NS, "catalogentry")) {
                    CatalogEntryElement element = new CatalogEntryElement();
                    add(element);
                    catalogEntryList.add(element);
                    element.parse(child);
                } else if (DOMs.isElement(child, LOM.NS, "language")) {
                    LanguageElement element = new LanguageElement();
                    add(element);
                    languageList.add(element);
                    element.parse(child);
                } else if (DOMs.isElement(child, LOM.NS, "description")) {
                    DescriptionElement element = new DescriptionElement();
                    add(element);
                    descriptionList.add(element);
                    element.parse(child);
                } else if (DOMs.isElement(child, LOM.NS, "keyword")) {
                    KeywordElement element = new KeywordElement();
                    add(element);
                    keywordList.add(element);
                    element.parse(child);
                } else if (DOMs.isElement(child, LOM.NS, "coverage")) {
                    CoverageElement element = new CoverageElement();
                    add(element);
                    coverageList.add(element);
                    element.parse(child);
                } else if (DOMs.isElement(child, LOM.NS, "structure")) {
                    if (structureElement != null) {
                        throw new IOException("'structure' element may only be specified once whithin a 'organization' element.");
                    }
                    add(structureElement = new StructureElement());
                    structureElement.parse(child);
                } else if (DOMs.isElement(child, LOM.NS, "aggregationlevel")) {
                    if (aggregationLevelElement != null) {
                        throw new IOException("'aggregationlevel' element may only be specified once whithin a 'organization' element.");
                    }
                    add(aggregationLevelElement = new AggregationLevelElement());
                    aggregationLevelElement.parse(child);
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
        buf.append("<b>General</b> ");

        buf.append("</font>");
        return buf.toString();
    }
}
