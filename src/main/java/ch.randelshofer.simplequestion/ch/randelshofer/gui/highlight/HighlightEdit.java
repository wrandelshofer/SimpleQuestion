/*
 * @(#)HighlightEdit.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
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
