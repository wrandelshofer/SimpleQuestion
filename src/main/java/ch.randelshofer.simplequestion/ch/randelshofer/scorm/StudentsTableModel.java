/* @(#)StudentsTableModel.java
 *
 * Copyright (c) 2003-2006 Werner Randelshofer
 * Staldenmattweg 2, Immensee, CH-6405, Switzerland.
 * http://www.randelshofer.ch
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Werner Randelshofer. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Werner Randelshofer.
 */

package ch.randelshofer.scorm;

import ch.randelshofer.gui.datatransfer.CompositeTransferable;
import ch.randelshofer.gui.datatransfer.DefaultTransferable;
import ch.randelshofer.gui.table.MutableTableModel;
import ch.randelshofer.gui.table.TableModels;
import ch.randelshofer.util.Strings;
import ch.randelshofer.xml.DOMs;
import org.jhotdraw.util.ResourceBundleUtil;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.swing.Action;
import javax.swing.table.AbstractTableModel;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ResourceBundle;

/**
 * StudentsTableModel.
 *
 * @author Werner Randelshofer
 * @version 1.2 2006-05-26 Internationalized.
 * <br>1.1 2006-05-06 Reworked.
 * <br>1.0 August 24, 2003  Created.
 */
public class StudentsTableModel extends AbstractTableModel implements MutableTableModel {
    public final static long serialVersionUID = 1L;
    /**
     * We store our rows in an array list.
     * Each row contains a StudentModel object.
     */
    private ArrayList<StudentModel> rows = new ArrayList<>();
    /**
     * The enabled state of the model.
     * By default this value is set to false.
     */
    private boolean enabled;

    private String[] columnNames;

    /**
     * Creates a new instance.
     */
    public StudentsTableModel() {
        ResourceBundleUtil labels;
        labels = new ResourceBundleUtil(ResourceBundle.getBundle("ch.randelshofer.scorm.Labels"));
        columnNames = new String[]{
                labels.getString("login.id"),
                labels.getString("login.firstName"),
                labels.getString("login.middleInitial"),
                labels.getString("login.lastName"),
                labels.getString("login.password")
        };
    }

    public void add(int rowIndex, Object data) {
        rows.add(rowIndex, (StudentModel) data);
        fireTableRowsInserted(rowIndex, rowIndex);
    }

    public void add(Object data) {
        add(getRowCount(), data);
    }

    public void clear() {
        int rowCount = getRowCount();
        if (rowCount > 0) {
            rows.clear();
            fireTableRowsDeleted(0, rowCount - 1);
        }
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }


    public Object getCreatableRowType(int rowIndex) {
        return "User";
    }

    public Object[] getCreatableRowTypes(int rowIndex) {
        return new Object[]{"User"};
    }

    public javax.swing.Action[] getRowActions(int[] rows) {
        return new Action[0];
    }

    public int getRowCount() {
        return rows.size();
    }

