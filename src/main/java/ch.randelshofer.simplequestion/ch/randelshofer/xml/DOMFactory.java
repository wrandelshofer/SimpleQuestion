/* @(#)DOMFactory.java
 *
 * Copyright (c) 2004 Werner Randelshofer
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

/**
 * DOMFactory.
 *
 * @author  Werner Randelshofer
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