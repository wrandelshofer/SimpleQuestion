
/*
 * @(#)Scanner.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.gui.highlight;

import java.util.HashMap;

/**
 * <p>A Scanner object provides a lexical analyser and a resulting token array.
 * Incremental rescanning is supported, e.g. for use in a token colouring
 * editor.  This is a base class dealing with plain text, which can be extended
 * to support other languages.
 * <p/>
 * <p>The actual text is assumed to be held elsewhere, e.g. in a document.  The
 * <code>change()</code> method is called to report the position and length of
 * a change in the text, and the <code>scan()</code> method is called to
 * perform scanning or rescanning.  For example, to scan an entire document
 * held in a character array <code>text</code> in one go:
 * <p/>
 * <blockquote>
 * <pre>
 * scanner.change(0, 0, text.length);
 * scanner.scan(text, 0, text.length);
 * </pre>
 * </blockquote>
 * <p/>
 * <p>For incremental scanning, the <code>position()</code> method is used
 * to find the text position at which rescanning should start.  For example, a
 * syntax highlighter might contain this code:
 * <p/>
 * <blockquote>
 * <pre>
 * // Where to start rehighlighting, and a segment object
 * int firstRehighlightToken;
 * Segment segment;
 * <p/>
 * ...
 * <p/>
 * // Whenever the text changes, e.g. on an insert or remove or read.
 * firstRehighlightToken = scanner.change(offset, oldLength, newLength);
 * repaint();
 * <p/>
 * ...
 * <p/>
 * // in repaintComponent
 * int offset = scanner.position();
 * if (offset < 0) return;
 * int tokensToRedo = 0;
 * int amount = 100;
 * while (tokensToRedo == 0 && offset >= 0)
 * {
 *    int length = doc.getLength() - offset;
 *    if (length > amount) length = amount;
 *    try { doc.getText(offset, length, text); }
 *    catch (BadLocationException e) { return; }
 *    tokensToRedo = scanner.scan(text.array, text.offset, text.count);
 *    offset = scanner.position();
 *    amount = 2*amount;
 * }
 * for (int i = 0; i < tokensToRedo; i++)
 * {
 *    Token t = scanner.getToken(firstRehighlightToken + i);
 *    int length = t.symbol.name.length();
 *    int type = t.symbol.type;
 *    doc.setCharacterAttributes (t.position, length, styles[type], false);
 * }
 * firstRehighlightToken += tokensToRedo;
 * if (offset >= 0) repaint(2);
 * </pre>
 * </blockquote>
 * <p/>
 * <p>Note that <code>change</code> can be called at any time, even between
 * calls to <code>scan</code>.  Only small number of characters are passed to
 * <code>scan</code> so that only a small burst of scanning is done, to prevent
 * the program's user interface from freezing.
 *
 * @version 1.3 2010-09-16 Werner Randelshofer Font Attributes for WHITESPACE
 * were not set.
 * <br>1.2 2006-10-09 Werner Randelshofer Don't repaint for each update.
 * <br>1.1 2006-05-11 Werner Randelshofer Font style attributes were not always
 * correctly set.
 */
public class Scanner implements TokenTypes {
    /**
     * The current buffer of text being scanned.
     */
    protected char[] buffer;

    /**
     * The current offset within the buffer, at which to scan the next token.
     */
    protected int start;

    /**
     * The end offset in the buffer.
     */
    protected int end;

    // BEGIN PATCH Werner Randelshofer
    /**
     * The larger context of the current scanner state.
     */
    protected int context = 0;
    // END PATCH Werner Randelshofer
    /**
     * The current scanner state, as a representative token type.
     */
    protected int state = WHITESPACE;

    // The array of tokens forms a gap buffer.  The total length of the text is
    // tracked, and tokens after the gap have (negative) positions relative to
    // the end of the text.  While scanning, the gap represents the area to be
    // scanned, no tokens after the gap can be taken as valid, and in particular
    // the end-of-text sentinel token is after the gap.

    private Token[] tokens;
    private int gap, endgap, textLength;
    private boolean scanning;
    private int position;
    /**
     * The symbol table can be accessed by <code>initSymbolTable</code> or
     * <code>lookup</code>, if they are overridden.  Symbols are inserted with
     * <code>symbolTable.put(sym,sym)</code> and extracted with
     * <code>symbolTable.get(sym)</code>.
     */
    protected HashMap<Symbol, Symbol> symbolTable;

