/*
 * @(#)HighlightEdit.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package org.ghinkle.jarjunkie.view.content.java;

// A program illustrating the use of the SyntaxHighlighter class.

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

class HighlightEdit extends JFrame {
    private final static long serialVersionUID = 1L;
    private String filename;
    private SyntaxHighlighter text;

    public static void main(String[] args) {
        if (args.length != 1) {
            /*
            System.err.println("Usage: java HighlightEdit filename");
            System.exit(1);
             */
            args = new String[]{"src/org/ghinkle/jarjunkie/view/content/java/HighlightEdit.java"};
        }
        Runner runner = new Runner(args[0]);
        SwingUtilities.invokeLater(runner);
    }

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
