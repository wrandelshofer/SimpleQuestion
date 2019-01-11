package ch.randelshofer.gui.highlight;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.io.*;

/**
 * Display text with syntax highlighting.  Highlighting is done with full
 * accuracy, using a given language scanner.  Large amounts of re-highlighting
 * are done in small bursts to make sure the user interface doesn't freeze.
 *
 * @version 1.3 2010-09-16 Werner Randelshofer Font Attributes for WHITESPACE
 * were not set.
 * <br>1.2 2006-10-09 Werner Randelshofer Don't repaint for each update.
 * <br>1.1 2006-05-11 Werner Randelshofer Font style attributes were not always
 * correctly set.
 */
public class SyntaxHighlighter extends JTextPane implements DocumentListener, TokenTypes {
    static final long serialVersionUID = 1L;
    private StyledDocument doc;
    private Scanner scanner;
    
    private Runnable highlightUpdater = new Runnable() {
        @Override
        public void run() {
            updateHighlight();
        }
    };
    
    
    /**
     * Create a graphics component which displays text with syntax highlighting.
     * Provide a width and height, in characters, and a language scanner.
     */
    public SyntaxHighlighter(int height, int width, Scanner scanner) {
        super(new DefaultStyledDocument());
        doc = (StyledDocument) getDocument();
        this.scanner = scanner;
        doc.addDocumentListener(this);
        initStyles();
    }
    /** BEGIN PATCH Werner Randelshofer. */
    @Override
    public void setDocument(Document newValue) {
        int oldLength = 0;
        if (doc != null) {
            oldLength = getDocument().getLength();
            doc.removeDocumentListener(this);
        }
        doc = (StyledDocument) newValue;
        super.setDocument(newValue);
        if (doc != null) {
            doc.addDocumentListener(this);
            if (scanner != null) {
                int newLength = getDocument().getLength();
                firstRehighlightToken = scanner.change(0, oldLength, newLength);
                //scanner.change(0, 0, newLength);
            }
            updateLater();
        }
    }
    /** END PATCH Werner Randelshofer. */
    
    /**
     * Read new text into the component from a <code>Reader</code>.  Overrides
     * <code>read</code> in <code>JTextComponent</code> in order to highlight
     * the new text.
     */
    @Override
    public void read(Reader in, Object desc) throws IOException {
        int oldLength = getDocument().getLength();
        doc.removeDocumentListener(this);
        super.read(in, desc);
        doc = (StyledDocument) getDocument();
        doc.addDocumentListener(this);
        int newLength = getDocument().getLength();
        firstRehighlightToken = scanner.change(0, oldLength, newLength);
        updateLater();
    }
    
    @Override
    public void setFont(Font font) {
        super.setFont(font);
        if (doc != null) {
            updateStyles();
            //setDocument(doc);
            if (scanner != null) {
                int newLength = getDocument().getLength();
                firstRehighlightToken = scanner.change(0, newLength, newLength);
                //scanner.change(0, 0, newLength);
            }
            updateLater();
        }
    }
    
    // An array of styles, indexed by token type.  Default styles are set up,
    // which can be used for any languages.
    private Style[] styles;
    private void initStyles() {
        styles = new Style[Math.max(255, typeNames.length)];
        changeStyle(UNRECOGNIZED, Color.red.brighter(), new Color(0xfff0f0), Font.BOLD, null);
        changeStyle(WHITESPACE, Color.black);
        changeStyle(WORD, Color.black);
        changeStyle(NUMBER, new Color(0xce7b00)); // dark orange
        changeStyle(PUNCTUATION, new Color(0xce7b00)); // dark orange
        changeStyle(COMMENT, new Color(0x969696)); // dark gray
        changeStyle(START_COMMENT, new Color(0x969696)); // dark gray
        changeStyle(MID_COMMENT, new Color(0x969696)); // dark gray
        changeStyle(END_COMMENT, new Color(0x969696)); // dark gray
        changeStyle(TAG, new Color(0x0000e6), Font.BOLD); // dark blue
        changeStyle(END_TAG, new Color(0x0000e6), Font.BOLD);
        changeStyle(KEYWORD, new Color(0x0000e6)); // dark blue
        changeStyle(KEYWORD2, new Color(0x0000e6)); // dark blue
        changeStyle(IDENTIFIER, Color.black);
        changeStyle(LITERAL, Color.magenta);
        changeStyle(STRING, Color.magenta);
        changeStyle(CHARACTER, Color.magenta);
        changeStyle(OPERATOR, Color.black, Font.BOLD);
        changeStyle(BRACKET, new Color(0xce7b00)); // dark orange
        changeStyle(SEPARATOR, new Color(0xce7b00)); // dark orange
        changeStyle(URL, new Color(0x0000e6)); // dark blue
        
        for (int i = 0; i < styles.length; i++) {
            if (styles[i] == null) styles[i] = styles[WHITESPACE];
        }
    }
    