    /**
     * Create a new Scanner representing an empty text document.  For
     * non-incremental scanning, use change() to report the document size, then
     * pass the entire text to the scan() method in one go, or if coming from an
     * input stream, a bufferful at a time.
     */
    protected Scanner() {
        tokens = new Token[1];
        gap = 0;
        endgap = 0;
        textLength = 0;
        symbolTable = new HashMap<>();
        initSymbolTable();
        Symbol endOfText = new Symbol(WHITESPACE, "");
        tokens[0] = new Token(endOfText, 0);
        scanning = false;
        position = 0;
    }


    /**
     * <p>Read one token from the start of the current text buffer, given the
     * start offset, end offset, and current scanner state.  The method moves
     * the start offset past the token, updates the scanner state, and returns
     * the type of the token just scanned.
     * <p/>
     * <p>The scanner state is a representative token type.  It is either the
     * state left after the last call to read, or the type of the old token at
     * the same position if rescanning, or WHITESPACE if at the start of a
     * document.  The method succeeds in all cases, returning whitespace or
     * comment or error tokens where necessary.  Each line of a multi-line
     * comment is treated as a separate token, to improve incremental
     * rescanning.  If the buffer does not extend to the end of the document,
     * the last token returned for the buffer may be incomplete and the caller
     * must rescan it.  The read method can be overridden to implement different
     * languages.  The default version splits plain text into words, numbers and
     * punctuation.
     */
    protected int read() {
        char c = buffer[start];
        int type;
        // Ignore the state, since there is only one.
        if (Character.isWhitespace(c)) {
            type = WHITESPACE;
            while (++start < end) {
                if (!Character.isWhitespace(buffer[start])) {
                    break;
                }
            }
        } else if (Character.isLetter(c)) {
            type = WORD;
            while (++start < end) {
                c = buffer[start];
                if (Character.isLetter(c) || Character.isDigit(c)) {
                    continue;
                }
                if (c == '-' || c == '\'' || c == '_') {
                    continue;
                }
                break;
            }
        } else if (Character.isDigit(c)) {
            type = NUMBER;
            while (++start < end) {
                c = buffer[start];
                if (!Character.isDigit(c) && c != '.') {
                    break;
                }
            }
        } else if (c >= '!' || c <= '~') {
            type = PUNCTUATION;
            start++;
        } else {
            type = UNRECOGNIZED;
            start++;
        }

        // state = WHITESPACE;
        return type;
    }


    // Move the gap to a new index within the tokens array.  When preparing to
    // pass a token back to a caller, this is used to ensure that the token's
    // position is relative to the start of the text and not the end.

    private void moveGap(int newgap) {
        if (scanning) {
            throw new Error("moveGap called while scanning");
        }
        if (newgap < 0 || newgap > gap + tokens.length - endgap) {
            throw new Error("bad argument to moveGap");
        }
        if (gap < newgap) {
            while (gap < newgap) {
                tokens[endgap].position += textLength;
                tokens[gap++] = tokens[endgap++];
            }
        } else if (gap > newgap) {
            while (gap > newgap) {
                tokens[--endgap] = tokens[--gap];
                tokens[endgap].position -= textLength;
            }
        }
    }

    /**
     * Find the number of available valid tokens, not counting tokens in or
     * after any area yet to be rescanned.
     */
    public int size() {
        if (scanning) {
            return gap;
        } else {
            return gap + tokens.length - endgap;
        }
    }

    /**
     * Find the n'th token, or null if it is not currently valid.
     */
    public Token getToken(int n) {
        if (n < 0 || n >= gap && scanning) {
            return null;
        }
        if (n >= gap) {
            moveGap(n + 1);
        }
        return tokens[n];
    }

    /**
     * Find the index of the valid token starting before, but nearest to, text
     * position p.  This uses an O(log(n)) binary chop search.
     */
    public int find(int p) {
        int start = 0, end, mid, midpos;
        if (!scanning) {
            moveGap(gap + tokens.length - endgap);
        }
        end = gap - 1;
        if (p > tokens[end].position) {
            return end;
        }
        while (end > start + 1) {
            mid = (start + end) / 2;
            midpos = tokens[mid].position;
            if (p > midpos) {
                start = mid;
            } else {
                end = mid;
            }
        }
        return start;
    }

