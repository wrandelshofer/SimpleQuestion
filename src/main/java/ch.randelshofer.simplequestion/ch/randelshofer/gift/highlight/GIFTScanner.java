/* @(#)GIFTScanner.java
 *
 * Copyright (c) 2006-2008 Hochschule Luzern, Fachstelle Neue Lernmedien,
 * Zentralstrasse 18, Postfach 2858, CH-6002 Lucerne, Switzerland
 * All rights reserved.
 *
 * The copyright of this software is owned by Hochschule Luzern (HSLU).
 * You may not use, copy or modify this software except in accordanc with
 * the license agreement you entered into with HSLU. For details see
 * accompanying license terms.
 */
package ch.randelshofer.gift.highlight;

import ch.randelshofer.gui.highlight.Scanner;

/**
 * <p>Provide a hand-written scanner for the GIFT language.
 * This scanner is used for syntax highlighting in the editor.
 *
 * @version 2.0.1 2008-12-03 The '.' character was wrongly marked as bad if it was
 * located at CONTEXT_TEXTUAL_ANSWERBEGIN. Multi-line comments were wrongly
 * supported. The second new-line after a single-line comment was wrongly
 * considered as being part of the comment.
 * <br>2.0 2008-02-20 Added CONTEXT_QUESTIN_BEGIN. Fixed highlighting
 * issues with titles.
 * <br>1.1.1 2006-11-29 Fixed bug in read method.
 * <br>1.1 2006-10-08 Fixed highlighting of full stops.
 */
public class GIFTScanner extends Scanner implements GIFTTokenTypes {

    private final static int CONTEXT_QUESTION_BEGIN = 0;
    private final static int CONTEXT_QUESTION_TITLE = 1;
    private final static int CONTEXT_QUESTION = 2;
    private final static int CONTEXT_ANSWERLIST_BEGIN = 3;
    private final static int CONTEXT_TEXTUAL_ANSWERLIST = 4;
    private final static int CONTEXT_NUMERICAL_ANSWERLIST = 5;
    private final static int CONTEXT_TEXTUAL_ANSWERBEGIN = 6;
    private final static int CONTEXT_TEXTUAL_ANSWERWEIGHT = 7;
    private final static int CONTEXT_TEXTUAL_ANSWER = 8;
    private final static int CONTEXT_TEXTUAL_ANSWER_PAIR = 9;
    private final static int CONTEXT_TEXTUAL_ANSWERFEEDBACK = 10;
    private final static int CONTEXT_NUMERICAL_ANSWERBEGIN = 11;
    private final static int CONTEXT_NUMERICAL_ANSWERWEIGHT = 12;
    private final static int CONTEXT_NUMERICAL_ANSWER = 13;
    private final static int CONTEXT_NUMERICAL_ANSWERFEEDBACK = 14;
    private boolean debug = false;
    /**
     * Classify the ascii characters using an array of kinds.
     */
    private static final byte[] kind = new byte[128];
    /**
     * Classify all
     * other unicode characters using an array indexed by unicode category.
     * See the source file java/lang/Character.java for the categories.
     * To find the classification of a character, use:
     * if (c < 128) k = kind[c]; else k = unikind[Character.getType(c)];
     */
    private static final byte[] unikind = new byte[31];
    /**
     * Record the number of source code characters used up.
     */
    private int charlength = 1;
    /**
     * To deal with an odd
     * or even number of backslashes preceding a unicode escape, whenever a
     * second backslash is coming up, mark its position as a pair.
     */
    private int pair = 0;

    public GIFTScanner() {
        initKind();
        initUniKind();
    }