    public StudentModel getRow(int rowIndex) {
        return rows.get(rowIndex);
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        StudentModel student = rows.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return student.getID();
            case 1:
                return student.getFirstName();
            case 2:
                return student.getMiddleInitial();
            case 3:
                return student.getLastName();
            case 4:
                String pw = student.getPassword();
                return (pw == null || pw.length() == 0) ? "" : "******";
            default:
                throw new ArrayIndexOutOfBoundsException("Illegal column index:" + columnIndex);
        }
    }

    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        StudentModel student = rows.get(rowIndex);
        String s = (String) value;
        switch (columnIndex) {
            case 0:
                student.setID(s);
                break;
            case 1:
                student.setFirstName(s);
                break;
            case 2:
                student.setMiddleInitial(s);
                break;
            case 3:
                student.setLastName(s);
                break;
            case 4:
                student.setPassword(s);
                break;
        }
    }

    public void createRow(int rowIndex, Object type) {
        StudentModel student = new StudentModel();
        student.setID("student");
        add(rowIndex, student);
    }

    public boolean isRowAddable(int rowIndex) {
        return true;
    }

    public boolean isRowRemovable(int rowIndex) {
        return true;
    }

    public void removeRow(int rowIndex) {
        rows.remove(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    public Transferable exportRowTransferable(int[] rows) {
        CompositeTransferable t = new CompositeTransferable();
        t.add(TableModels.createHTMLTransferable(this, rows));
        t.add(TableModels.createPlainTransferable(this, rows));
        t.add(createXMLTransferable(rows));
        return t;

    }

    /**
     * Writes the contents of the DocumentModel into the output stream.
     * For peak performance, the output stream should be buffered.
     * <p>
     * Format:
     * <pre>
     * &lt;TinyLMS version="1"&gt;
     * &lt;organization structure=("hierarchical"|"layered")/&gt;
     * &lt;authentication login=("automatic"|"unrestricted"|"restricted"/&gt;
     * &lt;locale&gt;&lt;/locale&gt;
     * &lt;layout&gt;
     *   &lt;toc width="*"/&gt;
     *   &lt;nav height="*"/&gt;
     *   &lt;page width="*" height="*"/&gt;
     * &lt;/layout&gt;
     * &lt;users&gt;
     *   &lt;student id=""&gt;
     *     &lt;firstName&gt;&lt;/firstName&gt;
     *     &lt;lastName&gt;&lt;/lastName&gt;
     *     &lt;middleInitial&gt;&lt;/middleInitial&gt;
     *     &lt;passwordDigest&gt;&lt;/passwordDigest&gt;
     *   &lt;/student&gt;
     * &lt;/students&gt;
     * &lt;/TinyLMS&gt;
     * </pre>
     */
    private Transferable createXMLTransferable(int[] rows) {
        try {
            String value;

            // Create the DOM Document. Our Markup language
            // is TinyLMS.
            DOMImplementation domImpl = DocumentBuilderFactory.newInstance().newDocumentBuilder().getDOMImplementation();
            org.w3c.dom.Document doc = domImpl.createDocument(null, "TinyLMS", domImpl.createDocumentType("TinyLMS", "TinyLmsPublicID", "TinyLMSSystemID"));
            org.w3c.dom.Element docRoot = doc.getDocumentElement();
            docRoot.setAttribute("version", "1");
            org.w3c.dom.Element elem, elem2, elem3;

            // ------------------------------------
            // Write Students
            elem = doc.createElement("users");

            for (int i = 0; i < rows.length; i++) {
                StudentModel student = getRow(rows[i]);
                elem2 = doc.createElement("student");
                elem2.setAttribute("id", student.getID());

                if (student.getFirstName() != null) {
                    elem3 = doc.createElement("firstName");
                    elem3.appendChild(doc.createTextNode(student.getFirstName()));
                    elem2.appendChild(elem3);
                }
                if (student.getLastName() != null) {
                    elem3 = doc.createElement("lastName");
                    elem3.appendChild(doc.createTextNode(student.getLastName()));
                    elem2.appendChild(elem3);
                }
                if (student.getMiddleInitial() != null) {
                    elem3 = doc.createElement("middleInitial");
                    elem3.appendChild(doc.createTextNode(student.getMiddleInitial()));
                    elem2.appendChild(elem3);
                }
                if (student.getPasswordDigest() != null) {
                    elem3 = doc.createElement("passwordDigest");
                    elem3.appendChild(doc.createTextNode(student.getPasswordDigest()));
                    elem2.appendChild(elem3);
                }
                elem.appendChild(elem2);
            }
            docRoot.appendChild(elem);

            // Write the document to the stream
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(out));
            out.close();

            return new DefaultTransferable(out.toByteArray(), "text/xml", "XML");

        } catch (IOException e) {
            throw new InternalError(e.toString());
        } catch (ParserConfigurationException e) {
            throw new InternalError(e.toString());
        } catch (TransformerException e) {
            throw new InternalError(e.toString());
        }
    }

    public boolean isRowImportable(DataFlavor[] transferFlavors, int action, int row, boolean asChild) {
        if (asChild) {
            return false;
        }
        return true;
    }

    public int importRowTransferable(Transferable t, int action, int row, boolean asChild) {
        if (asChild) {
            return 0;
        }

        try {
            if (t.isDataFlavorSupported(new DataFlavor("text/xml"))) {
                return importXMLTransferable(t, row);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            String[][] data = (String[][]) TableModels.getPlainTable(t, getColumnCount());
            for (int i = 0; i < data.length; i++) {
                StudentModel student = new StudentModel();
                student.setID(data[i][0]);
                student.setFirstName(data[i][1]);
                student.setMiddleInitial(data[i][2]);
                student.setLastName(data[i][3]);
                if (data[i][4] != null && Strings.replace(data[i][4], '*', ' ').trim().length() != 0) {
                    student.setPassword(data[i][4]);
                }
                add(row + i, student);
            }
            return data.length;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Reads the contents of the DocumentModel from the transferable.
     * <p>
     * Format:
     * <pre>
     * &lt;TinyLMS version="1"&gt;
     * &lt;users&gt;
     *   &lt;student id=""&gt;
     *     &lt;firstName&gt;&lt;/firstName&gt;
     *     &lt;lastName&gt;&lt;/lastName&gt;
     *     &lt;middleInitial&gt;&lt;/middleInitial&gt;
     *     &lt;passwordDigest&gt;&lt;/passwordDigest&gt;
     *   &lt;/student&gt;
     * &lt;/students&gt;
     * &lt;/TinyLMS&gt;
     * </pre>
     *
     * @throws IOException If the import failed.
     */
    public int importXMLTransferable(Transferable t, int row)
            throws IOException {
        LinkedList<StudentModel> importedStudents = new LinkedList<>();
        try (InputStream in = (InputStream) t.getTransferData(new DataFlavor("text/xml"))) {
            // The DOM Document.
            Element root, elem, elem2;
            String attrValue, text;
            Node node;

            // Read Document root
            root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in).getDocumentElement();
            if (!root.getTagName().equals("TinyLMS")) {
                throw new IOException("Unsupported document type: " + root.getNodeName());
            }
            attrValue = DOMs.getAttribute(root, "version", "");
            if (!(attrValue.equals("") || attrValue.equals("1"))) {
                throw new IOException("Unsupported document version: " + attrValue);
            }

            // Read Student elements
            // -------------------------
            Element[] students = DOMs.getElements(DOMs.getElement(root, "users"), "student");
            for (int i = 0; i < students.length; i++) {
                StudentModel student = new StudentModel();
                student.setID(DOMs.getAttribute(students[i], "id", ""));
                student.setFirstName(DOMs.getElementText(students[i], "firstName", null));
                student.setLastName(DOMs.getElementText(students[i], "lastName", null));
                student.setMiddleInitial(DOMs.getElementText(students[i], "middleInitial", null));
                student.setPasswordDigest(DOMs.getElementText(students[i], "passwordDigest", null));
                importedStudents.add(student);
            }

        } catch (Exception e) {
            throw new IOException(e.toString());
        }

        for (StudentModel importedStudent : importedStudents) {
            add(row++, importedStudent);
        }
        return importedStudents.size();
    }

}