    /**
     * Report the position of an edit, the length of the text being replaced,
     * and the length of the replacement text, to prepare for rescanning.  The
     * call returns the index of the token at which rescanning will start.
     */
    public int change(int start, int len, int newLen) {
        //System.out.println("change "+start+", "+len+", "+newLen);
        if (start < 0 || len < 0 || newLen < 0 || start + len > textLength) {
            throw new Error("change(" + start + "," + len + "," + newLen + ")");
        }

        textLength += newLen - len;
        int newEnd = start + newLen;
        int maxEnd = Math.max(start + len, newEnd);

        if (newLen < len) {
            while (gap > 0 && tokens[gap - 1].position > start) {
                gap--;
            }
            while (endgap < tokens.length - 1 && tokens[endgap].position < maxEnd) {
                endgap++;
            }
            if (gap > 1) {
                gap -= 2;
                position = tokens[gap].position;
                context = tokens[gap].context;
                state = tokens[gap].symbol.type;
            } else {
                gap = 0;
                position = 0;
                context = 0;
                state = WHITESPACE;
            }
                /*
            gap = 0;
            tokens[tokens.length - 1] = tokens[endgap];
            endgap = tokens.length - 1;
            position = 0;
            state = WHITESPACE;
            context = 0;*/
        } else {
            if (scanning) {
                while (gap > 0 && tokens[gap - 1].position > start) {
                    gap--;
                }
                if (gap > 1) {
                    gap -= 2;
                    position = tokens[gap].position;
                    state = tokens[gap].symbol.type;
                } else {
                    gap = 0;
                    position = 0;
                    state = WHITESPACE;
                }
                while (tokens[endgap].position + textLength < newEnd) {
                    endgap++;
                }
                return gap;
            }
            if (endgap == tokens.length) {
                moveGap(gap - 1);
            }
            while (tokens[endgap].position + textLength < start) {
                tokens[endgap].position += textLength;
                //System.out.println("shifting token down from "+endgap+" to "+gap);
                tokens[gap++] = tokens[endgap++];
            }
            while (gap > 0 && tokens[gap - 1].position > start) {
                //System.out.println("shifting token up from "+gap+" to "+endgap);
                tokens[--endgap] = tokens[--gap];
                tokens[endgap].position -= textLength;
            }
            if (gap > 2) {
                gap -= 3;
                position = tokens[gap].position;
                state = tokens[gap].symbol.type;
            } else {
                gap = 0;
                position = 0;
                context = 0;
                state = WHITESPACE;
            }
            while (tokens[endgap].position + textLength < newEnd) {
                endgap++;
            }
        }
        scanning = true;

        return gap;
        
/*
        textLength += newLen - len;
 
 
        // FIXME - The above code is buggy
        // As a workaround, if we get a negative position, we rescan the whole
        // text
        if (position < 0) {
            gap = 0;
            endgap = 0;
            //textLength = 0;
            Symbol endOfText = new Symbol(WHITESPACE, "");
            tokens = new Token[1];
            tokens[0] = new Token(endOfText, 0);
            position = 0;
            state = WHITESPACE;
            System.err.println("SCANNER is BUSTED - Rescanning everything!");
        }
        return gap;
 */
    }

    /**
     * Find out at what text position any remaining scanning work should
     * start, or -1 if scanning is complete.
     */
    public int position() {
        if (!scanning) {
            return -1;
        } else {
            return position;
        }
    }

    /**
     * Create the initial symbol table.  This can be overridden to enter
     * keywords, for example.  The default implementation does nothing.
     */
    protected void initSymbolTable() {
    }

    // Reuse this symbol object to create each new symbol, then look it up in
    // the symbol table, to replace it by a shared version to minimize space.

    private Symbol symbol = new Symbol(0, null);

    /**
     * Lookup a symbol in the symbol table.  This can be overridden to implement
     * keyword detection, for example.  The default implementation just uses the
     * table to ensure that there is only one shared occurrence of each symbol.
     */
    protected Symbol lookup(int type, String name) {
        symbol.type = type;
        symbol.name = name;
        Symbol sym = symbolTable.get(symbol);
        if (sym != null) {
            return sym;
        }
        sym = new Symbol(type, name);
        symbolTable.put(sym, sym);
        return sym;
    }

