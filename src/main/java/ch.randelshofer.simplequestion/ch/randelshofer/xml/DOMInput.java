/*
 * @(#)DOMInput.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */


package ch.randelshofer.xml;

/**
 * DOMInput.
 *
 * @author Werner Randelshofer
 * @version 1.0 10. März 2004  Created.
 */
public interface DOMInput {

    /**
     * Returns the tag name of the current element.
     */
    public String getTagName();

    /**
     * Gets an attribute of the current element of the DOM Document.
     */
    public String getAttribute(String name, String defaultValue);

    /**
     * Gets the text of the current element of the DOM Document.
     */
    public String getText();

    /**
     * Gets the text of the current element of the DOM Document.
     */
    public String getText(String defaultValue);

    /**
     * Gets an attribute of the current element of the DOM Document.
     */
    public int getAttribute(String name, int defaultValue);

    /**
     * Gets an attribute of the current element of the DOM Document.
     */
    public double getAttribute(String name, double defaultValue);

    /**
     * Gets an attribute of the current element of the DOM Document.
     */
    public boolean getAttribute(String name, boolean defaultValue);


    /**
     * Returns the number of child elements of the current element.
     */
    public int getElementCount();

    /**
     * Returns the number of child elements with the specified tag name
     * of the current element.
     */
    public int getElementCount(String tagName);

    /**
     * Opens the element with the specified index and makes it the current node.
     */
    public void openElement(int index);

    /**
     * Opens the last element with the specified name and makes it the current node.
     */
    public void openElement(String tagName);

    /**
     * Opens the element with the specified name and index and makes it the
     * current node.
     */
    public void openElement(String tagName, int index);

    /**
     * Closes the current element of the DOM Document.
     * The parent of the current element becomes the current element.
     *
     * @throws IllegalArgumentException if the provided tagName does
     *                                  not match the tag name of the element.
     */
    public void closeElement();

    /**
     * Reads an object from the current element.
     */
    public Object readObject();

    /**
     * Reads an object from the current element.
     */
    public Object readObject(int index);
}
