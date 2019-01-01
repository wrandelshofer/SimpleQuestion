/*
 * @(#)NumberAnswer.java  1.0  24. April 2006
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
