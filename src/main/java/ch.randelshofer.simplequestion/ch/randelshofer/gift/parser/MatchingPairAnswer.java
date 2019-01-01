/*
 * @(#)Answer.java  1.0  24. April 2006
 *
 * Copyright (c) 2008 Werner Randelshofer
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
 * A MatchingPair answer consists of a key and a value.
 * An AnswerList can contain multiple matching pairs.
 * 
 * 
 * @author Werner Randelshofer
 * @version 1.0 24. April 2006 Created.
 */
public class MatchingPairAnswer extends Answer {
    private String key;
    private String value;
    
    /** Creates a new instance. */
    public MatchingPairAnswer() {
    }
    
    public void setKey(String newValue) {
        key = newValue;
    }
    public void setValue(String newValue) {
        value = newValue;
    }
    
    public String getKey() {
        return key;
    }
    
    public String getValue() {
        return value;
    }

    /**
     * Returns true, if the other answer is a MatchingPairAnswer.
     */
    public boolean canBeInSameList(Answer that) {
        return that instanceof MatchingPairAnswer;
    }
    
    public String toString() {
        return (getWeight() != 0 ? "%"+getWeight()+"%" : "") +
            key+"->"+value;
    }
}
