/*
 * @(#)DOMFactory.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.xml;

/**
 * DOMFactory.
 *
 * @author Werner Randelshofer
 * @version 1.0 February 17, 2004 Create..
 */
public interface DOMFactory {
    /**
     * Returns the tag name for the specified object.
     * Note: The tag names "string", "int", "float", "long", "double", "boolean",
     * "null" are reserved and must not be returned by this operation.
     */
    public String getTagName(DOMStorable o);

    public Object create(String tagName);
}