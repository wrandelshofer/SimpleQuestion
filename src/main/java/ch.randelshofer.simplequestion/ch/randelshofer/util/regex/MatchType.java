/*
 * @(#)MatchType.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.util.regex;

/**
 * Typesafe Enumeration of Syntaxes for the Parser.
 *
 * @author Werner Randelshofer
 * @version 5.0 2005-01-31 Reworked.
 * <br>1.0  November 14, 2004  Created.
 */
public class MatchType  /*implements Comparable*/ {
    private MatchType() {
    }

    public static final MatchType CONTAINS = new MatchType();
    public static final MatchType STARTS_WITH = new MatchType();
    public static final MatchType FULL_WORD = new MatchType();
}
