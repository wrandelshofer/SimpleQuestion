/*
 * @(#)MatchingPairAnswer.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.gift.parser;

/**
 * A MatchingPair answer consists of a key and a value.
 * An AnswerList can contain multiple matching pairs.
 *
 * @author Werner Randelshofer
 * @version 1.0 24. April 2006 Created.
 */
public class MatchingPairAnswer extends Answer {
    private String key;
    private String value;

    /**
     * Creates a new instance.
     */
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
        return (getWeight() != 0 ? "%" + getWeight() + "%" : "") +
                key + "->" + value;
    }
}
