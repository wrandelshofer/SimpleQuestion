/*
 * @(#)DOMOutput.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */


package ch.randelshofer.xml;

/**
 * DOMOutput.
 *
 * @author Werner Randelshofer
 * @version 1.0 10. März 2004  Created.
 */
public interface DOMOutput {

    /**
     * Adds a new element to the DOM Document.
     * The new element is added as a child to the current element in the DOM
     * document. Then it becomes the current element.
     * The element must be closed using closeElement.
     */
    public void addElement(String tagName);

    /**
     * Closes the current element of the DOM Document.
     * The parent of the current element becomes the current element.
     *
     * @throws IllegalArgumentException if the provided tagName does
     *                                  not match the tag name of the element.
     */
    public void closeElement();

    /**
     * Adds a comment to the current element of the DOM Document.
     */
    public void addComment(String comment);

    /**
     * Adds a text to current element of the DOM Document.
     * Note: Multiple consecutives texts will be merged.
     */
    public void addText(String text);

    /**
     * Adds an attribute to current element of the DOM Document.
     */
    public void addAttribute(String name, String value);

    /**
     * Adds an attribute to current element of the DOM Document if it is
     * different from the default value.
     */
    public void addAttribute(String name, String value, String defaultValue);

    /**
     * Adds an attribute to current element of the DOM Document.
     */
    public void addAttribute(String name, int value);

    /**
     * Adds an attribute to current element of the DOM Document if it is
     * different from the default value.
     */
    public void addAttribute(String name, int value, int defaultValue);

    /**
     * Adds an attribute to current element of the DOM Document.
     */
    public void addAttribute(String name, boolean value);

    /**
     * Adds an attribute to current element of the DOM Document if it is
     * different from the default value.
     */
    public void addAttribute(String name, boolean value, boolean defaultValue);

    /**
     * Adds an attribute to current element of the DOM Document.
     */
    public void addAttribute(String name, float value);

    /**
     * Adds an attribute to current element of the DOM Document if it is
     * different from the default value.
     */
    public void addAttribute(String name, float value, float defaultValue);

    /**
     * Adds an attribute to current element of the DOM Document.
     */
    public void addAttribute(String name, double value);

    /**
     * Adds an attribute to current element of the DOM Document if it is
     * different from the default value.
     */
    public void addAttribute(String name, double value, double defaultValue);

    /**
     * Writes an object.
     */
    public void writeObject(Object o);
}
