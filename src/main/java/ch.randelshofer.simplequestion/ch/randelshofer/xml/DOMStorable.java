/* @(#)DOMStorable.java
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

/**
 * DOMStorable.
 *
 * @author Werner Randelshofer
 * @version 1.0 February 17, 2004 Create..
 */
public interface DOMStorable {
    public void write(DOMOutput out);

    public void read(DOMInput in);
}
