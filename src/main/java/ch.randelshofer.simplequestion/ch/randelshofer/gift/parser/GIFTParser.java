/* @(#)GIFTParser.java
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

import org.jhotdraw.io.StreamPosTokenizer;
import org.jhotdraw.util.ResourceBundleUtil;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import static org.jhotdraw.io.StreamPosTokenizer.TT_EOF;
import static org.jhotdraw.io.StreamPosTokenizer.TT_EOL;
import static org.jhotdraw.io.StreamPosTokenizer.TT_WORD;

/**
 * A parser for questions in the Moodle GIFT Format.
 *
 * @author Werner Randelshofer
 * @version 1.3.1 2008-12-03 The character immediately following a colon in a
 * question was suppressed due to the next token being looked ahead but not
 * being pushed back.
 * <br>1.3 2008-02-22 Fixed endless loops in parseExternalAnswerList and
 * in parseNumericalAnswer.
 * <br>1.2 2006-12-12 Automatic generation of question id added.
 * <br>1.1 2006-08-13 Store start and end positions in Question objects.
 * <br>1.0.3 2006-07-24 Fixed whitespace handling around '\' escape character.
 * <br>1.0.2 2006-07-04 Fixed negative numerical number answer issue.
 * <br>1.0.1 2006-06-26 Fixed ':' parsing issue in title.
 * <br>1.0 19. April 2006 Created.
 */
public class GIFTParser {

    private ResourceBundleUtil labels;

    /**
     * Creates a new instance.
     */
    public GIFTParser() {
        labels = new ResourceBundleUtil(ResourceBundle.getBundle("ch/randelshofer/gift/Labels"));
    }

    /**
     * Parses a GIFT text.
     */
    public List<Question> parse(String text) throws IOException {
        StreamPosTokenizer st = new StreamPosTokenizer(new StringReader(text));
        st.resetSyntax();
        st.whitespaceChars(0, 31);
        st.ordinaryChar(' ');
        st.wordChars('!', '"');
        st.ordinaryChar('#');
        st.wordChars('$', '$');
        st.ordinaryChar('%');
        st.wordChars('&', ',');
        st.ordinaryChar('-');
        st.ordinaryChar('.');
        st.ordinaryChar('/');
        st.wordChars('0', '<');
        st.ordinaryChar('=');
        st.ordinaryChar('>');
        st.wordChars('?', '[');
        st.ordinaryChar('\\');
        st.wordChars(']', 'z');
        st.ordinaryChar('{');
        st.wordChars('|', '|');
        st.ordinaryChar('}');
        st.wordChars(0x00a1, 0xffff);
        st.ordinaryChar('~');
        st.ordinaryChar(':');
        st.slashSlashComments(true);
        st.eolIsSignificant(true);

        LinkedList<Question> result = new LinkedList<Question>();

        while (st.nextToken() != TT_EOF) {
            switch (st.ttype) {
                case TT_EOL:
                    // we consume the end of line
                    break;
                default:
                    st.pushBack();
                    skipWhitespace(st);
                    Question q = parseQuestion(st);
                    //System.out.println(q);
                    //System.out.flush();
                    result.add(q);
                    q.setId("" + result.size());
                    break;
            }
        }

        return result;
    }

