/*
 * @(#)Answer.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.gift.parser;

/**
 * An Answer represents a single entry in an AnswerList.
 *
 * @author Werner Randelshofer
 * @version 1.0 24. April 2006 Created.
 */
public abstract class Answer {
    /**
     * Percentage weight. -100..100. The value 0 is used when the weight
     * is not specified for the answer.
     */
    private int weight;

    /**
     * Feedback text.
     */
    private String feedbackText;

    /**
     * Creates a new instance.
     */
    public Answer() {
    }

    /**
     * Sets the weight of the answer. This must be a value between -100 to +100.
     * Specify 0 to clear the weight.
     */
    public void setWeight(int newValue) {
        weight = newValue;
    }

    /**
     * Returns the weight of the answer or 0 if not specified.
     */
    public int getWeight() {
        return weight;
    }

    public void setFeedbackText(String newValue) {
        feedbackText = newValue;
    }

    public String getFeedbackText() {
        return feedbackText;
    }

    /**
     * Returns true, if this Answer can be in the same AnswerList as
     * the specified Answer.
     */
    public abstract boolean canBeInSameList(Answer that);
}