    private char next() {
        charlength = 1;
        if (start >= end) {
            return 26;
        } // EOF
        char c = buffer[start];
        if (c != '\\') {
            return c;
        }

        if (start == pair) {
            pair = 0;
            charlength = 2;
            return '\\';
        }
        if (start + 1 >= end) {
            return '\\';
        }

        c = buffer[start + 1];
        charlength = 2;
        if (c == '\\') {
            pair = start + 1;
        }
        return '\\';
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
    @Override
    protected int read() {
        // System.out.println("GIFTScanner read "+start+".."+end+" c="+context);
        int begin = start;
        charlength = 1;

        if (start >= end) {
            return WHITESPACE;
        }

        char c = buffer[start];
        int type = getKind(c);
        if (c == '\\') {
            c = next();
            type = WORD;
        }

        boolean consumed = false;

        // comments are context free;
        switch (type) {
            case COMMENT:
                start = start + charlength;
                charlength = 1;
                type = readSlash();
                if (type == START_COMMENT) {
                    state = MID_COMMENT;
                }
                consumed = true;
                break;
            case WHITESPACE:
                start = start + charlength;
                charlength = 1;
                while (start < end) {
                    c = buffer[start];
                    int k = getKind(c);
                    if (c == '\\') {
                        c = next();
                        k = WORD;
                    }
                    if (k != WHITESPACE) {
                        break;
                    }
                    start = start + charlength;
                    charlength = 1;
                }
                consumed = true;
                break;
            case WORD:
                start = start + charlength;
                charlength = 1;
                while (start < end) {
                    c = buffer[start];
                    int k = getKind(c);
                    if (c == '\\') {
                        c = next();
                        k = WORD;
                    }
                    if (k != WORD) {
                        break;
                    }
                    start = start + charlength;
                    charlength = 1;
                }
                consumed = true;
                break;
            case NUMBER:
                int decimalPoint = -10;
                start = start + charlength;
                charlength = 1;
                while (start < end) {
                    c = buffer[start];
                    int k = getKind(c);
                    if (c == '\\') {
                        c = next();
                        k = WORD;
                    }
                    if (k == OPERATOR && c == '.') {
                        if (decimalPoint == -10) {
                            decimalPoint = start;
                            k = NUMBER;
                        }
                    }
                    if (k != NUMBER) {
                        break;
                    }
                    start = start + charlength;
                    charlength = 1;
                }
                if (decimalPoint == start - 1) {
                    start = start - 1;
                }
                consumed = true;
                break;
        }

        // ------------------------

        switch (context) {
            case CONTEXT_QUESTION_BEGIN:
                switch (type) {
                    case WORD:
                    case NUMBER:
                        type = WORD;
                        context = CONTEXT_QUESTION;
                        break;
                    case OPERATOR:
                        if (!consumed) {
                            start = start + charlength;
                            charlength = 1;
                            switch (c) {
                                case ':':
                                    type = readTitleOperator(c);
                                    context = CONTEXT_QUESTION_TITLE;
                                    break;
                                case '{':
                                    context = CONTEXT_ANSWERLIST_BEGIN;
                                    break;
                                case '}':
                                    type = bad(type);
                                    break;
                                default:
                                    context = CONTEXT_QUESTION;
                                    break;
                            }
                            consumed = true;
                        }
                        break;
                }
                break;
            case CONTEXT_QUESTION_TITLE:
                switch (type) {
                    case WORD:
                    case NUMBER:
                        type = TITLE;
                        break;
                    case OPERATOR:
                        if (!consumed) {
                            start = start + charlength;
                            charlength = 1;
                            type = readTitleOperator(c);
                            if (type == OPERATOR) {
                                context = CONTEXT_QUESTION;
                            } else {
                                type = TITLE;
                            }
                            consumed = true;
                        }
                        break;
                }
                break;

            case CONTEXT_QUESTION:
                switch (type) {
                    case NUMBER:
                        type = WORD;
                        break;
                    case OPERATOR:
                        if (!consumed) {
                            start = start + charlength;
                            charlength = 1;
                            switch (c) {
                                case '{':
                                    context = CONTEXT_ANSWERLIST_BEGIN;
                                    break;
                                case '}':
                                    type = bad(type);
                                    break;
                                case ':':
                                    type = readTitleOperator(':');
                                    if (type == OPERATOR) {
                                        type = bad(type);
                                    }
                                    break;
                                default:
                                    type = WORD;
                                    break;
                            }
                            consumed = true;
                        } else {
                            type = WORD;
                        }
                        break;
                    case NEWLINE:
                        if (!consumed) {
                            start = start + charlength;
                            charlength = 1;
                            type = readQuestionSeparator(c);
                            if (type == QUESTION_SEPARATOR) {
                                context = CONTEXT_QUESTION_BEGIN;
                            }
                            consumed = true;
                        }
                        break;
                }
                break;
            case CONTEXT_ANSWERLIST_BEGIN:
                switch (type) {
                    case WORD:
                        if (isSymbol(LITERAL, new String(buffer, begin, start - begin))) {
                            type = LITERAL;
                        } else {
                            type = ANSWER;
                            // type = bad(type);
                        }
                        context = CONTEXT_TEXTUAL_ANSWER;
                        break;
                    case OPERATOR:
                        if (!consumed) {
                            start = start + charlength;
                            charlength = 1;
                            switch (c) {
                                case '#':
                                    context = CONTEXT_NUMERICAL_ANSWERLIST;
                                    type = NUMERIC_OPERATOR;
                                    break;
                                case '%':
                                    readExternal();
                                    type = LITERAL;
                                    context = CONTEXT_TEXTUAL_ANSWERLIST;
                                    break;
                                case '=':
                                case '~':
                                    context = CONTEXT_TEXTUAL_ANSWERBEGIN;
                                    break;
                                default:
                                    context = CONTEXT_TEXTUAL_ANSWERLIST;
                                    type = bad(type);
                                    break;
                            }
                            consumed = true;
                        }
                        break;
                }
                break;
            case CONTEXT_TEXTUAL_ANSWERLIST:
                switch (type) {
                    case OPERATOR:
                        if (!consumed) {
                            start = start + charlength;
                            charlength = 1;
                            switch (c) {
                                case '=':
                                case '~':
                                    context = CONTEXT_TEXTUAL_ANSWERBEGIN;
                                    break;
                                case '}':
                                    context = CONTEXT_QUESTION;
                                    break;
                                default:
                                    type = bad(type);
                                    break;
                            }
                            consumed = true;
                        }
                        break;
                }
                break;
            case CONTEXT_TEXTUAL_ANSWERBEGIN:
                switch (type) {
                    case NUMBER:
                    case WORD:
                        type = ANSWER;
                        context = CONTEXT_TEXTUAL_ANSWER;
                        break;
                    case OPERATOR:
                        if (!consumed) {
                            start = start + charlength;
                            charlength = 1;
                            switch (c) {
                                case '%':
                                    context = CONTEXT_TEXTUAL_ANSWERWEIGHT;
                                    break;
                                case '}':
                                    type = bad(type);
                                    context = CONTEXT_QUESTION;
                                    break;
                                case '-':
                                    c = buffer[start];
                                    charlength = 1;
                                    int k = getKind(c);
                                    if (c == '\\') {
                                        c = next();
                                        k = ANSWER;
                                    }
                                    if (c == '>') {
                                        type = bad(OPERATOR);
                                        start = start + charlength;
                                        charlength = 1;
                                    } else {
                                        type = ANSWER;
                                    }
                                    context = CONTEXT_TEXTUAL_ANSWER;
                                    break;
                                case '.':
                                    type = ANSWER;
                                    context = CONTEXT_TEXTUAL_ANSWER;
                                    break;
                                default:
                                    type = bad(type);
                                    break;
                            }
                            consumed = true;
                        }
                        break;
                }
                break;
            case CONTEXT_TEXTUAL_ANSWERWEIGHT:
                switch (type) {
                    case NUMBER:
                        type = OPERATOR;
                        break;
                    case WORD:
                        context = CONTEXT_TEXTUAL_ANSWER;
                        type = bad(type);
                        break;
                    case OPERATOR:
                        if (!consumed) {
                            start = start + charlength;
                            charlength = 1;
                            switch (c) {
                                case '%':
                                    context = CONTEXT_TEXTUAL_ANSWER;
                                    break;
                                case '-':
                                    type = OPERATOR;
                                    break;
                                case '}':
                                    type = bad(type);
                                    context = CONTEXT_QUESTION;
                                    break;
                                default:
                                    type = bad(type);
                                    context = CONTEXT_TEXTUAL_ANSWER;
                                    break;
                            }
                            consumed = true;
                        }
                        break;
                }
                break;
            case CONTEXT_TEXTUAL_ANSWER:
                switch (type) {
                    case NUMBER:
                    case WORD:
                        type = ANSWER;
                        context = CONTEXT_TEXTUAL_ANSWER;
                        break;
                    case OPERATOR:
                        if (!consumed) {
                            start = start + charlength;
                            charlength = 1;
                            switch (c) {
                                case '=':
                                case '~':
                                    context = CONTEXT_TEXTUAL_ANSWERBEGIN;
                                    break;
                                case '#':
                                    context = CONTEXT_TEXTUAL_ANSWERFEEDBACK;
                                    break;
                                case '.':
                                case ':':
                                case '>':
                                    type = ANSWER;
                                    context = CONTEXT_TEXTUAL_ANSWER;
                                    break;
                                case '-':
                                    c = buffer[start];
                                    charlength = 1;
                                    int k = getKind(c);
                                    if (c == '\\') {
                                        c = next();
                                        k = ANSWER;
                                    }
                                    if (c == '>') {
                                        type = OPERATOR;
                                        start = start + charlength;
                                        charlength = 1;
                                        context = CONTEXT_TEXTUAL_ANSWER_PAIR;
                                    } else {
                                        type = ANSWER;
                                        context = CONTEXT_TEXTUAL_ANSWER;
                                    }
                                    break;
                                case '}':
                                    context = CONTEXT_QUESTION;
                                    break;
                                default:
                                    type = bad(type);
                                    break;
                            }
                            consumed = true;
                        }
                        break;
                }
                break;
            case CONTEXT_TEXTUAL_ANSWER_PAIR:
                switch (type) {
                    case NUMBER:
                    case WORD:
                        type = ANSWER;
                        break;
                    case OPERATOR:
                        if (!consumed) {
                            start = start + charlength;
                            charlength = 1;
                            switch (c) {
                                case '=':
                                case '~':
                                    context = CONTEXT_TEXTUAL_ANSWERBEGIN;
                                    break;
                                case '#':
                                    context = CONTEXT_TEXTUAL_ANSWERFEEDBACK;
                                    break;
                                case '.':
                                case ':':
                                case '>':
                                    type = ANSWER;
                                    break;
                                case '-':
                                    c = buffer[start];
                                    charlength = 1;
                                    int k = getKind(c);
                                    if (c == '\\') {
                                        c = next();
                                        k = ANSWER;
                                    }
                                    if (c == '>') {
                                        type = bad(OPERATOR);
                                        start = start + charlength;
                                        charlength = 1;
                                    } else {
                                        type = ANSWER;
                                    }
                                    break;
                                case '}':
                                    context = CONTEXT_QUESTION;
                                    break;
                                default:
                                    type = bad(type);
                                    break;
                            }
                            consumed = true;
                        }
                        break;
                }
                break;
            case CONTEXT_TEXTUAL_ANSWERFEEDBACK:
                switch (type) {
                    case NUMBER:
                    case WORD:
                        type = FEEDBACK;
                        break;
                    case OPERATOR:
                        if (!consumed) {
                            start = start + charlength;
                            charlength = 1;
                            switch (c) {
                                case '=':
                                case '~':
                                    context = CONTEXT_TEXTUAL_ANSWERBEGIN;
                                    break;
                                case '}':
                                    context = CONTEXT_QUESTION;
                                    break;
                                case '{':
                                case '#':
                                    type = bad(type);
                                    break;
                                default:
                                    type = FEEDBACK;
                                    break;
                            }
                            consumed = true;
                        }
                        break;
                }
                break;
            case CONTEXT_NUMERICAL_ANSWERLIST:
                switch (type) {
                    case NUMBER:
                        context = CONTEXT_NUMERICAL_ANSWER;
                        break;
                    case OPERATOR:
                        if (!consumed) {
                            start = start + charlength;
                            charlength = 1;
                            switch (c) {
                                case '=':
                                case '~':
                                    context = CONTEXT_NUMERICAL_ANSWERBEGIN;
                                    break;
                                case '.':
                                case '-':
                                    context = CONTEXT_NUMERICAL_ANSWER;
                                    break;
                                case '%':
                                    context = CONTEXT_NUMERICAL_ANSWERWEIGHT;
                                    break;
                                case '}':
                                    context = CONTEXT_QUESTION;
                                    break;
                                default:
                                    type = bad(type);
                                    break;
                            }
                            consumed = true;
                        }
                        break;
                }
                break;
            case CONTEXT_NUMERICAL_ANSWERBEGIN:
                switch (type) {
                    case NUMBER:
                        context = CONTEXT_NUMERICAL_ANSWER;
                        break;
                    case WORD:
                        type = bad(type);
                        context = CONTEXT_NUMERICAL_ANSWER;
                        break;
                    case OPERATOR:
                        if (!consumed) {
                            start = start + charlength;
                            charlength = 1;
                            switch (c) {
                                case '-':
                                    type = NUMBER;
                                    context = CONTEXT_NUMERICAL_ANSWER;
                                    break;
                                case '%':
                                    context = CONTEXT_NUMERICAL_ANSWERWEIGHT;
                                    break;
                                case '}':
                                    type = bad(type);
                                    context = CONTEXT_QUESTION;
                                    break;
                                default:
                                    type = bad(type);
                                    break;
                            }
                            consumed = true;
                        }
                        break;
                }
                break;
            case CONTEXT_NUMERICAL_ANSWERWEIGHT:
                switch (type) {
                    case NUMBER:
                        type = OPERATOR;
                        break;
                    case WORD:
                        context = CONTEXT_NUMERICAL_ANSWER;
                        type = bad(type);
                        break;
                    case OPERATOR:
                        if (!consumed) {
                            start = start + charlength;
                            charlength = 1;
                            switch (c) {
                                case '%':
                                    context = CONTEXT_NUMERICAL_ANSWER;
                                    break;
                                case '-':
                                    type = NUMBER;
                                    break;
                                case '}':
                                    type = bad(type);
                                    context = CONTEXT_QUESTION;
                                    break;
                                default:
                                    type = bad(type);
                                    context = CONTEXT_NUMERICAL_ANSWER;
                                    break;
                            }
                            consumed = true;
                        }
                        break;
                }
                break;
            case CONTEXT_NUMERICAL_ANSWER:
                switch (type) {
                    case WORD:
                        type = bad(type);
                        break;
                    case OPERATOR:
                        if (!consumed) {
                            start = start + charlength;
                            charlength = 1;
                            switch (c) {
                                case '=':
                                case '~':
                                    context = CONTEXT_NUMERICAL_ANSWERBEGIN;
                                    consumed = true;
                                    break;
                                case '#':
                                    context = CONTEXT_NUMERICAL_ANSWERFEEDBACK;
                                    consumed = true;
                                    break;
                                case ':':
                                    consumed = true;
                                    break;
                                case '-':
                                    type = NUMBER;
                                    consumed = true;
                                    break;
                                case '.':
                                    if (start >= buffer.length) {
                                        type = NUMBER;
                                        break;
                                    } else {
                                        c = buffer[start];
                                        charlength = 1;
                                        int k = getKind(c);
                                        if (c == '\\') {
                                            c = next();
                                            k = ANSWER;
                                        }
                                        if (c == '.') {
                                            start = start + charlength;
                                            charlength = 1;
                                        } else {
                                            type = bad(type);
                                        }
                                        consumed = true;
                                        break;
                                    }
                                case '}':
                                    context = CONTEXT_QUESTION;
                                    consumed = true;
                                    break;
                                default:
                                    type = bad(type);
                                    consumed = true;
                                    break;
                            }
                        }
                        break;
                }
                break;
            case CONTEXT_NUMERICAL_ANSWERFEEDBACK:
                switch (type) {
                    case NUMBER:
                    case WORD:
                        type = FEEDBACK;
                        break;
                    case OPERATOR:
                        if (!consumed) {
                            start = start + charlength;
                            charlength = 1;
                            switch (c) {
                                case '=':
                                case '~':
                                    context = CONTEXT_NUMERICAL_ANSWERBEGIN;
                                    break;
                                case '}':
                                    context = CONTEXT_QUESTION;
                                    break;
                                case '{':
                                    type = bad(type);
                                    break;
                                default:
                                    type = FEEDBACK;
                                    break;
                            }
                            consumed = true;
                        }
                        break;
                }
                break;
            default:
                throw new InternalError("GIFTScanner unknown context:" + context);
        }
        if (!consumed) {
            start = start + charlength;
            charlength = 1;
        }

        return type;
        /*
        switch (type) {
        case WHITESPACE :
        start = start + charlength;
        charlength = 1;
        while (start < end) {
        c = buffer[start];
        if (c == '\\') {
        c = next();
        }
        int k = getKind(c);
        if (k != type) {
        break;
        }
        start = start + charlength;
        charlength = 1;
        }
        break;
        case NEWLINE :
        start = start + charlength;
        int linecount = 1;
        charlength = 1;
        while (start < end) {
        c = buffer[start];
        if (c == '\\')
        c = next();
        int k = getKind(c);
        if (k != NEWLINE && k != WHITESPACE) {
        break;
        }
        start = start + charlength;
        charlength = 1;
        if (k == NEWLINE) {
        linecount++;
        }
        }
        if (linecount > 1 && (context == CONTEXT_QUESTION)) {
        type = QUESTION_SEPARATOR;
        }
        break;
        case OPERATOR:
        start = start + charlength;
        charlength = 1;
        type = readOperator(c);
        break;
        case COMMENT :
        start = start + charlength;
        charlength = 1;
        type = readSlash();
        break;
        case ANSWERLIST_BEGIN :
        start = start + charlength;
        charlength = 1;
        break;
        case ANSWERLIST_END :
        start = start + charlength;
        charlength = 1;
        break;
        default:
        start = start + charlength;
        charlength = 1;
        while (start < end) {
        c = buffer[start];
        int k = getKind(c);
        if (c == '\\') {
        c = next();
        k = WORD;
        }
        if (k != type) {
        break;
        }
        start = start + charlength;
        charlength = 1;
        }
        break;
        }
        // Handle context sensitive tokens
        switch (type) {
        case COMMENT :
        break;
        case ANSWERLIST_BEGIN :
        if (context != CONTEXT_QUESTION_BEGIN &&
        context != CONTEXT_QUESTION &&
        context != CONTEXT_ANSWER_END &&
        context != CONTEXT_TITLE_END) {
        type = UNRECOGNIZED;
        }
        context = CONTEXT_ANSWER;
        break;
        case ANSWERLIST_END :
        if (context == CONTEXT_QUESTION) {
        type = UNRECOGNIZED;
        }
        context = CONTEXT_ANSWER_END;
        break;
        case TITLE_BEGIN :
        if (context != CONTEXT_QUESTION_BEGIN) {
        switch (context) {
        case CONTEXT_TITLE :
        type = TITLE;
        break;
        case CONTEXT_QUESTION :
        case CONTEXT_ANSWER_END :
        type = QUESTION;
        break;
        case CONTEXT_ANSWER :
        case CONTEXT_TEXT_ANSWER :
        type = ANSWER;
        context = CONTEXT_TEXT_ANSWER;
        break;
        case CONTEXT_NUMERICAL_ANSWER :
        type = NUMBER;
        break;
        }
        } else {
        context = CONTEXT_TITLE;
        }
        break;
        case TITLE_END :
        if (context != CONTEXT_TITLE) {
        type = UNRECOGNIZED;
        }
        context = CONTEXT_TITLE_END;
        break;
        case NUMERIC_OPERATOR :
        if (context == CONTEXT_ANSWER) {
        context = CONTEXT_NUMERICAL_ANSWER;
        } else {
        type = UNRECOGNIZED;
        }
        break;
        case WHITESPACE :
        break;
        case NEWLINE :
        break;
        case QUESTION_SEPARATOR :
        if (context == CONTEXT_ANSWER || context == CONTEXT_NUMERICAL_ANSWER) {
        type = NEWLINE;
        } else {
        context = CONTEXT_QUESTION_BEGIN;
        }
        break;
        case OPERATOR :
        if (context == CONTEXT_QUESTION ||
        context == CONTEXT_QUESTION_BEGIN) {
        type = UNRECOGNIZED;
        }
        break;
        case FEEDBACK :
        if (context == CONTEXT_QUESTION) {
        type = UNRECOGNIZED;
        }
        break;
        case SEPARATOR :
        if (context == CONTEXT_QUESTION || context == CONTEXT_QUESTION_BEGIN ||
        context == CONTEXT_TITLE ||  context == CONTEXT_TITLE_END) {
        type = WORD;
        } else if (context == CONTEXT_ANSWER) {
        context = CONTEXT_TEXT_ANSWER;
        }
        break;
        case NUMBER :
        switch (context) {
        case CONTEXT_TITLE :
        type = TITLE;
        break;
        case CONTEXT_QUESTION_BEGIN :
        case CONTEXT_QUESTION :
        case CONTEXT_ANSWER_END :
        type = QUESTION;
        break;
        case CONTEXT_ANSWER :
        case CONTEXT_TEXT_ANSWER :
        type = ANSWER;
        context = CONTEXT_TEXT_ANSWER;
        break;
        case CONTEXT_NUMERICAL_ANSWER :
        type = NUMBER;
        break;
        }
        break;
        case WORD :
        switch (context) {
        case CONTEXT_TITLE :
        type = TITLE;
        break;
        case CONTEXT_TITLE_END :
        context = CONTEXT_QUESTION;
        type = QUESTION;
        break;
        case CONTEXT_QUESTION_BEGIN :
        type = QUESTION;
        context = CONTEXT_QUESTION;
        break;
        case CONTEXT_QUESTION :
        type = QUESTION;
        break;
        case CONTEXT_ANSWER :
        if (isSymbol(LITERAL, new String(buffer, begin, start - begin))) {
        type = LITERAL;
        } else {
        type = ANSWER;
        }
        context = CONTEXT_TEXT_ANSWER;
        break;
        case CONTEXT_TEXT_ANSWER :
        type = ANSWER;
        break;
        case CONTEXT_NUMERICAL_ANSWER :
        type = UNRECOGNIZED;
        break;
        case CONTEXT_ANSWER_END :
        context = CONTEXT_QUESTION;
        type = QUESTION;
        break;
        }
        break;
        }
        state = type;
        return type;
         */
    }
    // Read one line of a /*...*/ comment, given the expected type

    int readComment(int type) {
        if (start >= end) {
            return type;
        }
        char c = buffer[start];
        if (c == '\\') {
            c = next();
        }

        while (true) {
            while (/*c != '*' &&*/c != '\n') {
                start = start + charlength;
                charlength = 1;
                if (start >= end) {
                    return type;
                }
                c = buffer[start];
                if (c == '\\') {
                    c = next();
                }
            }
            start = start + charlength;
            charlength = 1;
            if (c == '\n') {
                return type;
            }
            if (start >= end) {
                return type;
            }
            c = buffer[start];
            if (c == '\\') {
                c = next();
            }
            if (c == '/') {
                start = start + charlength;
                charlength = 1;
                if (type == START_COMMENT) {
                    return COMMENT;
                } else {
                    return END_COMMENT;
                }
            }
        }
    }

    private int readSlash() {
        if (start >= end) {
            return OPERATOR;
        }
        char c = buffer[start];
        if (c == '\\') {
            c = next();
        }
        if (c == '/') {
            while (c != '\n') {
                start = start + charlength;
                charlength = 1;
                if (start >= end) {
                    return COMMENT;
                }
                c = buffer[start];
                if (c == '\\') {
                    c = next();
                }
            }
            //start = start + charlength;
            charlength = 1;
            return COMMENT;
        } /*else if (c == '*') {
        start = start + charlength;
        charlength = 1;
        return readComment(START_COMMENT);
        }*/
        return WORD;//readOperator('/');
    }

    /**
     * Detects and consumes the title operator "::".
     *
     * @param c the first operator character ':'
     * @return OPERATOR, if the operator is a title operator.
     * WORD, in all other cases.
     */
    private int readTitleOperator(char c) {
        if (start >= end) {
            return WORD;
        }
        char c2;

        switch (c) {
            case ':':
                c2 = buffer[start];
                if (c2 != ':') {
                    break;
                }
                start = start + charlength;
                charlength = 1;
                return OPERATOR;
        }
        return WORD;
    }

    /**
     * Detects and consumes the question separator "\n" (Whitespace)* "\n".
     *
     * @param c the first operator character '\n'
     * @return QUESTION_SEPARATOR, if the operator is a title operator.
     * WHITESPACE, in all other cases.
     */
    private int readQuestionSeparator(char c) {
        if (start >= end) {
            return WHITESPACE;
        }
        int linecount = 1;
        while (start < end) {
            c = buffer[start];
            int k = getKind(c);
            if (c == '\\') {
                c = next();
                k = WORD;
            }
            if (k != WHITESPACE && k != NEWLINE) {
                break;
            }
            if (k == NEWLINE) {
                linecount++;
            }
            start = start + charlength;
            charlength = 1;
        }
        return linecount <= 1 ? WHITESPACE : QUESTION_SEPARATOR;
    }

    private int readExternal() {
        if (start >= end) {
            return LITERAL;
        }
        char c = buffer[start];
        while (c != '}') {
            start = start + charlength;
            charlength = 1;
            if (start >= end) {
                return LITERAL;
            }
            c = buffer[start];
            if (c == '\\') {
                c = next();
            }
        }
        start = start + charlength - 1;
        charlength = 1;
        return LITERAL;
    }
    /*private int readFeedback() {
    if (start >= end) {
    return FEEDBACK;
    }
    char c = buffer[start];
    while (c != '~' && c != '=' && c != '#' && c != '{' && c != '}') {
    start = start + charlength;
    charlength = 1;
    if (start >= end) {
    return FEEDBACK;
    }
    c = buffer[start];
    if (c == '\\')
    c = next();
    }
    start = start + charlength - 1;
    charlength = 1;
    return FEEDBACK;
    }*/
    /*
    private int readOperator(char c) {
    if (start >= end)
    return OPERATOR;
    char c2;
    switch (c) {
    case ':':
    c2 = buffer[start];
    if (c2 == '\\') {
    c2 = next();
    }
    if (c2 != ':') {
    return NUMBER;
    //break;
    }
    start = start + charlength;
    charlength = 1;
    switch (context) {
    case CONTEXT_TITLE :
    return TITLE_END;
    default :
    return TITLE_BEGIN;
    }
    // break; <- not reached
    case '-':
    c2 = buffer[start];
    if (c2 == '\\') {
    c2 = next();
    }
    if (c2 != '>') {
    return NUMBER;
    //break;
    }
    start = start + charlength;
    charlength = 1;
    if (context == CONTEXT_NUMERICAL_ANSWER) {
    return UNRECOGNIZED;
    }
    break;
    case '.':
    c2 = buffer[start];
    if (c2 == '\\') {
    c2 = next();
    }
    if (c2 != '.') {
    return NUMBER;
    //break;
    }
    start = start + charlength;
    charlength = 1;
    return NUMBER;
    //break;
    case '#':
    if (context == CONTEXT_ANSWER) {
    return NUMERIC_OPERATOR;
    } else if (context == CONTEXT_QUESTION_BEGIN ||
    context == CONTEXT_QUESTION ||
    context == CONTEXT_TITLE ||
    context == CONTEXT_TITLE_END) {
    return WORD;
    } else if (state == FEEDBACK) {
    return UNRECOGNIZED;
    } else {
    return readFeedback();
    }
    //break; <- not reached
    case '>':
    return WORD;
    case '%':
    if (context == CONTEXT_ANSWER) {
    return readExternal();
    }
    break;
    }
    return OPERATOR;
    }
     */

    /**
     * Create the initial symbol table.
     */
    protected void initSymbolTable() {
        lookup(LITERAL, "TRUE");
        lookup(LITERAL, "FALSE");
        lookup(LITERAL, "T");
        lookup(LITERAL, "F");
    }
    /*
    private char next() {
    if (start >= end) {
    return '\u0026'; //EOF character
    }
    char c = buffer[start];
    if (c != '\\') {
    start++;
    return c;
    }
    if (start == pair) {
    pair = 0;
    start++;
    return '\\';
    }
    if (start + 1 >= end) {
    start++;
    return '\\';
    }
    c = buffer[start + 1];
    if (c == '\\') {
    start++;
    pair = start;
    }
    return '\\';
    }*/

    /**
     * A malformed or incomplete token has a negative type.
     */
    private int bad(int type) {
        return -type;
    }

    private int getKind(char c) {
        return (c < 128) ? kind[c] : unikind[Character.getType(c)];
    }

    private void initKind() {
        for (char c = 0; c < 128; c++) {
            kind[c] = -1;
        }
        for (char c = 0; c < 128; c++) {
            switch (c) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                case 11:
                case 13:
                case 14:
                case 15:
                case 16:
                case 17:
                case 18:
                case 19:
                case 20:
                case 21:
                case 22:
                case 23:
                case 24:
                case 25:
                case 27:
                case 28:
                case 29:
                case 30:
                case 31:
                case 127:
                    kind[c] = UNRECOGNIZED;
                    break;
                case '\t':
                    kind[c] = WHITESPACE;
                    break;
                case '\n':
                    kind[c] = NEWLINE;
                    break;
                case ' ':
                case '\f':
                case 26:
                    kind[c] = WHITESPACE;
                    break;
                case '#':
                case '%':
                case '-':
                case '>':
                case ':':
                case '.':
                case '\\':
                case '=':
                case '~':
                    kind[c] = OPERATOR;
                    break;
                case '/':
                    kind[c] = COMMENT;
                    break;
                case '\'':
                case '"':
                case ',':
                case ';':
                case '?':
                case '^':
                case '|':
                case '<':
                case '&':
                case '*':
                case '+':
                case '@':
                case '`':
                case '!':
                case '$':
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                case 'Z':
                case '_':
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                case 'g':
                case 'h':
                case 'i':
                case 'j':
                case 'k':
                case 'l':
                case 'm':
                case 'n':
                case 'o':
                case 'p':
                case 'q':
                case 'r':
                case 's':
                case 't':
                case 'u':
                case 'v':
                case 'w':
                case 'x':
                case 'y':
                case 'z':
                case '(':
                case ')':
                case '[':
                case ']':
                    kind[c] = WORD;
                    break;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    kind[c] = NUMBER;
                    break;
                case '{':
                    kind[c] = OPERATOR;
                    break;
                case '}':
                    kind[c] = OPERATOR;
                    break;
            }
        }
        for (char c = 0; c < 128; c++) {
            if (kind[c] == -1) {
                System.out.println("Char " + ((int) c) + " hasn't been classified");
            }
        }
    }