    /**
     * Parses a single GIFT question.
     * <p>
     * A question has the following EBNF syntax productions:
     * <p>
     * question ::= ["::" title ] { text | "{" answerList } (EOL EOL | EOF)
     * title ::= [{word}] "::"
     * text ::= {word}
     * answerList ::= {answer} "}"
     */
    private Question parseQuestion(StreamPosTokenizer st) throws IOException {
        Question question = new Question();
        // We build question texts using this StringBuilder
        StringBuilder text = new StringBuilder();
        StringBuilder whitespace = new StringBuilder();

        // Parse the title if the question starts with two colons
        st.nextToken();
        question.setStartPosition(st.getStartPosition());
        question.setEndPosition(st.getEndPosition());

        switch (st.ttype) {
            case ':':
                if (st.nextToken() == ':') {
                    question.setTitle(parseTitle(st));
                } else {
                    text.append(':');
                    st.pushBack();
                }
                break;
            default:
                st.pushBack();
                break;
        }

        if (text.length() == 0) {
            skipWhitespace(st);
        }

        whileLoop:
        while (true) {
            question.setEndPosition(st.getEndPosition());
            switch (st.nextToken()) {
                case TT_WORD:
                    text.append(whitespace.toString());
                    whitespace.setLength(0);
                    text.append(st.sval);
                    break;
                case TT_EOL:
                    //skipWhitespace(st);
                    while (st.nextToken() == ' ') {
                    }
                    st.pushBack();
                    if (st.nextToken() == TT_EOL) {
                        break whileLoop;
                    } else {
                        whitespace.append('\n');
                        st.pushBack();
                    }
                    break;
                case TT_EOF:
                    break whileLoop;
                case '\\':
                    // Treat next special char like ordinary char
                    if (st.nextToken() >= 0) {
                        text.append(whitespace.toString());
                        whitespace.setLength(0);
                        text.append((char) st.ttype);
                    } else {
                        //st.pushBack();
                        //text.append('\\');
                        throw new ch.randelshofer.io.ParseException(labels.getFormatted("parser.illegalEscapeSequence", st.lineno()), st.getStartPosition(), st.getEndPosition());
                    }
                    break;
                case '{':
                    int startpos = st.getStartPosition();
                    if (text.length() > 0) {
                        text.append(whitespace.toString());
                        whitespace.setLength(0);
                        question.addQuestionText(eliminateFullStopLines(text.toString()));
                        text.setLength(0);
                    }
                    AnswerList answerList = parseAnswerList(st);
                    if (answerList.answers().size() == 0) {
                        throw new ch.randelshofer.io.ParseException(labels.getFormatted("parser.emptyAnswerList", st.lineno()), startpos, st.getEndPosition());
                    }
                    question.addAnswerList(answerList);
                    break;
                case '}':
                    throw new ch.randelshofer.io.ParseException(labels.getFormatted("parser.illegalCharacter", (char) st.ttype, st.lineno()), st.getStartPosition(), st.getEndPosition());
                case ':':
                    if (st.nextToken() == ':') {
                        throw new ch.randelshofer.io.ParseException(labels.getFormatted("parser.illegalCharacter", (char) st.ttype, st.lineno()), st.getStartPosition(), st.getEndPosition());
                    }
                    st.pushBack();
                    text.append(whitespace.toString());
                    whitespace.setLength(0);
                    text.append(':');
                    break;
                default:
                    if (st.ttype <= ' ') {
                        whitespace.append((char) st.ttype);
                    } else {
                        text.append(whitespace.toString());
                        whitespace.setLength(0);
                        text.append((char) st.ttype);
                    }
                    break;
            }
        }
        if (text.length() > 0) {
            text.append(whitespace.toString());
            whitespace.setLength(0);
            question.addQuestionText(eliminateFullStopLines(text.toString()));
        }
        return question;
    }

    /**
     * Parses a GIFT title.
     * <p>
     * A title has the following EBNF syntax productions:
     * <p>
     * title ::= [{word}] "::"
     */
    private String parseTitle(StreamPosTokenizer st) throws IOException {
        StringBuilder title = new StringBuilder();

        whileLoop:
        while (true) {
            switch (st.nextToken()) {
                case TT_WORD:
                    title.append(st.sval);
                    break;
                case TT_EOL:
                    title.append('\n');
                    break;
                case TT_EOF:
                    throw new ch.randelshofer.io.ParseException(labels.getFormatted("parser.unexpectedEOFInTitle", st.lineno()), st.getStartPosition(), st.getEndPosition());
                    //break; not reached
                case '\\':
                    // Treat next special char like ordinary char
                    if (st.nextToken() >= 0) {
                        title.append((char) st.ttype);
                    } else {
                        throw new ch.randelshofer.io.ParseException(labels.getFormatted("parser.illegalEscapeSequence", st.lineno()), st.getStartPosition(), st.getEndPosition());
                    }
                    break;
                case ':':
                    if (st.nextToken() == ':') {
                        break whileLoop;
                    } else {
                        title.append(':');
                        st.pushBack();
                    }
                    break;
                case '{':
                case '}':
                    throw new ch.randelshofer.io.ParseException(labels.getFormatted("parser.illegalCharacter", (char) st.ttype, st.lineno()), st.getStartPosition(), st.getEndPosition());
                default:
                    title.append((char) st.ttype);
                    break;
            }
        }

        return title.toString();
    }