    /**
     * Returns true, if the provided name is already in the symbol table.
     */
    protected boolean isSymbol(int type, String name) {
        symbol.type = type;
        symbol.name = name;
        return symbolTable.containsKey(symbol);
    }

    /**
     * Scan or rescan a given read-only segment of text.  The segment is assumed
     * to represent a portion of the document starting at
     * <code>position()</code>.  Return the number of tokens successfully
     * scanned, excluding any partial token at the end of the text segment but
     * not at the end of the document.  If the result is 0, the call should be
     * retried with a longer segment.
     */
    public int scan(char[] array, int offset, int length) {
        if (!scanning) {
            throw new Error("scan called when not scanning");
        }
        if (position + length > textLength) {
            throw new Error("scan too much");
        }
        boolean all = position + length == textLength;
        end = start + length;
        int startGap = gap;

        buffer = array;
        start = offset;
        end = start + length;

        // BEGIN PATCH Werner Randelshofer
        if (gap > 0) {
            state = tokens[gap - 1].symbol.type;
            context = tokens[gap - 1].context;
        } else {
            state = WHITESPACE;
            context = 0;
        }
        // END PATCH Werner Randelshofer

        while (start < end) {
            int tokenStart = start;
            // int previousContext = context;
            int type = read();
            if (start == end && !all) {
                break;
            }

            //if (type != WHITESPACE) {
            //  String name =
            //          new String(buffer, tokenStart, start - tokenStart);
            if (tokenStart >= buffer.length || start > buffer.length) {
                break;
            }
            String name = type == WHITESPACE ? " "
                    : new String(buffer, tokenStart, start - tokenStart);
            Symbol sym = lookup(type, name);
            Token t = new Token(sym, position);
            t.context = context;
            if (gap >= endgap) {
                checkCapacity(gap + tokens.length - endgap + 1);
            }
            tokens[gap++] = t;
            //}

            // Try to synchronise
            while (tokens[endgap].position + textLength < position) {
                endgap++;
            }
            if (position + start - tokenStart == textLength) {
                scanning = false;
            } else if (
                    gap > 0
                            && tokens[endgap].position + textLength == position
                            && tokens[endgap].symbol.type == type
                            && tokens[endgap].context == context) {
                endgap++;
                scanning = false;
                break;
            }
            position += start - tokenStart;
        }
        checkCapacity(gap + tokens.length - endgap);
        return gap - startGap;
    }

    /**
     * Change the size of the gap buffer, doubling it if it fills up, and
     * halving if it becomes less than a quarter full.
     */
    private void checkCapacity(int capacity) {
        int oldCapacity = tokens.length;
        if (capacity <= oldCapacity && 4 * capacity >= oldCapacity) {
            return;
        }
        Token[] oldTokens = tokens;
        int newCapacity;
        if (capacity > oldCapacity) {
            newCapacity = oldCapacity * 2;
            if (newCapacity < capacity) {
                newCapacity = capacity;
            }
        } else {
            newCapacity = capacity * 2;
        }

        tokens = new Token[newCapacity];
        System.arraycopy(oldTokens, 0, tokens, 0, gap);
        int n = oldCapacity - endgap;
        System.arraycopy(oldTokens, endgap, tokens, newCapacity - n, n);
        endgap = newCapacity - n;
    }

    public void print() {
        for (int i = 0; i < tokens.length; i++) {
            if (i >= gap && i < endgap) {
                continue;
            }
            if (i == endgap) {
                System.out.print("... ");
            }
            System.out.print("" + i + ":" + tokens[i].position);
            System.out.print("-" + (tokens[i].position + tokens[i].symbol.name.length()));
            System.out.print(" ");
        }
        System.out.println();
    }

    public String dump() {
        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < tokens.length; i++) {
            if (i >= gap && i < endgap) {
                continue;
            }
            if (i == endgap) {
                buf.append("...\n");
            }
            buf.append(i);
            buf.append(':');
            if (tokens[i] != null) {
                buf.append(tokens[i].dump());
            }
            buf.append('\n');
        }

        return buf.toString();
    }
}
