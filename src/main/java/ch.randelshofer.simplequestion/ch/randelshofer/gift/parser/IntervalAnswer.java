/*
 * @(#)IntervalAnswer.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.gift.parser;

/**
 * An IntervalAnswer consists of a minimal and a maximal number.
 * An AnswerList can contain multiple NumericalAnswers and IntervalAnswers.
 *
 * @author Werner Randelshofer
 * @version 1.0 24. April 2006 Created.
 */
public class IntervalAnswer extends Answer {
    private double min;
    private double max;

    /**
     * Creates a new instance.
     */
    public IntervalAnswer() {
    }

    public void setMin(double newValue) {
        min = newValue;
    }

    public void setMax(double newValue) {
        max = newValue;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public String getMinAsString() {
        return getNumberAsString(min);
    }

    public String getMaxAsString() {
        return getNumberAsString(max);
    }

    private String getNumberAsString(double number) {
        String str = Double.toString(number);
        return (str.endsWith(".0")) ?
                str.substring(0, str.length() - 2) :
                str;
    }

    /**
     * Returns true, if the other answer is a NumberAnswer or
     * an IntervalAnswer.
     */
    public boolean canBeInSameList(Answer that) {
        return (that instanceof NumberAnswer) ||
                (that instanceof IntervalAnswer);
    }

    public String toString() {
        return (getWeight() != 0 ? "%" + getWeight() + "%" : "") +
                min + ".." + max;
    }
}
