/* @(#)ExternalAnswer.java
 *
 * Copyright (c) 2006-2008 Werner Randelshofer
 * Staldenmattweg 2, CH-6405 Immensee, Switzerland
 * All rights reserved.
 *
 * The copyright of this software is owned by Werner Randelshofer. 
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * Werner Randelshofer. For details see accompanying license terms.
 */

package ch.randelshofer.gift.parser;

/**
 * ExternalAnswer.
 *
 * @author Werner Randelshofer
 * @version 1.0 28. November 2006 Created.
 */
public class ExternalAnswer extends Answer {
    private String externalReference;
    
    /** Creates a new instance. */
    public ExternalAnswer() {
    }

    public boolean canBeInSameList(Answer that) {
        return false;
    }
    
    public String toString() {
        return getExternalReference().toString();
    }

    public String getExternalReference() {
        return externalReference;
    }

    public void setExternalReference(String newValue) {
        this.externalReference = newValue;
    }
}
