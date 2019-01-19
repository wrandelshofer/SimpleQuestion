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
 *
 */

package ch.randelshofer.gui.highlight;

// A program illustrating the use of the SyntaxHighlighter class.

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

class HighlightEdit extends JFrame {
    static final long serialVersionUID = 1L;
    String filename;
    SyntaxHighlighter text;

    static class Runner implements Runnable {
        String filename;

        Runner(String f) {
            filename = f;
        }

        public void run() {
            HighlightEdit program = new HighlightEdit();
            program.display(filename);
        }
    }

    void display(String s) {
        filename = s;
        String localStyle = UIManager.getSystemLookAndFeelClassName();
        try {
            UIManager.setLookAndFeel(localStyle);
        } catch (Exception e) {
        }

        setTitle("HighlightEdit " + filename);
        addWindowListener(new Closer());
        Scanner scanner = new JavaScanner();
        text = new SyntaxHighlighter(24, 80, scanner);
        JScrollPane scroller = new JScrollPane(text);
        scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        Container pane = getContentPane();
        pane.add(scroller);
        pack();
        setVisible(true);

        try {
            text.read(new FileReader(filename), null);
        } catch (IOException err) {
            System.err.println(err.getMessage());
            System.exit(1);
        }
        // Workaround for bug 4782232 in Java 1.4
        text.setCaretPosition(1);
        text.setCaretPosition(0);
    }

    class Closer extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
            try {
                text.write(new FileWriter(filename));
            } catch (IOException err) {
                System.err.println(err.getMessage());
                System.exit(1);
            }
            System.exit(0);
        }
    }
}