    /**
     * Parses an answer list.
     * <p>
     * A answerList has the following EBNF syntax productions:
     * <p>
     * answerList ::= textualAnswerList | "#" numericalAnswerList | "%" externalAnswerList
     * textualAnswerList = {textualAnswer} "}"
     * numericalAnswerList = {numericalAnswer} "}"
     */
    private AnswerList parseAnswerList(StreamPosTokenizer st) throws IOException {
        skipWhitespace(st);

        AnswerList answerList;
        switch (st.nextToken()) {
            case '%':
                answerList = parseExternalAnswerList(st);
                break;
            case '#':
                answerList = parseNumericalAnswerList(st);
                break;
            case '\\':
                st.pushBack();
                answerList = parseTextualAnswerList(st);
                break;
            default:
                st.pushBack();
                answerList = parseTextualAnswerList(st);
                break;
        }
        return answerList;
    }

    /**
     * Parses a text answer list.
     * <p>
     * A textualAnswerList has the following EBNF syntax productions:
     * <p>
     * textualAnswerList = {textualAnswer} "}"
     */
    private AnswerList parseTextualAnswerList(StreamPosTokenizer st) throws IOException {
        AnswerList answerList = new AnswerList();
        StringBuilder text = new StringBuilder();

        whileLoop:
        while (true) {
            switch (st.nextToken()) {
                case '}':
                    break whileLoop;
                default:
                    st.pushBack();
                    int startPosition = st.getStartPosition();
                    int lineno = st.lineno();
                    Answer answer = parseTextualAnswer(st);
                    int endPosition = st.getEndPosition();
                    if (!answerList.canBeInGroup(answer)) {
                        throw new ch.randelshofer.io.ParseException(labels.getFormatted("parser.illegalAnswerCombination", answer.toString(), lineno), startPosition, endPosition);
                    }
                    answerList.add(answer);
                    break;
            }
        }
        return answerList;
    }

    /**
     * Parses a numerical answer list.
     * <p>
     * A numericalAnswerList has the following EBNF syntax productions:
     * <p>
     * numericalAnswerList = {numericalAnswer} "}"
     */
    private AnswerList parseNumericalAnswerList(StreamPosTokenizer st) throws IOException {
        AnswerList answerList = new AnswerList();
        StringBuilder text = new StringBuilder();

        whileLoop:
        while (true) {
            switch (st.nextToken()) {
                case StreamPosTokenizer.TT_EOF:
                    throw new ch.randelshofer.io.ParseException(labels.getFormatted("parser.unexpectedEOF", st.lineno()), st.getStartPosition(), st.getEndPosition());

                case '}':
                    break whileLoop;
                default:
                    st.pushBack();
                    answerList.add(parseNumericalAnswer(st));
                    break;
            }
        }
        return answerList;
    }

    /**
     * Parses an external answer list.
     * <p>
     * An externalAnswerList has the following EBNF syntax productions:
     * <p>
     * externalAnswerList = externalAnswer "}"
     */
    private AnswerList parseExternalAnswerList(StreamPosTokenizer st) throws IOException {
        AnswerList answerList = new AnswerList();
        StringBuilder text = new StringBuilder();

        whileLoop:
        while (true) {
            switch (st.nextToken()) {
                case StreamPosTokenizer.TT_EOF:
                    throw new ch.randelshofer.io.ParseException(labels.getFormatted("parser.unexpectedEOF", st.lineno()), st.getStartPosition(), st.getEndPosition());
                case '}':
                    break whileLoop;
                default:
                    st.pushBack();
                    answerList.add(parseExternalAnswer(st));
                    break;
            }
        }
        return answerList;
    }

