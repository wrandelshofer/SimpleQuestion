/*
 * @(#)ResourcesElement.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.scorm.cam;

import ch.randelshofer.scorm.AbstractElement;
import ch.randelshofer.util.IdentifierGenerator;
import ch.randelshofer.xml.DOMs;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.tree.TreeNode;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Represents a SCORM CAM 'resources' element.
 * <p>
 * Describes a collection of references to resources.
 * There is no assumption of order or hierarchy.
 * <p>
 * A 'resources' element has a structure as shown below.
 * Square brackets [ ] denote zero or one occurences.
 * Braces { } denote zero or more occurences.
 * <pre>
 * &lt;resources [xml:base="string"]&gt;
 * {&lt;resource&gt;}
 * &lt;/resources&gt;
 * </pre>
 * <p>
 * Reference:
 * ADL (2001c). Advanced Distributed Learning.
 * Sharable Content Object Reference Model (SCORM(TM)) Version 1.2.
 * The SCORM Content Aggregation Model. October 1, 2001.
 * Internet (2003-01-20): http://www.adlnet.org
 *
 * @author Werner Randelshofer, Staldenmattweg 2, Immensee, CH-6405, Switzerland
 * @version 1.2 2006-10-10 Parse with XML namespaces.
 * <br>1.1 2006-10-07 Removed HTML output from method toString, due to
 * bug http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4988885
 * <br>1.0 2003-10-29 Method findResource added. HTML output in method
 * toString changed.
 * <br>0.17 2003-03-16 Naming conventions streamlined with JavaScript code
 * of the LMS.
 * <br>0.1 2003-02-02 Created.
 */
public class ResourcesElement extends AbstractElement {
    private final static long serialVersionUID = 1L;
    private String info;
    /**
     * This variable is set to true or false by validate, when all
     * files in the content package are referenced in the imsmanifest.xml
     * document.
     * The following files need not to be referenced: imsmanifest.xml,
     * adl_cp_rootv1p1.xsd, ims_cp_rotv1p1.xsd, ims_md_rootv1p1.xsd,
     * ims_xml.xsd.
     */
    private boolean areAllFilesInContentPackageReferenced;

    /**
     * If areAllFilesInContentPackageReferenced is true, then this variable holds the
     * names of the unreferenced files.
     * If areAllFilesInContentPackageReferenced is false, this variable is null.
     */
    private List<String> unreferencedFileNames;

    /**
     * xml:base (optional) - This provides a relative path offset for the content
     * file(s). The usage of this element is defined in the XML Base Working
     * Draft from W3C.
     */
    private String xmlBase;

    private LinkedList<ResourceElement> resourceList = new LinkedList<>();

    /**
     * Creates a new instance of ResourcesElement
     */
    public ResourcesElement() {
    }

