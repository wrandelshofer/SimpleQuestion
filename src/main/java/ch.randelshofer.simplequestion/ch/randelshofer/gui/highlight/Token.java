
package ch.randelshofer.gui.highlight;

/**
 * A token represents a smallest meaningful fragment of text, such as a word,
 * recognised by a scanner.
 */
public class Token {
    /**
     * The symbol contains all the properties shared with similar tokens.
     */
    public Symbol symbol;

    // BEGIN PATCH Werner Randelshofer
    /**
     * The context of the token. This is used for non-contextfree grammars.
     */
    public int context;
    // END PATCH Werner Randelshofer
    
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
    
    public String dump() {
        return "p:"+position+" c:"+context+" "+symbol.dump();
    }
}