    /**
     * Parses a textual answer.
     * <p>
     * A textualAnswer has the following EBNF syntax productions:
     * <p>
     * textualAnswer ::= booleanAnswer | choiceAnswer | matchingPairAnswer
     * booleanAnswer ::= "T" | "F" | "TRUE" | "FALSE" ["#" feedbackComment]
     * choiceAnswer ::= ("~"|"=") weight {word} ["#" feedbackComment]
     * matchingPairAnswer ::= ("=") weight {word} "-" ">" {word} ["#" feedbackComment]
     * weight ::= ["%" number "%"]
     */
    private Answer parseTextualAnswer(StreamPosTokenizer st) throws IOException {
        Answer answer;

        skipWhitespace(st);

        switch (st.nextToken()) {
            case TT_WORD:
                String w = st.sval;
                if (w.equals("T") || w.equals("TRUE")) {
                    answer = new BooleanAnswer(true);
                } else if (w.equals("F") || w.equals("FALSE")) {
                    answer = new BooleanAnswer(false);
                } else {
                    st.pushBack();
                    answer = parseChoiceAnswerOrMatchingPairAnswer(st);
                }
                skipWhitespace(st);
                if (st.nextToken() == '#') {
                    answer.setFeedbackText(parseFeedbackComment(st));
                } else {
                    st.pushBack();
                }

                break;

            case '~':
            case '=':
                st.pushBack();
                answer = parseChoiceAnswerOrMatchingPairAnswer(st);
                break;
            case '\\':
                st.pushBack();
                answer = parseChoiceAnswerOrMatchingPairAnswer(st);
                break;

            default:
                throw new ch.randelshofer.io.ParseException(labels.getFormatted("parser.illegalCharacter", (char) st.ttype, st.lineno()), st.getStartPosition(), st.getEndPosition());
        }
        return answer;
    }

