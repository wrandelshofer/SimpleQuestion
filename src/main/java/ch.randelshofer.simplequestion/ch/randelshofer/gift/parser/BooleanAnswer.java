/*
 * @(#)Answer.java  1.0  24. April 2006
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
 * A boolean answer can either be TRUE or FALSE.
 * An answer list can contain only one boolean answer.
 * 
 * @author Werner Randelshofer
 * @version 1.0 24. April 2006 Created.
 */
public class BooleanAnswer extends Answer {
    private boolean isTrue;
    
    /** Creates a new instance. */
    public BooleanAnswer() {
    }
    
    public BooleanAnswer(boolean isTrue) {
        setTrue(isTrue);
    }
    
    public void setTrue(boolean newValue) {
        isTrue = newValue;
    }
    public boolean isTrue() {
        return isTrue;
    }
    /**
     * Always returns false, because there can be only one boolean answer
     * in an answer group.
     */
    public boolean canBeInSameList(Answer that) {
        return false;
    }
    
    public String toString() {
        return (isTrue) ? "TRUE" : "FALSE";
    }
}
