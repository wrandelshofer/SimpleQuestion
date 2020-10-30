/*
 * @(#)AnswerList.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.gift.parser;

import java.util.LinkedList;

/**
 * Holds a list of answers.
 *
 * @author Werner Randelshofer
 * @version 1.1 2008-02-19 Added numeric type.
 * <br>1.0 28. April 2006 Created.
 */
public class AnswerList {
    private LinkedList<Answer> answers;

    /**
     * Creates a new instance.
     */
    public AnswerList() {
        answers = new LinkedList<Answer>();
    }

    public void add(Answer answer) throws IllegalArgumentException {
        if (canBeInGroup(answer)) {
            answers.add(answer);
        }
    }

    public LinkedList<Answer> answers() {
        return answers;
    }


    public boolean canBeInGroup(Answer answer) {
        for (Answer a : answers) {
            if (!a.canBeInSameList(answer)) {
                return false;
            }
        }
        return true;
    }

    public AnswerListType getType() {
        if (answers.size() == 0) {
            return AnswerListType.ESSAY;
        }
        Answer answer = answers.getFirst();
        if (answer instanceof BooleanAnswer) {
            return AnswerListType.BOOL;
        }
        if ((answer instanceof NumberAnswer) || (answer instanceof IntervalAnswer)) {
            return AnswerListType.NUMERIC;
        }
        if (answer instanceof ExternalAnswer) {
            return AnswerListType.EXTERNAL;
        }
        if (answer instanceof ChoiceAnswer) {
            int correctAnswerCount = 0;
            int incorrectAnswerCount = 0;
            int partiallyCorrectAnswerCount = 0;
            for (Answer a : answers) {
                ChoiceAnswer ta = (ChoiceAnswer) a;
                if (ta.isCorrect()) {
                    correctAnswerCount++;
                } else if (ta.getWeight() > 0) {
                    partiallyCorrectAnswerCount++;
                } else {
                    incorrectAnswerCount++;
                }
            }
            if (correctAnswerCount == answers.size()) {
                //if (incorrectAnswerCount == 0 && partiallyCorrectAnswerCount == 0) {
                return AnswerListType.CLOZE;
            } else if (correctAnswerCount == 1 /*&& partiallyCorrectAnswerCount == 0*/) {
                return AnswerListType.SINGLE_CHOICE;
            } else {
                return AnswerListType.MULTIPLE_CHOICE;
            }
        }
        if (answer instanceof MatchingPairAnswer) {
            return AnswerListType.MATCHING_PAIR;
        }

        return null;
    }


    public String toString() {
        return answers.toString();
    }
}