    /**
     * Parses the specified DOM Element and incorporates its contents into this element.
     *
     * @param elem An XML element with the tag name 'resources'.
     */
    public void parse(Element elem)
            throws IOException, ParserConfigurationException, SAXException {
        if (!DOMs.isElement(elem, CAM.IMSCP_NS, "resources")) {
            throw new IOException("'imscp:resources' element expected, but found '" + elem.getTagName() + "' element.");
        }
        this.xmlBase = DOMs.getAttributeNS(elem, "xml", "base", null);

        // Read the child elements
        NodeList nodes = elem.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            if (nodes.item(i) instanceof Element) {
                Element child = (Element) nodes.item(i);
                if (DOMs.isElement(child, CAM.IMSCP_NS, "resource")) {
                    ResourceElement resourceElement = new ResourceElement();
                    add(resourceElement);
                    this.resourceList.add(resourceElement);
                    resourceElement.parse(child);
                }
            }
        }
    }

    /**
     * Dumps the contents of this subtree into the provided string buffer.
     */
    public void dump(StringBuffer buf, int depth) {
        for (int i = 0; i < depth; i++) {
            buf.append('.');
        }
        buf.append("<resources xml:base=\"" + xmlBase + "\">\n");
        for (ResourceElement resourceElement : resourceList) {
            resourceElement.dump(buf, depth + 1);
        }
        for (int i = 0; i < depth; i++) {
            buf.append('.');
        }
        buf.append("</resouces>\n");
    }

    /**
     * Exports this CAM subtree to JavaScript using the specified PrintWriter.
     *
     * @param out   The output stream.
     * @param depth The current depth of the tree (used for indention).
     * @param gen   The identifier generator used to generate short(er) identifiers
     *              whithin the JavaScript.
     */
    public void exportToJavaScript(PrintWriter out, int depth, IdentifierGenerator gen)
            throws IOException {
        indent(out, depth);
        out.println("new ResourcesElement([");

        // FIXME - We export too much here. We only have to export the resource
        //         elements referenced by the items of the default organization.

        boolean isFirst = true;
        for (ResourceElement resource : resourceList) {
            if (resource.getHRef() != null) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    out.println(",");
                }
                resource.exportToJavaScript(out, depth + 1, gen);
            }
        }
        out.println();
        indent(out, depth);
        out.print((depth == 0) ? "]);" : "])");
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("<html><font size=-1 face=SansSerif>");
        if (!isValid()) {
            buf.append("<font color=red>* </font>");
        }
        buf.append("<b>Resources</b>");
        if (!areAllFilesInContentPackageReferenced) {
            buf.append("<font color=blue><b> ").append(labels.getFormatted(
                    "cam.unusedFilesInContentPackage",
                    unreferencedFileNames.size())).append("</b></font>");
        }
        buf.append("</font>");
        return buf.toString();
    }

    /**
     * Validates this CAM element.
     *
     * @return Returns true if this elements is valid.
     */
    public boolean validate() {
        isValid = super.validate();
        info = null;

        Set<String> fileNames = new HashSet<>(getIMSManifestDocument().getFileNames());
        fileNames.remove("imscp_rootv1p1p2.xsd");
        fileNames.remove("ims_cp_rootv1p1.xsd");
        fileNames.remove("imsmd_rootv1p2p1.xsd");
        fileNames.remove("ims_md_rootv1p1.xsd");
        fileNames.remove("ims_xml.xsd");
        fileNames.remove("imsmanifest.xml");
        fileNames.remove("adl_cp_rootv1p1.xsd");
        fileNames.remove("adlcp_rootv1p2.xsd");
        fileNames.remove("adl_cp_rootv1p2.xsd");
        fileNames.remove("tinylms.xml");

        Enumeration<TreeNode> enm = getIMSManifestDocument().preorderEnumeration();
        while (enm.hasMoreElements()) {
            AbstractElement element = (AbstractElement) enm.nextElement();
            element.consumeFileNames(fileNames);
        }
        LinkedList<String> list = new LinkedList<>(fileNames);
        if (list.size() > 0) {
            Collections.sort(list);
            StringBuffer buf = new StringBuffer(
                    labels.getString("cam.warning") + ": " +
                            labels.getString("cam.manifestNeedsResourceElement") + "\n\n");
            buf.append("<resource identifier=\"...id...\" adlcp:scormtype=\"asset\" type=\"webcontent\">\n");

            for (String s : list) {
                buf.append("  <file href=\"").append(s).append("\"/>\n");
            }
            buf.append("</resource>\n");
            info = buf.toString();
            isValid = areAllFilesInContentPackageReferenced = false;
            this.unreferencedFileNames = list;
        } else {
            areAllFilesInContentPackageReferenced = true;
            unreferencedFileNames = null;
        }
        return isValid;
    }

    public ResourceElement findResource(String identifierref) {
        return (ResourceElement) findChildByIdentifier(identifierref);
    }

    public String getInfo() {
        return (info == null)
                ? super.getInfo()
                : info
                ;
    }
}
