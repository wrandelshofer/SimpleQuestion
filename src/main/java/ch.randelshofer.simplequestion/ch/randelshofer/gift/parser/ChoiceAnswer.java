/*
 * @(#)ChoiceAnswer.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.gift.parser;

/**
 * A choice answer has a text and can be correct or incorrect.
 * An AnswerList can contain multiple choice answers.
 * <p>
 * If all ChoiceAnswers in an AnswerList are correct, then it must be
 * presented as a cloze question.
 * <p>
 * If exactly one ChoiceAnswers in an AnswerList is correct, and all others
 * are incorrect, then it must be presented as a single choice question.
 * <p>
 * If all ChoiceAnswers in an AnswerList are incorrect, then it must be
 * presented as a multiple choice question. ChoiceAnswers with a weight greater
 * than 0 must be checked, those with a weight equal or smaller than 0 must
 * not be checked.
 * <p>
 * If more than one ChoiceAnswers in an AnswerList is correct, and at least one
 * incorrect answer is present, then it must be presented as a multiple choice
 * question. ChoiceAnswers which are correct, or which are incorrect and have a
 * weight greater than 0 must be checked. All others must not be checked.
 *
 * @author Werner Randelshofer
 * @version 1.0 24. April 2006 Created.
 */
public class ChoiceAnswer extends Answer {
    private boolean isCorrect;
    private String text;

    /**
     * Creates a new instance.
     */
    public ChoiceAnswer() {
    }

    public void setCorrect(boolean newValue) {
        isCorrect = newValue;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setText(String newValue) {
        text = newValue;
    }

    public String getText() {
        return text;
    }

    /**
     * Returns true, if the other answer is a ChoiceAnswer.
     */
    public boolean canBeInSameList(Answer that) {
        return that instanceof ChoiceAnswer;
    }

    public String toString() {
        return ((isCorrect()) ? '=' : '~') +
                (getWeight() != 0 ? "%" + getWeight() + "%" : "") +
                text;
    }
}
