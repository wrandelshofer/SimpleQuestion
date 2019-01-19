/* @(#)SimpleQuestionView.java
 * 
 * Copyright (c) 2009 Werner Randelshofer
 * Staldenmattweg 2, Immensee, CH-6405, Switzerland.
 * All rights reserved.
 * 
 * The copyright of this software is owned by Werner Randelshofer. 
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * Werner Randelshofer. For details see accompanying license terms.
 */
package ch.randelshofer.simplequestion;

import ch.randelshofer.gift.export.Exporter;
import ch.randelshofer.gift.highlight.GIFTScanner;
import ch.randelshofer.gift.highlight.GIFTTokenTypes;
import ch.randelshofer.gift.parser.GIFTParser;
import ch.randelshofer.gift.parser.Question;
import ch.randelshofer.gui.highlight.SyntaxHighlighter;
import ch.randelshofer.io.ConfigurableFileFilter;
import ch.randelshofer.teddy.CharacterSetAccessory;
import ch.randelshofer.teddy.TeddyView;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import org.jhotdraw.app.action.file.LoadFileAction;
import org.jhotdraw.gui.JFileURIChooser;
import org.jhotdraw.gui.JSheet;
import org.jhotdraw.gui.URIChooser;
import org.jhotdraw.gui.event.SheetEvent;
import org.jhotdraw.gui.event.SheetListener;
import org.jhotdraw.util.ResourceBundleUtil;

/**
 * SimpleQuestionView.
 *
 * @author Werner Randelshofer
 * @version 1.0 2009-09-06 Created.
 */
public class SimpleQuestionView extends TeddyView {
    public final static long serialVersionUID=1L;

    private ResourceBundleUtil labels;
    private Preferences prefs;
    private GIFTScanner scanner;
    // FIXME - Replace this by a TransferHandler
    private DropTargetListener dropTargetListener = new DropTargetListener() {

        @Override
        public void dragEnter(DropTargetDragEvent event) {
            if (event.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                event.acceptDrag(DnDConstants.ACTION_COPY);
            } else {
                event.rejectDrag();
            }
        }

        @Override
        public void dragOver(DropTargetDragEvent event) {
            if (event.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                event.acceptDrag(DnDConstants.ACTION_COPY);
            } else {
                event.rejectDrag();
            }
        }

        @Override
        public void dropActionChanged(DropTargetDragEvent dtde) {
            // Nothing to do
        }

        @Override
        public void dragExit(DropTargetEvent dte) {
            // Nothing to do
        }

        @Override
        public void drop(DropTargetDropEvent event) {
            if (event.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                event.acceptDrop(DnDConstants.ACTION_COPY);
                try {
                    @SuppressWarnings("unchecked")
                    List<File> files= (List<File>) event.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (files.size() == 1) {
                        LoadFileAction action = new LoadFileAction(getApplication(), SimpleQuestionView.this);
                        action.loadViewFromURI(SimpleQuestionView.this, files.get(0).toURI(),null);
                    }
                } catch (UnsupportedFlavorException ex) {
                } catch (IOException ex) {
                }
            } else {
                event.rejectDrop();
            }
        }
    };

    /** Creates new form SimpleQuestionView */
    public SimpleQuestionView() {
        }
    @Override
    protected void init0() {
        try {
        prefs = Preferences.userNodeForPackage(SimpleQuestionView.class);
        prefs.addPreferenceChangeListener(new PreferenceChangeListener() {

            @Override
            public void preferenceChange(PreferenceChangeEvent evt) {
                if (evt.getKey().equals("editorFontFamily") ||
                        evt.getKey().equals("editorFontSize")) {
                    if (editor != null) {
                        editor.setFont(new Font(prefs.get("editorFontFamily", "Dialog"), Font.PLAIN, prefs.getInt("editorFontSize", 13)));
                    }
                }
            }
        });
        } catch (NoClassDefFoundError e) {
            // Handle "Could not initialize java.util.prefs.MacOSX" error
        }
        super.init0();
    }

