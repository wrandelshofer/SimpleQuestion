/*
 * @(#)HighlightPanel.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package org.ghinkle.jarjunkie.view.content.java;

import javax.swing.*;
import java.awt.*;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), Feb 8, 2005
 * @version $Revision: 1.1.1.1 $($Author: ghinkl $ / $Date: 2005/03/15 01:58:23 $)
 */
public class HighlightPanel extends JPanel {
    private final static long serialVersionUID = 1L;
    private SyntaxHighlighter text;

    public void display(String s) {
        setLayout(new BorderLayout());

        Scanner scanner = new JavaScanner();
        text = new SyntaxHighlighter(24, 80, scanner);
        JScrollPane scroller = new JScrollPane(text);
        scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scroller, BorderLayout.CENTER);
        text.setText(s);
        if (text.getText().length() > 0) {
            // Workaround for bug 4782232 in Java 1.4
            text.setCaretPosition(1);
            text.setCaretPosition(0);
        }
    }

    public void display(InputStream inputStream) {
        setLayout(new BorderLayout());

        Scanner scanner = new JavaScanner();
        text = new SyntaxHighlighter(24, 80, scanner);
        JScrollPane scroller = new JScrollPane(text);
        scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scroller, BorderLayout.CENTER);

        try {
            text.read(new InputStreamReader(inputStream), null);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        if (text.getText().length() > 0) {
            // Workaround for bug 4782232 in Java 1.4
            text.setCaretPosition(1);
            text.setCaretPosition(0);
        }
    }

    public String getText() {
        return text.getText();
    }

}