    private void initUniKind() {
        for (byte b = 0; b < 31; b++) {
            unikind[b] = -1;
        }
        for (byte b = 0; b < 31; b++) {
            switch (b) {
                case Character.UNASSIGNED:
                case Character.ENCLOSING_MARK:
                case Character.OTHER_NUMBER:
                case Character.SPACE_SEPARATOR:
                case Character.LINE_SEPARATOR:
                case Character.PARAGRAPH_SEPARATOR:
                case Character.CONTROL:
                case 17: // category 17 is unused
                case Character.PRIVATE_USE:
                case Character.SURROGATE:
                case Character.DASH_PUNCTUATION:
                case Character.START_PUNCTUATION:
                case Character.END_PUNCTUATION:
                case Character.OTHER_PUNCTUATION:
                case Character.MATH_SYMBOL:
                case Character.MODIFIER_SYMBOL:
                case Character.OTHER_SYMBOL:
                case Character.INITIAL_QUOTE_PUNCTUATION:
                case Character.FINAL_QUOTE_PUNCTUATION:
                    unikind[b] = WORD;
                    break;
                case Character.UPPERCASE_LETTER:
                case Character.LOWERCASE_LETTER:
                case Character.TITLECASE_LETTER:
                case Character.MODIFIER_LETTER:
                case Character.OTHER_LETTER:
                case Character.LETTER_NUMBER:
                case Character.CONNECTOR_PUNCTUATION: // maybe NUMBER
                case Character.CURRENCY_SYMBOL:
                    // Characters where Other_ID_Start is true
                    unikind[b] = WORD;
                    break;
                case Character.NON_SPACING_MARK:
                case Character.COMBINING_SPACING_MARK:
                case Character.DECIMAL_DIGIT_NUMBER:
                case Character.FORMAT:
                    unikind[b] = WORD;
                    break;
            }
        }
        for (byte b = 0; b < 31; b++) {
            if (unikind[b] == -1) {
                System.out.println("Unicode cat " + b + " hasn't been classified");
            }
        }
    }
}
