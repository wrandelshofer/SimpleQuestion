/*
 * @(#)EducationalElement.java  1.1  2006-10-11
 *
 * Copyright (c) 2001 Werner Randelshofer
 * Staldenmattweg 2, Immensee, CH-6405, Switzerland.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Werner Randelshofer. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Werner Randelshofer.
 */


package ch.randelshofer.scorm.lom;

import ch.randelshofer.scorm.AbstractElement;
import ch.randelshofer.util.*;
import ch.randelshofer.xml.*;
import java.io.*;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.w3c.dom.*;
/**
 * Represents a SCORM 1.2 LOM 'educational' Element.
 * <p>
 * This data element describes conditions of use of the resource.
 * <p>
 * An 'educational' element has a structure as shown below.
 * Square brackets [ ] denote zero or one occurences.
 * Braces { } denote zero or more occurences.
 * Text in <b>bold</b> denotes non-terminal symbols.
 * <pre>
 * <b>educational</b> ::= &lt;educational&gt;
 *                 [<b>interactivitytype</b>]
 *                 {<b>learningresourcetype</b>}
 *                 [<b>interactivitylevel</b>]
 *                 [<b>semanticdensity</b>]
 *                 {<b>intendedenduserrole</b>}
 *                 {<b>context</b>}
 *                 {<b>typicalageragen</b>}
 *                 [<b>difficulty</b>]
 *                 [<b>typicallearningtime</b>]
 *                 [<b>description</b>]
 *                 {<b>language</b>}
 *                 &lt;/educational&gt;
 * </pre>
 * Reference:
 * ADL (2001). Advanced Distributed Learning.
 * Sharable Content Object Reference Model (SCORM(TM)) Version 1.2.
 * The SCORM Content Aggregation Model. October 1, 2001.
 * Internet (2003-01-20): http://www.adlnet.org
 *
 * @author  Werner Randelshofer
 * @version 1.1 2006-10-11 Parse using XML namespaces.
 * <br>1.0.1 2004-01-19 Comments updated.
 * <br>1.0 5. Januar 2004  Created.
 */
public class EducationalElement extends AbstractElement {
    private final static long serialVersionUID=1L;
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
    
    /** Creates a new instance. */
    public EducationalElement() {
    }
    
    /**
     * Parses the specified DOM Element and incorporates its contents into this element.
     * @param elem An XML element with the tag name 'file'.
     */
    public void parse(Element elem)
    throws IOException, ParserConfigurationException, SAXException {
        if (! DOMs.isElement(elem, LOM.NS, "educational")) {
            throw new IOException("'educational' element expected, but found '"+elem.getTagName()+"' element.");
        }
    }

    public void dump(StringBuffer buf, int depth) {
    }
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("<html><font size=-1 face=SansSerif>");
        if (! isValid()) buf.append("<font color=red>* </font>");
        buf.append("<b>Educational</b> ");
        
        buf.append("</font>");
        return buf.toString();
    }
}
