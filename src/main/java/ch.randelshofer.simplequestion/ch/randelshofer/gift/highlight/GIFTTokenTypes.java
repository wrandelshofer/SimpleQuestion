/*
 * @(#)GIFTTokenTypes.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.gift.highlight;

import ch.randelshofer.gui.highlight.TokenTypes;

/**
 * GIFTTokenTypes.
 *
 * @author Werner Randelshofer
 * @version 1.0 10. Mai 2006 Created.
 */
public interface GIFTTokenTypes extends TokenTypes {
    public final static int
            //We mark all words that we can not classify as question or answer,
            //with this type. 
            //The type is commented out here, because we inherit it from our
            //super interface.
            //WORD = 2,

            //Used for numbers in a numeric answer list.
            //The type is commented out here, because we inherit it from our
            //super interface.
            //NUMBER = 3,

            //Not used
            //PUNCTUATION = 4,

            //GIFT single line comment
            //The type is commented out here, because we inherit it from our
            //super interface.
            //COMMENT = 5,

            //Not used
            //START_COMMENT = 6,
            //MID_COMMENT = 7,
            //END_COMMENT = 8,
            //TAG = 9,
            //END_TAG = 10,

            //Used for TRUE, FALSE keyword in the answer list
            //The type is commented out here, because we inherit it from our
            //super interface.
            //KEYWORD = 11,

            //Not used
            //KEYWORD2 = 12,
            //IDENTIFIER = 13,

            // used
            //LITERAL = 14,

            // not  used
            //STRING = 15,
            //CHARACTER = 16,

            //Used for operators in the answer list '#','%','-','>'
            //The type is commented out here, because we inherit it from our
            //super interface.
            //OPERATOR = 17,

            //The type is commented out here, because we inherit it from our
            //super interface.
            //BRACKET = 18,
            //Used for separators in the answer list: '~','='
            //The type is commented out here, because we inherit it from our
            //super interface.
            //SEPARATOR = 19,

            //Not used
            //URL = 20;

            // We mark all words in the title part of a GIFT question with 
            // this token type.
            TITLE = 9,

    // We mark all words in the question part of a GIFT question with
    // this token type.
    QUESTION = 12,

    // We mark all words in the answer part of a GIFT question with
    // this token type.
    ANSWER = 13,
    // We mark all words in the feedback part of a GIFT question with
    // this token type.
    FEEDBACK = 20,

    NUMERIC_OPERATOR = 21,

    // A newline character \n
    NEWLINE = 22,

    // Two newline characters \n\n are used to separate questions
    QUESTION_SEPARATOR = 23;
}