    @Override
    protected JTextPane createEditor() {
        scanner = new GIFTScanner();
        SyntaxHighlighter sh = new SyntaxHighlighter(60, 40, scanner);
        sh.changeStyle(GIFTTokenTypes.COMMENT, new Color(0x969696)); // dark gray
        sh.changeStyle(GIFTTokenTypes.MID_COMMENT, new Color(0x969696)); // dark gray
        sh.changeStyle(GIFTTokenTypes.WHITESPACE, new Color(0x0000e6), Font.BOLD); // dark blue
        sh.changeStyle(GIFTTokenTypes.OPERATOR, new Color(0x0000e6), Font.BOLD); // dark blue
        sh.changeStyle(GIFTTokenTypes.SEPARATOR, new Color(0x0000e6), Font.BOLD); // dark blue
        sh.changeStyle(GIFTTokenTypes.TITLE, Color.black, Font.BOLD);
        sh.changeStyle(GIFTTokenTypes.ANSWER, new Color(0x009900)); // dark green
        sh.changeStyle(GIFTTokenTypes.NUMERIC_OPERATOR, new Color(0xce7b00)); // dark orange
        sh.changeStyle(GIFTTokenTypes.NUMBER, new Color(0xce7b00)); // dark orange
        sh.changeStyle(GIFTTokenTypes.LITERAL, new Color(0xce7b00)); // dark orange
        sh.changeStyle(GIFTTokenTypes.QUESTION, Color.black);
        sh.changeStyle(GIFTTokenTypes.FEEDBACK, Color.magenta.darker(), Font.ITALIC);
        // sh.changeStyle(GIFTTokenTypes.UNRECOGNIZED, Color.red.brighter(), Font.BOLD);
        if (prefs!=null)
        sh.setFont(new Font(prefs.get("editorFontFamily", "Dialog"), Font.PLAIN, prefs.getInt("editorFontSize", 13)));
        return sh;
    }

    @Override
    protected StyledDocument createDocument() {
        DefaultStyledDocument doc = new DefaultStyledDocument() {

            @Override
            public void setCharacterAttributes(int offset, int length, AttributeSet s, boolean replace) {
                if (length == 0) {
                    return;
                }
                try {
                    writeLock();
                    DefaultDocumentEvent changes =
                            new DefaultDocumentEvent(offset, length, DocumentEvent.EventType.CHANGE);

                    // split elements that need it
                    buffer.change(offset, length, changes);

                    AttributeSet sCopy = (s == null) ? null : s.copyAttributes();

                    // PENDING(prinz) - this isn't a very efficient way to iterate
                    int lastEnd = Integer.MAX_VALUE;
                    for (int pos = offset; pos < (offset + length); pos = lastEnd) {
                        Element run = getCharacterElement(pos);
                        lastEnd = run.getEndOffset();
                        if (pos == lastEnd) {
                            // offset + length beyond length of document, bail.
                            break;
                        }
                        MutableAttributeSet attr = (MutableAttributeSet) run.getAttributes();
                        changes.addEdit(new AttributeUndoableEdit(run, sCopy, replace));
                        if (replace) {
                            attr.removeAttributes(attr);
                        }
                        attr.addAttributes(s);
                    }
                    changes.end();
                    fireChangedUpdate(changes);
                    // Suppress undoable edit events for attribute changes
                    //fireUndoableEditUpdate(new UndoableEditEvent(this, changes));
                } finally {
                    writeUnlock();
                }
            }
        };
        doc.setParagraphAttributes(0, 1, ((StyledEditorKit) editor.getEditorKit()).getInputAttributes(), true);
        return doc;
    }

    @Override
    public void init() {
        super.init();
        new DropTarget(this, dropTargetListener);
        new DropTarget(editor, dropTargetListener);
    }

