/*
 * @(#)DOMStorable.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
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
