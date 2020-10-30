/*
 * @(#)BooleanAnswer.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
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

    /**
     * Creates a new instance.
     */
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
