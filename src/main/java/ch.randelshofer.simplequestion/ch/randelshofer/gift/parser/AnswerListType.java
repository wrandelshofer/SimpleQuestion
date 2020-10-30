/*
 * @(#)AnswerListType.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.gift.parser;

/**
 * AnswerListType.
 *
 * @author Werner Randelshofer
 * @version 1.1 2008-02-19 Added numeric type.
 * <br>1.0 2006-05-02 Created.
 */
public enum AnswerListType {
    SINGLE_CHOICE,
    MULTIPLE_CHOICE,
    MATCHING_PAIR,
    CLOZE,
    BOOL,
    NUMERIC,
    EXTERNAL,
    ESSAY,

    /**
     * Questions without an answer list use this type. This type can be used
     * for pages which contain descriptive text about a test.
     */
    NONE
}