    private void updateStyles() {
        for (int i = 0; i < styles.length; i++) {
            Style style = styles[i];
            StyleConstants.setFontFamily(style, getFont().getFamily());
            StyleConstants.setFontSize(style, getFont().getSize());
        }
    }
    /**
     * Change the style of a particular type of token.
     */
    public void changeStyle(int type, Color color) {
        changeStyle(type, color, Color.WHITE, Font.PLAIN, null);
    }
    /**
     * Change the style of a particular type of token.
     */
    public void changeStyle(int type, Color foreground, Color background, int fontStyle, Icon icon) {
        Style style = addStyle(typeNames[type], null);
        StyleConstants.setForeground(style, foreground);
        if (background != null) {
        StyleConstants.setBackground(style, background);
        }
        StyleConstants.setBold(style, (fontStyle & Font.BOLD) != 0);
        StyleConstants.setItalic(style, (fontStyle & Font.ITALIC) != 0);
        StyleConstants.setFontFamily(style, getFont().getFamily());
        StyleConstants.setFontSize(style, getFont().getSize());
        if (icon != null) {
        StyleConstants.setIcon(style, icon);
        }
        styles[type] = style;
    }
    
    /**
     * Change the style of a particular type of token, including adding bold or
     * italic using a third argument of <code>Font.BOLD</code> or
     * <code>Font.ITALIC</code> or the bitwise union
     * <code>Font.BOLD|Font.ITALIC</code>.
     */
    public void changeStyle(int type, Color color, int fontStyle) {
        changeStyle(type, color, Color.WHITE, fontStyle, null);
    }
    /**
     * Change the style of a particular type of token, including adding bold or
     * italic using a third argument of <code>Font.BOLD</code> or
     * <code>Font.ITALIC</code> or the bitwise union
     * <code>Font.BOLD|Font.ITALIC</code>.
     */
    public void changeStyle(int type, Color color, Icon icon) {
        changeStyle(type, color, Color.WHITE, Font.PLAIN, icon);
    }
    
    /**
     * <font style='color:gray;'>Ignore this method. Responds to the
     * underlying document changes by re-highlighting.</font>
     */
    @Override
    public void insertUpdate(DocumentEvent e) {
        int offset = e.getOffset();
        int length = e.getLength();
//System.out.println("SyntaxHighlighter.insertUpdate o="+offset+" len="+length);
        firstRehighlightToken = scanner.change(offset, 0, length);
        //repaint();
        updateLater();
    }
    
    /**
     * <font style='color:gray;'>Ignore this method. Responds to the
     * underlying document changes by re-highlighting.</font>
     */
    @Override
    public void removeUpdate(DocumentEvent e) {
        int offset = e.getOffset();
        int length = e.getLength();
        firstRehighlightToken = scanner.change(offset, length, 0);
        //repaint();
        updateLater();
    }
    
    /**
     * <font style='color:gray;'>Ignore this method. Responds to the
     * underlying document changes by re-highlighting.</font>
     */
    @Override
    public void changedUpdate(DocumentEvent e) {
        // Do nothing.
    }
    
    // Scan a small portion of the document.  If more is needed, call repaint()
    // so the GUI gets a go and doesn't freeze, but calls this again later.
    
    Segment text = new Segment();
    int firstRehighlightToken;
    int smallAmount = 100;
    
    /**
     * <font style='color:gray;'>Ignore this method. Carries out a small
     * amount of re-highlighting for each call to <code>repaint</code>.</font>
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        updateHighlight();
    }
    
    private void updateHighlight() {
        synchronized (highlightUpdater) {
            if (scanner != null) {
                int offset = scanner.position();
            //System.out.println("updateHighlight offset:"+offset);                
                if (offset < 0) return;
                
                int tokensToRedo = 0;
                int amount = smallAmount;
                while (tokensToRedo == 0 && offset >= 0) {
                    int length = doc.getLength() - offset;
                    if (length > amount) length = amount;
                    try {
                        doc.getText(offset, length, text);
                    } catch (BadLocationException e) {
                        return;
                    }
                    tokensToRedo = scanner.scan(text.array, text.offset, text.count);
                    offset = scanner.position();
                    amount = 2 * amount;
                }
                for (int i = 0; i < tokensToRedo; i++) {
                    Token t = scanner.getToken(firstRehighlightToken + i);
                    if (t == null) return;
                    int length = t.symbol.name.length();
                    int type = t.symbol.type;
                    if (type < 0) type = UNRECOGNIZED;
                    doc.setCharacterAttributes(t.position, length, styles[type], false);
                }
                firstRehighlightToken += tokensToRedo;
                if (offset >= 0) {
                    updateLater();
                    //repaint(2);
            /*
            javax.swing.Timer timer = new javax.swing.Timer(2, repainter);
            timer.setRepeats(false);
            timer.setInitialDelay(2);
            timer.start();
             */
                }
            }
        }
    }
    
    private void updateLater() {
        if (highlightUpdater != null) {
            SwingUtilities.invokeLater(highlightUpdater);
        }
    }
}