    /**
     * Parses a choice or a matching pair answer.
     * <p>
     * A choice answer has the following EBNF syntax productions:
     * <p>
     * choiceAnswer ::= ["~"|"="] weight {word} ["#" feedbackComment]
     * matchingPairAnswer ::= ("=") weight {word} "-" ">" {word} ["#" feedbackComment]
     * weight ::= ["%" number "%"]
     * feedbackComment ::= {word} // until "=","~","}"
     */
    private Answer parseChoiceAnswerOrMatchingPairAnswer(StreamPosTokenizer st) throws IOException {
        boolean isCorrect;
        boolean isMatchingPairAnswer = false;
        int correctnessTokenStartPos = -1;
        int correctnessTokenEndPos = -1;
        int correctnessTokenLineno = -1;
        char correctnessToken = '\0';

        skipWhitespace(st);

        switch (st.nextToken()) {
            case '~':
                correctnessTokenStartPos = st.getStartPosition();
                correctnessTokenEndPos = st.getEndPosition();
                correctnessTokenLineno = st.lineno();
                correctnessToken = (char) st.ttype;
                isCorrect = false;
                break;
            case '=':
                correctnessTokenStartPos = st.getStartPosition();
                correctnessTokenEndPos = st.getEndPosition();
                correctnessTokenLineno = st.lineno();
                correctnessToken = (char) st.ttype;
                isCorrect = true;
                break;
            case '\\':
                st.pushBack();
                isCorrect = true;
                break;
            case TT_WORD:
                isCorrect = true;
                st.pushBack();
                //throw new ch.randelshofer.io.ParseException("Illegal choice answer \""+st.sval+"\" at "+st.lineno(), st.getStartPosition(), st.getEndPosition());
                break;
            default:
                throw new ch.randelshofer.io.ParseException(labels.getFormatted("parser.illegalCharacter", (char) st.ttype, st.lineno()), st.getStartPosition(), st.getEndPosition());
        }

        int weight = parseWeight(st);

        StringBuilder text = new StringBuilder();
        StringBuilder whitespace = new StringBuilder();

        whileLoop:
        while (true) {
            switch (st.nextToken()) {
                case TT_WORD:
                    text.append(whitespace.toString());
                    whitespace.setLength(0);
                    text.append(st.sval);
                    break;
                case TT_EOL:
                    whitespace.append('\n');
                    break;
                case TT_EOF:
                    throw new ch.randelshofer.io.ParseException(labels.getFormatted("parser.unexpectedEOFInChoice", st.lineno()), st.getStartPosition(), st.getEndPosition());
                    //break; not reached
                case '\\':
                    // Treat next special char like ordinary char
                    if (st.nextToken() >= 0) {
                        text.append(whitespace.toString());
                        whitespace.setLength(0);
                        text.append((char) st.ttype);
                    } else {
                        st.pushBack();
                        //throw new ch.randelshofer.io.ParseException(labels.getFormatted("parser.illegalEscapeSequence", st.lineno()), st.getStartPosition(), st.getEndPosition());
                    }
                    break;
                case '-':
                    if (st.nextToken() == '>') {
                        isMatchingPairAnswer = true;
                        if (text.length() == 0) {
                            throw new ch.randelshofer.io.ParseException(labels.getFormatted("parser.missingMatchingPairText", st.lineno()), st.getStartPosition() - 1, st.getEndPosition());
                        }
                        skipWhitespace(st);
                        break whileLoop;
                    } else {
                        text.append('-');
                        st.pushBack();
                        break;
                    }
                case '~':
                case '=':
                case '}':
                case '#':
                    st.pushBack();
                    break whileLoop;
                case ' ':
                    whitespace.append(' ');
                    break;
                case '{':
                case '%':
                    throw new ch.randelshofer.io.ParseException(labels.getFormatted("parser.illegalCharacter", (char) st.ttype, st.lineno()), st.getStartPosition(), st.getEndPosition());
                default:
                    text.append(whitespace.toString());
                    whitespace.setLength(0);
                    text.append((char) st.ttype);
                    break;
            }
        }

        Answer theAnswer;

        if (isMatchingPairAnswer) {
            if (!isCorrect) {
                throw new ch.randelshofer.io.ParseException(labels.getFormatted("parser.illegalMatchingPair", "" + correctnessToken, correctnessTokenLineno), correctnessTokenStartPos, correctnessTokenEndPos);
            }

            MatchingPairAnswer answer = new MatchingPairAnswer();
            answer.setWeight(weight);
            answer.setKey(text.toString());

            text.setLength(0);
            whitespace.setLength(0);

            whileLoop:
            while (true) {
                switch (st.nextToken()) {
                    case TT_WORD:
                        text.append(whitespace.toString());
                        whitespace.setLength(0);
                        text.append(st.sval);
                        break;
                    case TT_EOL:
                        whitespace.append('\n');
                        break;
                    case TT_EOF:
                        throw new ch.randelshofer.io.ParseException(labels.getFormatted("parser.unexpectedEOFInMatchingPair", st.lineno()), st.getStartPosition(), st.getEndPosition());
                        //break; not reatched
                    case '\\':
                        // Treat next special char like ordinary char
                        if (st.nextToken() >= 0) {
                            text.append(whitespace.toString());
                            whitespace.setLength(0);
                            text.append((char) st.ttype);
                        } else {
                            throw new ch.randelshofer.io.ParseException(labels.getFormatted("parser.illegalEscapeSequence", st.lineno()), st.getStartPosition(), st.getEndPosition());
                        }
                        break;
                    case '~':
                    case '=':
                    case '#':
                    case '}':
                        st.pushBack();
                        break whileLoop;
                    case '-':
                        if (st.nextToken() == '>') {
                            throw new ch.randelshofer.io.ParseException(labels.getFormatted("parser.illegalCharacter", "->", st.lineno()), st.getStartPosition() - 1, st.getEndPosition());
                        } else {
                            text.append('-');
                            st.pushBack();
                            break;
                        }
                    case ' ':
                        whitespace.append(' ');
                        break;
                    default:
                        text.append(whitespace.toString());
                        whitespace.setLength(0);
                        text.append((char) st.ttype);
                        break;
                }
            }
            if (text.length() == 0) {
                throw new ch.randelshofer.io.ParseException(labels.getFormatted("parser.missingMatchingPairText", st.lineno()), st.getStartPosition() - 1, st.getEndPosition());
            }
            answer.setValue(text.toString());

            theAnswer = answer;

        } else {
            if (text.length() == 0) {
                throw new ch.randelshofer.io.ParseException(labels.getFormatted("parser.missingChoiceText", st.lineno()), st.getStartPosition(), st.getEndPosition());
            }
            st.pushBack();
            ChoiceAnswer answer = new ChoiceAnswer();
            answer.setWeight(weight);
            answer.setCorrect(isCorrect);
            answer.setText(text.toString());
            theAnswer = answer;
        }

        skipWhitespace(st);
        if (st.nextToken() == '#') {
            theAnswer.setFeedbackText(parseFeedbackComment(st));
        } else {
            st.pushBack();
        }
        return theAnswer;

    }

