/* @(#)NumberAnswer.java
 * Copyright © Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.gift.parser;

/**
 * A NumeberAnswer consists of a number and an error margin.
 * An AnswerList can contain multiple NumericalAnswers and IntervalAnswers.
 *
 *
 * @author Werner Randelshofer
 * @version 1.0 24. April 2006 Created.
 */
public class NumberAnswer extends Answer {
    private double number;
    private double errorMargin;
    
    /** Creates a new instance. */
    public NumberAnswer() {
    }
    
    public void setNumber(double newValue) {
        number = newValue;
    }
    
    public double getNumber() {
        return number;
    }
    public double getErrorMargin() {
        return errorMargin;
    }
    
    public String getNumberAsString() {
        String str = Double.toString(number);
        return  (str.endsWith(".0")) ?
            str.substring(0, str.length() - 2) :
            str;
    }
    
    public void setErrorMargin(double newValue) {
        errorMargin = newValue;
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
        return  (getWeight() != 0 ? "%"+getWeight()+"%" : "") +
                number+":"+errorMargin;
    }
}
