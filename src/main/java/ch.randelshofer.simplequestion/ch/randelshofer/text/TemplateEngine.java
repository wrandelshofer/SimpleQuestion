/*
 * @(#)TemplateEngine.java  1.0  2011-06-06
 * 
 * Copyright (c) 2011 Werner Randelshofer, Immensee, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package ch.randelshofer.text;

import ch.randelshofer.io.StreamPosTokenizer;
import java.io.Reader;
import java.io.Writer;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * {@code TemplateEngine} replaces placeholders in a template text with values
 * from a key-value map.
 * <p>
 * The template is a String with placeholders. The following placeholders
 * are supported:
 * <ul>
 * <li>{@code ${key}} A placeholder with a key from the key-value map.<li>
 * </ul>
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-06-06 Created.
 */
public class TemplateEngine {

    /** The key-value map. */
    private Map<String, String> map;

    public TemplateEngine() {
    }


    /** Processes a template and replaces all placeholders with the values from
     * the given key-value map.
     *
     * @param in The template string.
     * @param map Key value pairs.
     * @throws IOException
     */
    public static String process(String in, String... map) throws IOException {
        StringWriter out=new StringWriter();
        HashMap<String,String> hm=new HashMap<String,String>();
        for (int i=0;i<map.length;i+=2) {
            hm.put(map[i], map[i+1]);
        }
        process(out, new StringReader(in), hm);
        return out.toString();
    }
    /** Processes a template and replaces all placeholders with the values from
     * the given key-value map.
     *
     * @param in
     * @param map
     * @throws IOException
     */
    public static String process(String in, Map<String, String> map) throws IOException {
        StringWriter out=new StringWriter();
        process(out, new StringReader(in), map);
        return out.toString();
    }
    /** Processes a template and replaces all placeholders with the values from
     * the given key-value map.
     *
     * @param out
     * @param in
     * @param map
     * @throws IOException
     */
    public static void process(Writer out, Reader in, Map<String, String> map) throws IOException {
        StreamPosTokenizer tt = new StreamPosTokenizer(in);
        tt.resetSyntax();
        //tt.whitespaceChars(0, ' '); Whitespace is significant, don't get rid of it
        tt.eolIsSignificant(true);
        tt.wordChars(' ', 255);

        tt.ordinaryChar('$');
        tt.ordinaryChar('{');
        tt.ordinaryChar('}');

        while (tt.nextToken() != StreamPosTokenizer.TT_EOF) {
            switch (tt.ttype) {
                case '$': {
                    if (tt.nextToken() != '{') {
                        out.write((char) tt.ttype);
                        tt.pushBack();
                        break;
                    }
                    StringBuilder keyb = new StringBuilder();
                    while (tt.nextToken() == StreamPosTokenizer.TT_WORD) {
                        keyb.append(tt.sval);
                    }
                    if (tt.ttype != '}') {
                        throw new IOException("\"}\" missing after \"${" + keyb.toString() + "\" in line " + tt.lineno());
                    }
                    String key=keyb.toString().trim();
                    out.write(map.get(key));
                    break;
                }
                case '{':
                case '}':
                    out.write((char) tt.ttype);
                    break;
                case StreamPosTokenizer.TT_EOL:
                    out.write('\n');
                    break;
                case StreamPosTokenizer.TT_WORD:
                    out.write(tt.sval);
                    break;
            }
        }
    }
}