    /**
     * Parses a numerical answer.
     * <p>
     * A numerical answer has the following EBNF syntax productions:
     * <pre>
     * numericalAnswer ::= ["="] weight numberAnswer | intervalAnswer
     * weight ::= ["%" number "%"]
     * numberAnswer ::= number ":" errorMargin ["#" feedbackComment]
     * intervalAnswer ::= number ".." number ["#" feedbackComment]
     *
     * number ::= ["-"] [{digit} "."] {digit}
     * errorMargin ::= [{digit} "."] {digit}
     * feedbackComment ::= {word} // until "=","~","}"
     * <pre>
     */
    private Answer parseNumericalAnswer(StreamPosTokenizer st) throws IOException {

        String number = "";

        skipWhitespace(st);

        // Skip '=' token
        switch (st.nextToken()) {
            case '=':
                break;
            default:
                st.pushBack();
                break;
        }

        // Parse weight
        int weight = parseWeight(st);

        // Parse sign
        switch (st.nextToken()) {
            case '-':
                number = "-";
                break;
            default:
                st.pushBack();
                break;
        }

        // Parse digits before decimal point
        int startpos = st.getStartPosition();
        switch (st.nextToken()) {
            case TT_WORD:
                number += st.sval;
                break;
            default:
                throw new ch.randelshofer.io.ParseException(labels.getFormatted("parser.illegalNumericalValue", (char) st.ttype, st.lineno()), st.getStartPosition(), st.getEndPosition());
        }

        // Parse decimal point and digits after decimal point
        // Decide whether we are parsing a IntervalAnswer or a NumberAnswer.
        Boolean isSpan = null;
        switch (st.nextToken()) {
            case '.':
                if (st.nextToken() == '.') {
                    isSpan = Boolean.TRUE;
                } else if (st.ttype == TT_WORD) {
                    number += '.' + st.sval;
                } else {
                    throw new ch.randelshofer.io.ParseException(labels.getFormatted("parser.illegalNumericalValue", (char) st.ttype, st.lineno()), startpos, st.getEndPosition());
                }
                break;
            default:
                st.pushBack();
                break;
        }

        if (isSpan == null) {
            skipWhitespace(st);

            switch (st.nextToken()) {
                case '.':
                    if (st.nextToken() == '.') {
                        isSpan = Boolean.TRUE;
                    } else {
                        throw new ch.randelshofer.io.ParseException(labels.getFormatted("parser.illegalNumericalValue", (char) st.ttype, st.lineno()), startpos, st.getEndPosition());
                    }
                    break;
                default:
                    st.pushBack();
                    break;
            }
        }

        Answer theAnswer;

        if (isSpan == Boolean.TRUE) {
            IntervalAnswer answer = new IntervalAnswer();
            answer.setWeight(weight);
            try {
                answer.setMin(Double.parseDouble(number));
            } catch (NumberFormatException e) {
                throw new ch.randelshofer.io.ParseException(labels.getFormatted("parser.illegalSpanMinValue", number, st.lineno()), st.getStartPosition(), st.getEndPosition());
            }

            number = "";

            // Parse sign
            switch (st.nextToken()) {
                case '-':
                    number = "-";
                    break;
                default:
                    st.pushBack();
                    break;
            }

            // Parse number digits before decimal point
            switch (st.nextToken()) {
                case TT_WORD:
                    number = st.sval;
                    break;
                default:
                    throw new ch.randelshofer.io.ParseException(labels.getFormatted("parser.illegalCharacter", (char) st.ttype, st.lineno()), st.getStartPosition(), st.getEndPosition());
            }

            // Parse decimal point and number digits after decimal point
            switch (st.nextToken()) {
                case '.':
                    if (st.nextToken() == TT_WORD) {
                        number += '.' + st.sval;

                    } else {
                        throw new ch.randelshofer.io.ParseException(labels.getFormatted("parser.illegalCharacter", (char) st.ttype, st.lineno()), st.getStartPosition(), st.getEndPosition());
                    }
                    break;
                default:
                    st.pushBack();
                    break;
            }

            try {
                answer.setMax(Double.parseDouble(number));
            } catch (NumberFormatException e) {
                throw new ch.randelshofer.io.ParseException(labels.getFormatted("parser.illegalSpanMaxValue", number, st.lineno()), st.getStartPosition(), st.getEndPosition());
            }

            theAnswer = answer;
        } else {
            NumberAnswer answer = new NumberAnswer();
            answer.setWeight(weight);
            try {
                answer.setNumber(Double.parseDouble(number));
            } catch (NumberFormatException e) {
                throw new ch.randelshofer.io.ParseException(labels.getFormatted("parser.illegalNumericalValue", number, st.lineno()), startpos, st.getEndPosition());
            }

            switch (st.nextToken()) {
                case ':':
                    break;
                default:
                    st.pushBack();
                    return answer;
            }

            number = "";

            // Parse sign
            switch (st.nextToken()) {
                case '-':
                    number = "-";
                    break;
                default:
                    st.pushBack();
                    break;
            }

            // Parse number digits before decimal point
            switch (st.nextToken()) {
                case TT_WORD:
                    number = st.sval;
                    break;
                default:
                    throw new ch.randelshofer.io.ParseException(labels.getFormatted("parser.illegalNumericalValue", (char) st.ttype, st.lineno()), st.getStartPosition(), st.getEndPosition());
            }

            // Parse decimal point and number digits after decimal point
            switch (st.nextToken()) {
                case '.':
                    if (st.nextToken() == TT_WORD) {
                        number += '.' + st.sval;

                    } else {
                        throw new ch.randelshofer.io.ParseException(labels.getFormatted("parser.illegalNumericalValue", (char) st.ttype, st.lineno()), st.getStartPosition(), st.getEndPosition());
                    }
                    break;
                default:
                    st.pushBack();
                    break;
            }

            try {
                answer.setErrorMargin(Double.parseDouble(number));
            } catch (NumberFormatException e) {
                throw new ch.randelshofer.io.ParseException(labels.getFormatted("parser.illegalNumericalValue", number, st.lineno()), st.getStartPosition(), st.getEndPosition());
            }


            theAnswer = answer;
        }

        skipWhitespace(st);

        if (st.nextToken() == '#') {
            theAnswer.setFeedbackText(parseFeedbackComment(st));
        } else {
            st.pushBack();
        }

        return theAnswer;
    }

