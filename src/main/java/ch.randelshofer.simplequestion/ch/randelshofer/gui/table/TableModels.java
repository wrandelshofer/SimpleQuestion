/* @(#)TableModels.java
 * Copyright © Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.gui.table;

import ch.randelshofer.gui.datatransfer.CharArrayReaderTransferable;
import ch.randelshofer.gui.datatransfer.JVMLocalObjectTransferable;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
/**
 * This class provides static utility operations for
 * <code>MutableTableModel</code>'s.
 *
 * @author Werner Randelshofer
 * @version 1.0 2002-11-21 Created.
 */
public class TableModels {
    
    /**
     * Suppresses default constructor, ensuring non-instantiability.
     */
    public TableModels() {
    }
    
    /**
     * Creates a transferable in text/html format from
     * a mutable table model.
     *
     * @return A transferable of type text/html
     */
    public static Transferable createHTMLTransferable(MutableTableModel model, int[] rows) {
        try {
            CharArrayWriter w = new CharArrayWriter();
            w.write("<html><body><table>");
            int columnCount = model.getColumnCount();
            for (int i=0; i < rows.length; i++) {
                w.write("<tr>");
                for (int j=0; j < columnCount; j++) {
                    w.write("<td>"+model.getValueAt(rows[i],j)+"</td>");
                }
                w.write("</tr>");
            }
            w.write("</table></body></html>");
            w.close();
            return new CharArrayReaderTransferable(w.toCharArray(), "text/html", "HTML");
        } catch (IOException e) {
            throw new InternalError(e.getMessage());
        }
    }
    /**
     * Creates a transferable in text/plain format from
     * a mutable table model.
     *
     * @return A transferable of type java.awt.datatransfer.StringSelection
     */
    public static Transferable createPlainTransferable(MutableTableModel model, int[] rows) {
        StringBuffer buf = new StringBuffer();
        int columnCount = model.getColumnCount();
        for (int i=0; i < rows.length; i++) {
            for (int j=0; j < columnCount; j++) {
                if (j != 0) buf.append('\t');
                buf.append(model.getValueAt(rows[i],j));
            }
            if (i != 0) buf.append('\n');
        }
        return new StringSelection(buf.toString());
    }
    
    /**
     * Creates a local JVM transferable from
     * a mutable table model.
     *
     * @return A JVM local object transferable of type java.util.ArrayList.
     * Each element of the list is in turn a java.util.ArrayList.
     */
    public static Transferable createLocalTransferable(MutableTableModel model, int[] rows) {
        Object[][] table = new Object[rows.length][model.getColumnCount()];
        int columnCount = model.getColumnCount();
        for (int i=0; i < rows.length; i++) {
            ArrayList<Object> c = new ArrayList<>(columnCount);
            for (int j=0; j < columnCount; j++) {
                table[i][j] = model.getValueAt(rows[i],j);
            }
        }
        return new JVMLocalObjectTransferable(Object[][].class, table);
    }

     /**
     * Returns the contents of the transferable as
     * an <code>Object[][]</code> containing String's.
     * Where each row is determined by reading a line of text
     * from the transferable and each column by splitting
     * a line into tabulator delimited strings.
     *
     * @exception UnsupportedFlavorException
     * If the transferable does not support DataFlavor.getTextPlainUnicodeFlavor()
     */
    public static Object[][] getPlainTable(Transferable t, int columnCount)
    throws UnsupportedFlavorException, IOException {
        List<Object> list = new ArrayList<>();
        try (BufferedReader in = new BufferedReader(new StringReader((String) t.getTransferData(DataFlavor.stringFlavor)))) {
            String line;
            while ((line = in.readLine()) != null) {
                Object[] rowData = new Object[columnCount];
                StringTokenizer st = new StringTokenizer(line, "\t");
                for (int i = 0; i < columnCount && st.hasMoreTokens(); i++) {
                    rowData[i] = st.nextToken();
                }
                list.add(rowData);
            }
        }
        return (Object[][]) list.toArray();
    }

     /**
     * Returns the contents of the transferable as
     * an <code>Object[][]</code> containing String's.
     * Where each row is determined by reading a line of text
     * from the transferable and each column by splitting
     * a line into tabulator delimited strings.
     *
     * @exception UnsupportedFlavorException
     * If the transferable does not support
     * <code>DataFlavor.stringFlavor</code>
     */
    public static Object[][] getStringTable(Transferable t, int columnCount)
    throws UnsupportedFlavorException, IOException {
        List<Object> list = new ArrayList<>();
        try (BufferedReader in = new BufferedReader(DataFlavor.stringFlavor.getReaderForText(t))) {
            String line;
            while ((line = in.readLine()) != null) {
                Object[] rowData = new Object[columnCount];
                StringTokenizer st = new StringTokenizer(line, "\t");
                for (int i = 0; i < columnCount && st.hasMoreTokens(); i++) {
                    rowData[i] = st.nextToken();
                }
                list.add(rowData);
            }
        }
        return (Object[][]) list.toArray();
    }
}
