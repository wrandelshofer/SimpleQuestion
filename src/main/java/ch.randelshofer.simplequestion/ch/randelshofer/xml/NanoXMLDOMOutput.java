/* @(#)NanoXMLDOMOutput.java
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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Stack;

/**
 * DOMOutput using Nano XML.
 *
 * @author Werner Randelshofer
 * @version 1.2 2006-03-20 Added support for default values.
 * <br>1.1 2006-01-18 Remove ".0" at the end of float and double numbers.
 * <br>1.0 February 17, 2004 Created.
 */
public class NanoXMLDOMOutput implements DOMOutput {


    /**
     * This map is used to marshall references to objects to
     * the XML DOM. A key in this map is a Java Object, a value in this map
     * is String representing a marshalled reference to that object.
     */
    private HashMap<Object, String> objectids;

    /**
     * The document used for output.
     */
    private XMLElement document;
    /**
     * The current node used for output.
     */
    private XMLElement current;
    /**
     * The factory used to create objects.
     */
    private DOMFactory factory;
    /**
     * The stack.
     */
    private Stack<XMLElement> stack;

    /**
     * Creates a new instance.
     */
    public NanoXMLDOMOutput(DOMFactory factory) {
        this.factory = factory;
        objectids = new HashMap<Object, String>();
        document = new XMLElement(null, false, false);
        current = document;
        stack = new Stack<XMLElement>();
        stack.push(current);
    }

    /**
     * Writes the contents of the DOMOutput into the specified output stream.
     */
    public void save(OutputStream out) throws IOException {
        Writer w = new OutputStreamWriter(out, "UTF8");
        save(w);
        w.flush();
    }

    /**
     * Writes the contents of the DOMOutput into the specified output stream.
     */
    public void save(Writer out) throws IOException {
        document.getChildren().get(0).write(out);
    }

    /**
     * Puts a new element into the DOM Document.
     * The new element is added as a child to the current element in the DOM
     * document. Then it becomes the current element.
     * The element must be closed using closeElement.
     */
    public void addElement(String tagName) {
        XMLElement newElement = new XMLElement(null, false, false);
        newElement.setName(tagName);
        current.addChild(newElement);
        stack.push(current);
        current = newElement;
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
     * Adds a comment to the current element of the DOM Document.
     */
    public void addComment(String comment) {
        // NanoXML does not support comments
    }

    /**
     * Adds a text to current element of the DOM Document.
     * Note: Multiple consecutives texts will be merged.
     */
    public void addText(String text) {
        String old = current.getContent();
        if (old == null) {
            current.setContent(text);
        } else {
            current.setContent(old + text);
        }
    }

    /**
     * Adds an attribute to current element of the DOM Document.
     */
    public void addAttribute(String name, String value) {
        if (value != null) {
            current.setAttribute(name, value);
        }
    }

    /**
     * Adds an attribute to current element of the DOM Document.
     */
    public void addAttribute(String name, int value) {
        current.setAttribute(name, Integer.toString(value));
    }

    /**
     * Adds an attribute to current element of the DOM Document.
     */
    public void addAttribute(String name, boolean value) {
        current.setAttribute(name, new Boolean(value).toString());
    }

    /**
     * Adds an attribute to current element of the DOM Document.
     */
    public void addAttribute(String name, float value) {
        // Remove the awkard .0 at the end of each number
        String str = Float.toString(value);
        if (str.endsWith(".0")) {
            str = str.substring(0, str.length() - 2);
        }
        current.setAttribute(name, str);
    }

    /**
     * Adds an attribute to current element of the DOM Document.
     */
    public void addAttribute(String name, double value) {
        // Remove the awkard .0 at the end of each number
        String str = Double.toString(value);
        if (str.endsWith(".0")) {
            str = str.substring(0, str.length() - 2);
        }
        current.setAttribute(name, str);
    }

    public void writeObject(Object o) {
        if (o == null) {
            addElement("null");
            closeElement();
        } else if (o instanceof DOMStorable) {
            writeStorable((DOMStorable) o);
        } else if (o instanceof String) {
            addElement("string");
            addText((String) o);
            closeElement();
        } else if (o instanceof Integer) {
            addElement("int");
            addText(o.toString());
            closeElement();
        } else if (o instanceof Long) {
            addElement("long");
            addText(o.toString());
            closeElement();
        } else if (o instanceof Double) {
            addElement("double");
            // Remove the awkard .0 at the end of each number
            String str = o.toString();
            if (str.endsWith(".0")) {
                str = str.substring(0, str.length() - 2);
            }
            addText(str);
            closeElement();
        } else if (o instanceof Float) {
            addElement("float");
            // Remove the awkard .0 at the end of each number
            String str = o.toString();
            if (str.endsWith(".0")) {
                str = str.substring(0, str.length() - 2);
            }
            addText(str);
            closeElement();
        } else if (o instanceof Boolean) {
            addElement("boolean");
            addText(o.toString());
            closeElement();
        } else if (o instanceof Color) {
            Color c = (Color) o;
            addElement("color");
            addAttribute("rgba", "#" + Integer.toHexString(c.getRGB()));
            closeElement();
        } else if (o instanceof int[]) {
            addElement("intArray");
            int[] a = (int[]) o;
            for (int i = 0; i < a.length; i++) {
                writeObject(new Integer(a[i]));
            }
            closeElement();
        } else if (o instanceof float[]) {
            addElement("floatArray");
            float[] a = (float[]) o;
            for (int i = 0; i < a.length; i++) {
                writeObject(new Float(a[i]));
            }
            closeElement();
        } else if (o instanceof Font) {
            Font f = (Font) o;
            addElement("font");
            addAttribute("name", f.getName());
            addAttribute("style", f.getStyle());
            addAttribute("size", f.getSize());
            closeElement();
        } else {
            throw new IllegalArgumentException("unable to store: " + o + " " + o.getClass());
        }
    }

    private XMLElement writeStorable(DOMStorable o) {
        String tagName = factory.getTagName(o);
        if (tagName == null) {
            throw new IllegalArgumentException("no tag name for:" + o);
        }
        addElement(tagName);
        XMLElement element = current;
        if (objectids.containsKey(o)) {
            addAttribute("ref", (String) objectids.get(o));
        } else {
            String id = Integer.toString(objectids.size(), 16);
            objectids.put(o, id);
            addAttribute("id", id);
            o.write(this);
        }
        closeElement();
        return element;
    }

    public void addAttribute(String name, float value, float defaultValue) {
        if (value != defaultValue) {
            addAttribute(name, value);
        }
    }

    public void addAttribute(String name, int value, int defaultValue) {
        if (value != defaultValue) {
            addAttribute(name, value);
        }
    }

    public void addAttribute(String name, double value, double defaultValue) {
        if (value != defaultValue) {
            addAttribute(name, value);
        }
    }

    public void addAttribute(String name, boolean value, boolean defaultValue) {
        if (value != defaultValue) {
            addAttribute(name, value);
        }
    }

    public void addAttribute(String name, String value, String defaultValue) {
        if (value != null && !value.equals(defaultValue)) {
            addAttribute(name, value);
        }
    }
}
