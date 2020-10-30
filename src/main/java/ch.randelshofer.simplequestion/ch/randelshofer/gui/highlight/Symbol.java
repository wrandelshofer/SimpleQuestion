
/*
 * @(#)Symbol.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.gui.highlight;

/**
 * A Symbol represents the information shared between similar tokens,
 * i.e. their type and spelling.
 */
public class Symbol {
    /**
     * The type is a small integer used to classify symbols.  It also
     * distinguishes different symbols with the same spelling, where necessary.
     */
    public int type;

    /**
     * The spelling.
     */
    public String name;

    /**
     * Construct a symbol from its type and name.
     */
    public Symbol(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public String dump() {
        return "t:" + type + " " + name;
    }

    /**
     * Return the name of the symbol.
     */
    public String toString() {
        return name;
    }

    /**
     * Form a hash value from the type and name.
     */
    public int hashCode() {
        return name.hashCode() + type;
    }

    /**
     * Compare the type and name with some other symbol.
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof Symbol)) {
            return false;
        }
        Symbol that = (Symbol) obj;
        return name.equals(that.name) && type == that.type;
    }
}

