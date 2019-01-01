/*
 * @(#)AnswerListType.java   1.1 2008-02-19
 *
 * Copyright (c) 2006-2008 Werner Randelshofer
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
    
    /** Questions without an answer list use this type. This type can be used
     * for pages which contain descriptive text about a test. */
    NONE
}
