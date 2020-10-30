/*
 * @(#)MatchType.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.teddy.regex;

/**
 * Typesafe Enumeration of Syntaxes for the Parser.
 *
 * @author Werner Randelshofer
 * @version $Id: MatchType.java 527 2009-06-07 14:28:19Z rawcoder $
 */
public class MatchType  /*implements Comparable*/ {
    private MatchType() {
    }

    public static final MatchType CONTAINS = new MatchType();
    public static final MatchType STARTS_WITH = new MatchType();
    public static final MatchType FULL_WORD = new MatchType();
}
