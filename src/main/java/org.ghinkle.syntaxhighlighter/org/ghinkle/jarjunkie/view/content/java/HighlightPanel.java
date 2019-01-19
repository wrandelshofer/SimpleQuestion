/*
 * Copyright 2002-2004 Greg Hinkle
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
