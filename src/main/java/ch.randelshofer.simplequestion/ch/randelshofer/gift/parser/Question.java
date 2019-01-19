/* @(#)Question.java
 *
 * Copyright (c) 2006-2010 Werner Randelshofer
 * Staldenmattweg 2, CH-6405 Immensee, Switzerland
 * All rights reserved.
 *
 * The copyright of this software is owned by Werner Randelshofer. 
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * Werner Randelshofer. For details see accompanying license terms.
 */

package ch.randelshofer.gift.parser;

import java.text.Normalizer;
import java.util.LinkedList;
import java.util.StringTokenizer;
/**
 * Question.
 * 
 * @author Werner Randelshofer
 * @version 1.4.1 2010-04-05 Removed dependency to IBM ICU Unicode library.
 * <br>1.4 2008-12-03 Add support for 'Description' questions, which have
 * no answer list.
 * <br>1.3 2006-12-12 Added question id attribute.
 * <br>1.2 2006-09-13 Methods isIncomplete, setStartPosition, 
 * getStartPosition, setEndPosition, getEndPosition added.
 * <br>1.1 2006-07-11 Method getDescriptiveTitle() added.
 * <br>1.0 19. April 2006 Created.
 */
public class Question {
    private String id;
    private String title;
    private LinkedList<Object> body;
    private int startPosition;
    private int endPosition;
    
    /** Creates a new instance. */
    public Question() {
        body = new LinkedList<Object>();
    }
    
    public void setTitle(String newValue) {
        title = newValue;
    }
    public String getTitle() {
        return title;
    }
    public void setId(String newValue) {
        id = newValue;
    }
    public String getId() {
        return id;
    }
    
    /**
     * Returns the title of the question. If the question does not have a
     * title, returns the first few words of the question text with an
     * ellipsis added to them.
     */
    public String getDescriptiveTitle() {
        String str = getTitle();
        if (str == null) {
            // Get the first two words
            StringBuilder buf = new StringBuilder();
            bodyLoop: for (Object o : getBody()) {
                if (o instanceof String) {
                    StringTokenizer st = new StringTokenizer((String) o);
                    while (st.hasMoreTokens()) {
                        if (buf.length() == 0) {
                            buf.append(st.nextToken());
                        } else {
                            buf.append(' ');
                            buf.append(st.nextToken());
                            break bodyLoop;
                        }
                    }
                }
            }
            // Append an elipsis to the words
            if (buf.length() > 0) {
                buf.append("...");
                str = buf.toString();
            } else {
                str = "";
            }
        }
        return str;
    }
    /**
     * Returns the descriptive title of the question converted to lower
     * case and all special characters removed.
     */
    public String getDescriptiveURL() {
        String str = Normalizer.normalize(getDescriptiveTitle().toLowerCase(), Normalizer.Form.NFKD);
        StringBuilder buf = new StringBuilder(str.length());
        for (int i=0, n = str.length(); i < n; i++) {
            char ch = str.charAt(i);
            if (ch <= ' ' && buf.length() > 0 && buf.charAt(buf.length() - 1) != '_') {
                buf.append('_');
            } else if (ch >= 'a' && ch <= 'z') {
                buf.append(ch);
            }
        }
        if (buf.length() > 24) {
            buf.setLength(24);
        }
        
        return buf.toString();
    }
    
    public LinkedList<Object> getBody() {
        return body;
    }
    
    public void addQuestionText(String newValue) {
        body.add(newValue);
    }
    public void addAnswerList(AnswerList newValue) {
        body.add(newValue);
    }
    
    public String toString() {
        StringBuilder buf = new StringBuilder();
        if (title != null) {
            buf.append("title: ");
            buf.append(title);
            buf.append('\n');
        }
        buf.append("body: ");
        buf.append(body.toString());
        return buf.toString();
    }
    
    /**
     * Returns true, if the question text is missing, or if no answer list
     * is defined.
     */
    public boolean isIncomplete() {
        boolean isIncomplete = false;
        if (getBody().size() == 0) {
            isIncomplete = true;
        } else {
            boolean hasQuestionText = false;
            boolean hasAnswerList = false;
            boolean isExternalAnswerList = false;
            for (Object o : getBody()) {
                if (o instanceof String) {
                    hasQuestionText = true;
                }
                if (o instanceof AnswerList) {
                    hasAnswerList = true;
                    AnswerList al = (AnswerList) o;
                    isExternalAnswerList = isExternalAnswerList | al.getType() == AnswerListType.EXTERNAL;
                }
            }
            isIncomplete = (! hasQuestionText && !isExternalAnswerList) /*|| ! hasAnswerList*/;
        }
        return isIncomplete;
    }

    public int getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(int start) {
        this.startPosition = start;
    }

    public int getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(int end) {
        this.endPosition = end;
    }
}
