
/*
 * @(#)Token.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package org.ghinkle.jarjunkie.view.content.java;

/**
 * A token represents a smallest meaningful fragment of text, such as a word,
 * recognised by a scanner.
 */
public class Token {
    /**
     * The symbol contains all the properties shared with similar tokens.
     */
    public Symbol symbol;

    /**
     * The token's position is given by an index into the document text.
     */
    public int position;

    /**
     * Create a token with a given symbol and position.
     */
    Token(Symbol symbol, int position) {
        this.symbol = symbol;
        this.position = position;
    }
}