    /**
     * Parses an external answer.
     * <p>
     * An external answer has the following EBNF syntax productions:
     * <pre>
     * externalAnswer ::= externalReference
     * <pre>
     */
    private Answer parseExternalAnswer(StreamPosTokenizer st) throws IOException {
        ExternalAnswer theAnswer = new ExternalAnswer();

        String externalReference = "";
        int previousTType = ':';
        int startPosition = st.getStartPosition();
        while (st.nextToken() != StreamPosTokenizer.TT_EOF &&
                st.ttype != StreamPosTokenizer.TT_EOL &&
                st.ttype != '}') {
            switch (st.ttype) {
                case StreamPosTokenizer.TT_WORD:
                    if (previousTType == StreamPosTokenizer.TT_WORD) {
                        externalReference += ' ';
                    }
                    externalReference += st.sval;
                    previousTType = st.ttype;
                    break;
                default:
                    previousTType = st.ttype;
                    externalReference += (char) st.ttype;
            }
        }
        st.pushBack();
        theAnswer.setExternalReference(externalReference);
        return theAnswer;
    }

    private void skipWhitespace(StreamPosTokenizer st) throws IOException {
        // Skip leading whitespace
        while (st.nextToken() >= 0 && st.ttype <= ' ') {
            ;
        }
        st.pushBack();
    }

