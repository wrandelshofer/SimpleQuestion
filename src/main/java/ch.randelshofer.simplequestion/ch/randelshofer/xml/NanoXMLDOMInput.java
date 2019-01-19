/* @(#)NanoXMLDOMInput.java
 *
 * Copyright (c) 2003-2006 Werner Randelshofer
 * Staldenmattweg 2, Immensee, CH-6405, Switzerland.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Werner Randelshofer. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Werner Randelshofer.
 */

package ch.randelshofer.xml;

import nanoxml.XMLElement;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

/**
 * NanoXMLDOMInput.
 *
 * @author Werner Randelshofer
 * @version 1.0 February 17, 2004 Created.
 */
public class NanoXMLDOMInput implements DOMInput {
    /**
     * This map is used to unmarshall references to objects to
     * the XML DOM. A key in this map is a String representing a marshalled
     * reference. A value in this map is an unmarshalled Object.
     */
    private HashMap<String, Object> idobjects = new HashMap<String, Object>();

    /**
     * The document used for input.
     */
    private XMLElement document;
    /**
     * The current node used for input.
     */
    private XMLElement current;

    /**
     * The factory used to create objects from XML tag names.
     */
    private DOMFactory factory;

    /**
     * The stack.
     */
    private Stack<XMLElement> stack = new Stack<XMLElement>();

    public NanoXMLDOMInput(DOMFactory factory, InputStream in) throws IOException {
        this(factory, new InputStreamReader(in, "UTF8"));
    }

    public NanoXMLDOMInput(DOMFactory factory, Reader in) throws IOException {
        this.factory = factory;
        current = new XMLElement(null, false, false);
        current.parseFromReader(in);
        document = new XMLElement(null, false, false);
        document.addChild(current);
        current = document;
    }

    /**
     * Returns the tag name of the current element.
     */
    public String getTagName() {
        return current.getName();
    }

    /**
     * Gets an attribute of the current element of the DOM Document.
     */
    public String getAttribute(String name, String defaultValue) {
        String value = (String) current.getAttribute(name);
        return (value == null || value.length() == 0) ? defaultValue : value;
    }

    /**
     * Gets the text of the current element of the DOM Document.
     */
    public String getText() {
        return getText(null);
    }

    /**
     * Gets the text of the current element of the DOM Document.
     */
    public String getText(String defaultValue) {
        String value = current.getContent();
        return (value == null) ? defaultValue : value;
    }

    /**
     * Gets an attribute of the current element of the DOM Document.
     */
    public int getAttribute(String name, int defaultValue) {
        String value = (String) current.getAttribute(name);
        return (value == null || value.length() == 0) ? defaultValue : (int) Long.decode(value).intValue();
    }

    /**
     * Gets an attribute of the current element of the DOM Document.
     */
    public double getAttribute(String name, double defaultValue) {
        String value = (String) current.getAttribute(name);
        return (value == null || value.length() == 0) ? defaultValue : Double.parseDouble(value);
    }

    /**
     * Gets an attribute of the current element of the DOM Document.
     */
    public boolean getAttribute(String name, boolean defaultValue) {
        String value = (String) current.getAttribute(name);
        return (value == null || value.length() == 0) ? defaultValue : Boolean.valueOf(value).booleanValue();
    }


    /**
     * Returns the number of child elements of the current element.
     */
    public int getElementCount() {
        return current.countChildren();
    }

    /**
     * Returns the number of child elements with the specified tag name
     * of the current element.
     */
    public int getElementCount(String tagName) {
        int count = 0;
        List<XMLElement> list = current.getChildren();
        for (int i = 0; i < list.size(); i++) {
            XMLElement node = (XMLElement) list.get(i);
            if (node.getName().equals(tagName)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Opens the element with the specified index and makes it the current node.
     */
    public void openElement(int index) {
        stack.push(current);
        List<XMLElement> list = current.getChildren();
        current = (XMLElement) list.get(index);
    }

    /**
     * Opens the last element with the specified name and makes it the current node.
     */
    public void openElement(String tagName) {
        List<XMLElement> list = current.getChildren();
        for (int i = 0; i < list.size(); i++) {
            XMLElement node = (XMLElement) list.get(i);
            if (node.getName().equals(tagName)) {
                stack.push(current);
                current = node;
                return;
            }
        }
        throw new IllegalArgumentException("no such element:" + tagName);
    }

    /**
     * Opens the element with the specified name and index and makes it the
     * current node.
     */
    public void openElement(String tagName, int index) {
        int count = 0;
        List<XMLElement> list = current.getChildren();
        for (int i = 0; i < list.size(); i++) {
            XMLElement node = (XMLElement) list.get(i);
            if (node.getName().equals(tagName)) {
                if (count++ == index) {
                    stack.push(current);
                    current = node;
                    return;
                }
            }
        }
        throw new IllegalArgumentException("no such element:" + tagName + " at index:" + index);
    }

    /**
     * Closes the current element of the DOM Document.
     * The parent of the current element becomes the current element.
     *
     * @throws IllegalArgumentException if the provided tagName does
     *                                  not match the tag name of the element.
     */
    public void closeElement() {
        current = (XMLElement) stack.pop();
    }

    /**
     * Reads an object from the current element.
     */
    public Object readObject() {
        return readObject(0);
    }

    /**
     * Reads an object from the current element.
     */
    public Object readObject(int index) {
        openElement(index);
        Object o;

        String tagName = getTagName();
        if (tagName.equals("null")) {
            o = null;
        } else if (tagName.equals("string")) {
            o = getText();
        } else if (tagName.equals("int")) {
            o = Integer.decode(getText());
        } else if (tagName.equals("long")) {
            o = Long.decode(getText());
        } else if (tagName.equals("float")) {
            o = new Float(Float.parseFloat(getText()));
        } else if (tagName.equals("double")) {
            o = new Double(Double.parseDouble(getText()));
        } else if (tagName.equals("boolean")) {
            o = Boolean.valueOf(getText());
        } else if (tagName.equals("color")) {
            o = new Color(getAttribute("rgba", 0xff));
        } else if (tagName.equals("intArray")) {
            int[] a = new int[getElementCount()];
            for (int i = 0; i < a.length; i++) {
                a[i] = ((Integer) readObject(i)).intValue();
            }
            o = a;
        } else if (tagName.equals("floatArray")) {
            float[] a = new float[getElementCount()];
            for (int i = 0; i < a.length; i++) {
                a[i] = ((Float) readObject(i)).floatValue();
            }
            o = a;
        } else if (tagName.equals("font")) {
            o = new Font(getAttribute("name", "Dialog"), getAttribute("style", 0), getAttribute("size", 0));
        } else {
            String ref = getAttribute("ref", null);
            String id = getAttribute("id", ref);

            if (id == null) {
                throw new IllegalArgumentException(getTagName() + " has neither an 'id' nor a 'ref' attribute: " + current);
            }

            if (idobjects.containsKey(id)) {
                o = idobjects.get(id);
            } else {
                o = factory.create(getTagName());
                idobjects.put(id, o);
            }
            if (ref == null) {
                if (o instanceof DOMStorable) {
                    ((DOMStorable) o).read(this);
                }
            }
        }

        closeElement();
        return o;
    }
}