    @Override
    public void clear() {
        labels = new ResourceBundleUtil(ResourceBundle.getBundle("ch/randelshofer/simplequestion/Labels"));

        String templateChoice = prefs==null?"empty":prefs.get("templateChoice", "sample");
        if (!"empty".equals(templateChoice)) {
            InputStream in = null;
            try {
                if (templateChoice.equals("sample")) {
                    if (Locale.getDefault().getLanguage().equals("de")) {
                        in = getClass().getResourceAsStream("/ch/randelshofer/simplequestion/examples_de.txt");
                    } else {
                        in = getClass().getResourceAsStream("/ch/randelshofer/simplequestion/examples.txt");
                    }
                } else if (templateChoice.equals("file")) {
                    in = new FileInputStream(prefs.get("templateFile", ""));
                }
                final StyledDocument doc = readDocument(in, "UTF8");
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                editor.setDocument(doc);
                doc.addUndoableEditListener(undoManager);
                }
                });
            } catch (Throwable e) {
                // Should never happen, because we read a resource.
                // There is nothing we can do about this.
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // Should never happen, because we read a resource.
                        e.printStackTrace();
                    }
                }
            }
        }

        /*
        editor.addPropertyChangeListener(new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
        System.out.println("GIFTEditorProject.editor.propertyChange "+evt.getPropertyName());
        }

        });
         */
    }

    public GIFTScanner getScanner() {
        return scanner;
    }

    /**
     * Reads a document from a file using the specified character set.
     */
    private StyledDocument readDocument(InputStream pin, String characterSet)
            throws IOException {
        // ProgressMonitorInputStream pin = new ProgressMonitorInputStream(this, "Reading "+f.getName(), new FileInputStream(f));
        BufferedReader in = new BufferedReader(new InputStreamReader(pin, characterSet));
        try {

            // PlainDocument doc = new PlainDocument();
            StyledDocument doc = createDocument();
            MutableAttributeSet attrs = ((StyledEditorKit) editor.getEditorKit()).getInputAttributes();
            String line;
            boolean isFirst = true;
            while ((line = in.readLine()) != null) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    doc.insertString(doc.getLength(), "\n", attrs);
                }
                doc.insertString(doc.getLength(), line, attrs);
            }
            return doc;
        } catch (BadLocationException e) {
            throw new IOException(e.getMessage());
        } catch (OutOfMemoryError e) {
            System.err.println("out of memory!");
            throw new IOException("Out of memory.");
        } finally {
            in.close();
        }
    }
    @Override
    public void write(URI f, URIChooser chooser) throws IOException {
        if (chooser==null || !(chooser instanceof JFileURIChooser)) {
            write(f,"UTF-8","\n");
        } else {
            JFileURIChooser fc = (JFileURIChooser) chooser;
            if (fc.getAccessory() instanceof CharacterSetAccessory) {

        write(f, ((CharacterSetAccessory) fc.getAccessory()).getCharacterSet(), ((CharacterSetAccessory) ((JFileURIChooser) chooser).getAccessory()).getLineSeparator());
        } else {
                export(new File(f), fc.getFileFilter(), fc.getAccessory());
        }
        }
    }

    /**
     * By convention, this method is never invoked on the AWT event dispatcher
     * thread.
     */
    public void export(File f, javax.swing.filechooser.FileFilter filter, Component accessory) throws IOException {
        prefs.put("projectExportFile", f.getPath());
        prefs.put("projectExportFilter", filter.getDescription());
        try {
            List<Question> questions = new GIFTParser().parse(editor.getText());

            if (filter instanceof ConfigurableFileFilter) {
            ConfigurableFileFilter cff = (ConfigurableFileFilter) filter;
                Exporter exporter = (Exporter) cff.getClientProperty("exporter");
                File baseFile = (getURI() != null) ? new File(getURI()) : f;
                exporter.export(questions, f, cff, baseFile);
            }
        } catch (final ch.randelshofer.io.ParseException e) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {

                    public void run() {
                        select(e.getStartPosition(), e.getEndPosition() + 1);
                        editor.requestFocus();
                    }
                });
            } catch (InvocationTargetException e2) {
                InternalError error = new InternalError(e2.getMessage());
                error.initCause(e2);
                throw error;
            } catch (InterruptedException e2) {
                // Nothing we can do about
            }

            throw e;
        }
    }

    public void verifySyntax() {
        try {
            List<Question> questions = new GIFTParser().parse(editor.getText());

            for (final Question q : questions) {
                if (q.isIncomplete()) {
                    String text = editor.getText().substring(q.getStartPosition(), q.getEndPosition() + 1);
                    if (text.length() > 50) {
                        text = text.substring(0, 50) + "...";
                    }
                    JSheet.showMessageSheet(editor,
                            "<html>" + UIManager.getString("OptionPane.css") +
                            "<b>" + labels.getString("questionIsIncomplete") + "</b><p>" +
                            text, //q.toString(),
                            JOptionPane.ERROR_MESSAGE, new SheetListener() {

                        public void optionSelected(SheetEvent evt) {

                            select(q.getStartPosition(), q.getEndPosition() + 1);
                            editor.requestFocus();
                        }
                    });
                    return;
                }
            }

            JSheet.showMessageSheet((JComponent) this,
                    "<html>" + UIManager.getString("OptionPane.css") +
                    "<b>" + labels.getString("syntaxIsCorrect") + "</b><br>" +
                    labels.getFormatted("syntaxThereAreNQuestions", questions.size()),
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (final ch.randelshofer.io.ParseException e) {
            select(e.getStartPosition(), e.getEndPosition() + 1);


            // FIXME localize this error messsage
            JSheet.showMessageSheet(editor,
                    "<html>" + UIManager.getString("OptionPane.css") +
                    "<b>" + labels.getString("syntaxIsIncorrect") + "</b><p>" +
                    e.getMessage(),
                    JOptionPane.ERROR_MESSAGE, new SheetListener() {

                public void optionSelected(SheetEvent evt) {
                    select(e.getStartPosition(), e.getEndPosition() + 1);
                    editor.requestFocus();
                }
            });

        } catch (Exception e) {
            ((Throwable) e).printStackTrace();
            // FIXME localize this error messsage
            JSheet.showMessageSheet((JComponent) this,
                    "<html>" + UIManager.getString("OptionPane.css") +
                    "<b>" + labels.getString("syntaxIsIncorrect") + "</b><p>" +
                    e.getMessage(),
                    JOptionPane.ERROR_MESSAGE);

        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