    /**
     * Parses a weight.
     * <p>
     * A weight has the following EBNF syntax productions:
     * <pre>
     * weight ::= ["%" number "%"]
     * <pre>
     *
     * @return weight, or 0 if not specified
     */
    private int parseWeight(StreamPosTokenizer st) throws IOException {
        int weight = 0;

        skipWhitespace(st);
        switch (st.nextToken()) {
            case '%':
                break;
            default:
                st.pushBack();
                return 0;
            //break;
        }

        skipWhitespace(st);
        int sign = 1;
        switch (st.nextToken()) {
            case '-':
                sign = -1;
                break;
            default:
                st.pushBack();
                break;
        }

        switch (st.nextToken()) {
            case TT_WORD:
                try {
                    weight = sign * Integer.parseInt(st.sval);
                } catch (NumberFormatException e) {
                    throw new ch.randelshofer.io.ParseException(labels.getFormatted("parser.illegalPercentageValue", weight, st.lineno()), st.getStartPosition(), st.getEndPosition());
                }
                break;
            default:
                throw new ch.randelshofer.io.ParseException(labels.getFormatted("parser.missingPercentageValue", "", st.lineno()), st.getStartPosition(), st.getEndPosition());
        }

        skipWhitespace(st);
        switch (st.nextToken()) {
            case '%':
                break;
            default:
                throw new ch.randelshofer.io.ParseException(labels.getFormatted("parser.unexpectedEOFInPercentage", st.lineno()), st.getStartPosition(), st.getEndPosition());
        }

        return weight;
    }

    /**
     * Parses a feedback comment
     * <p>
     * A feedback comment has the following EBNF syntax productions:
     * <p>
     * title ::= [{word}]  // until "~", "=", "}"
     */
    private String parseFeedbackComment(StreamPosTokenizer st) throws IOException {
        StringBuilder text = new StringBuilder();
        StringBuilder whitespace = new StringBuilder();

        skipWhitespace(st);

        whileLoop:
        while (true) {
            switch (st.nextToken()) {
                case TT_WORD:
                    text.append(whitespace.toString());
                    whitespace.setLength(0);
                    text.append(st.sval);
                    break;
                case TT_EOL:
                    whitespace.append('\n');
                    break;
                case TT_EOF:
                    throw new ch.randelshofer.io.ParseException(labels.getFormatted("parser.unexpectedEOFInFeedbackComment", st.lineno()), st.getStartPosition(), st.getEndPosition());
                    //break; not reached
                case '\\':
                    // Treat next special char like ordinary char
                    if (st.nextToken() >= 0) {
                        text.append(whitespace.toString());
                        whitespace.setLength(0);
                        text.append((char) st.ttype);
                    } else {
                        throw new ch.randelshofer.io.ParseException(labels.getFormatted("parser.illegalEscapeSequence", st.lineno()), st.getStartPosition(), st.getEndPosition());
                    }
                    break;
                case '}':
                case '~':
                case '=':
                    st.pushBack();
                    break whileLoop;
                case '{':
                case '#':
                    throw new ch.randelshofer.io.ParseException(labels.getFormatted("parser.illegalCharacter", (char) st.ttype, st.lineno()), st.getStartPosition(), st.getEndPosition());
                case ' ':
                    whitespace.append(' ');
                    break;
                default:
                    text.append((char) st.ttype);
                    break;
            }
        }

        return text.toString();
    }

    /**
     * Eliminates line containing nothing but a full stop.
     */
    private String eliminateFullStopLines(String str) {
        StringBuilder buf = new StringBuilder();
        StringTokenizer tt = new StringTokenizer(str, "\n", true);
        while (tt.hasMoreTokens()) {
            String token = tt.nextToken();
            if (token.equals(".") && buf.length() > 0) {
                // skip full stops
            } else {
                buf.append(token);
            }
        }
        return buf.toString();
    }
}
