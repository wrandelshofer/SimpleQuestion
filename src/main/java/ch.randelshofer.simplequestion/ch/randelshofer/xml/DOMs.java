/* @(#)DOMs.java
 *
 * Copyright (c) 2003 Werner Randelshofer
 * Staldenmattweg 2, Immensee, CH-6405, Switzerland.
 * http://www.randelshofer.ch
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Werner Randelshofer. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Werner Randelshofer.
 */

package ch.randelshofer.xml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.LinkedList;

/**
 * A utility class for XML Document Object Model's (DOM).
 *
 * @author Werner Randelshofer
 * @version 1.1 2006-10-12 Added support for XML namespaces. 
 * <br>1.0.1 2003-11-03 Method getElement handles namespace prefix. 
 * <br>1.0 2003-09-23  Created.
 */
public class DOMs {
    
    /** Hide constructor to prevent instance creation. */
    private DOMs() {
    }
    
    /**
     * Returns the last child <code>Element</code> with a given name
     * of the specified <code>Element</code>.
     *
     * @param elem The parent <code>Element</code> of the child or null.
     * @param name The name of the tag to match on.
     * @return The last matching <code>Element</code> node or null if the element
     * has no child with the specified name or if the parameter <code>elem</code> is null.
     */
    public static Element getElement(Element elem, String name) {
        if (elem != null) {
            NodeList list = elem.getChildNodes();
            for (int i=list.getLength() - 1; i >= 0; i--) {
                Node node = list.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE
                && node.getNodeName().equals(name)) {
                    return (Element) node;
                }
            }
            
            // Search without namespace prefix
            int p = name.indexOf(':');
            if (p != -1) {
                return getElement(elem, name.substring(p + 1));
            }
        }
        return null;
    }
    /**
     * Returns the last child <code>Element</code> with a given namespaceURI and
     * name of the specified <code>Element</code>.
     *
     * @param elem The parent <code>Element</code> of the child or null.
     * @param namespaceURI The namespace URI.
     * @param name The name of the tag to match on.
     * @return The last matching <code>Element</code> node or null if the element
     * has no child with the specified name or if the parameter <code>elem</code> is null.
     */
    public static Element getElement(Element elem, String namespaceURI, String name) {
        if (elem != null) {
            NodeList list = elem.getChildNodes();
            for (int i=list.getLength() - 1; i >= 0; i--) {
                Node node = list.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE
                && node.getLocalName().equals(name) && node.getNamespaceURI().equals(namespaceURI)) {
                    return (Element) node;
                }
            }
            
            // Search without namespace prefix
            int p = name.indexOf(':');
            if (p != -1) {
                return getElement(elem, name.substring(p + 1));
            }
        }
        return getElement(elem, name);
    }
    
    /**
     * Returns true if the element is in the given namespaceURI and has the
     * specified name.
     *
     * @param elem The parent <code>Element</code> of the child or null.
     * @param namespaceURI The namespace URI.
     * @param name The name of the tag to match on.
     * @return True if namespace and name match.
     */
    public static boolean isElement(Element elem, String namespaceURI, String name) {
        if (elem.getNamespaceURI().equals(namespaceURI) && elem.getLocalName().equals(name)) {
            return true;
        }
        return false;
    }
    
    /**
     * Returns all children <code>Element</code>'s with a given name
     * of the specified <code>Element</code>.
     *
     * @param elem The parent <code>Element</code> of the child or null.
     * @param name The name of the tag to match on.
     * @return The matching <code>Element</code> nodes or an empty array
     * if the element has no children with the specified name or if the parameter <code>elem</code> is null..
     */
    public static Element[] getElements(Element elem, String name) {
        LinkedList children = new LinkedList();
        if (elem != null) {
            NodeList list = elem.getChildNodes();
            for (int i=0; i < list.getLength(); i++) {
                Node node = list.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE
                && node.getNodeName().equals(name)) {
                    children.add(node);
                }
            };
        }
        return (Element[]) children.toArray(new Element[children.size()]);
    }
    
    /**
     * Returns an attribute value with a given name of the specified <code>Element</code>.
     *
     * @param The <code>Element</code> containing the attribute or null.
     * @param name The name of the attribute.
     * @param defaultValue The default value of the attribute.
     * @return The value of the attribute or the default value if the element
     * has not an attribute with the specified name or if the parameter <code>elem</code> is null.
     */
    public static String getAttribute(Element elem, String name, String defaultValue) {
        if (elem == null) return defaultValue;
        String value = elem.getAttribute(name);
        return (value.equals("")) ? defaultValue : value;
    }
    /**
     * Returns an attribute value with a given namespace and name name of the 
     * specified <code>Element</code>.
     *
     * @param The <code>Element</code> containing the attribute or null.
     * @param namespaceURI The namespace URI.
     * @param name The name of the attribute.
     * @param defaultValue The default value of the attribute.
     * @return The value of the attribute or the default value if the element
     * has not an attribute with the specified name or if the parameter <code>elem</code> is null.
     */
    public static String getAttributeNS(Element elem, String namespaceURI, String name, String defaultValue) {
        if (elem == null) return defaultValue;
        String value = elem.getAttributeNS(namespaceURI, name);
    return (value.equals("")) ? getAttribute(elem, name, defaultValue) : value;
    }
    
    /**
     * Returns the attribute with a given name from the
     * last child <code>Element</code> with a given name
     * of the specified <code>Element</code>.
     *
     * @param The parent <code>Element</code> of the child or null.
     * @param elementName The name of the element tag to match on.
     * @param attributeName The name of the attribute to match on.
     * @param defaultValue The default value of the attribte.
     * @return The value of the attribute or the default value, if the
     * element or the attribute does not exist or if elem is null.
     */
    public static String getElementAttribute(Element elem, String elementName, String attributeName, String defaultValue) {
        String value = defaultValue;
        if (elem != null) {
            Element child = getElement(elem, elementName);
            if (child != null) {
                value = getAttribute(child, attributeName, defaultValue);
            }
        }
        return value;
    }
    /**
     * Returns the text of the specified <code>Element</code> or null.
     *
     * @param The <code>Element</code> containing the attribute.
     * @param name The name of the attribute.
     * @return The value of the attribute or null if the element
     * has not an attribute with the specified name.
     */
    public static String getText(Element n) {
        return getText(n, (String) null);
    }
    /**
     * Returns the text from the
     * last child <code>Element</code> with a given name
     * of the specified <code>Element</code>.
     *
     * @param The parent <code>Element</code> of the child or null.
     * @param elementName The name of the element tag to match on.
     * @param defaultText The default text.
     * @return The text of the element or the default text, if the
     * element with the givenname does not exist or if elem is null.
     */
    public static String getElementText(Element elem, String elementName, String defaultText) {
        String value = defaultText;
        if (elem != null) {
            Element child = getElement(elem, elementName);
            if (child != null) {
                value = getText(child, defaultText);
            }
        }
        return value;
    }
    /**
     * Returns the text of the specified <code>Element</code>.
     *
     * @param The <code>Element</code> containing the attribute.
     * @param name The name of the attribute.
     * @param defaultValue The default value of the attribute.
     * @return The value of the attribute or the default value if the element
     * has not an attribute with the specified name.
     */
    public static String getText(Element n, String defaultValue) {
        if (n == null) return defaultValue;
        StringBuffer buf = new StringBuffer();
        getText(n, buf);
        return buf.toString();
    }
    private static void getText(Node n, StringBuffer buf) {
        if (n.getNodeValue() != null) buf.append(n.getNodeValue());
        NodeList children = n.getChildNodes();
        for (int i=0; i < children.getLength(); i++) {
            getText(children.item(i), buf);
        }
    }
}
