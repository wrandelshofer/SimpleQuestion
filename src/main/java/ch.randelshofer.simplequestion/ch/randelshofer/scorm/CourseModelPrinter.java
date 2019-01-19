/* @(#)CourseModelPrinter.java
 *
 * Copyright (c) 2003 Werner Randelshofer
 * Staldenmattweg 2, CH-6405 Immensee, Switzerland
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Werner Randelshofer. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Werner Randelshofer.
 *
 * 
 * The following conditions apply only, if this software is distributed 
 * as part of TinyLMS:
 *
 *      This program is free software; you can redistribute it and/or modify it 
 *      under the terms of the GNU General Public License as published by the 
 *      Free Software  Foundation; either version 2 of the License, or (at your
 *      option) any later version. 
 *
 *      This program is distributed in the hope that it will be useful, but 
 *      WITHOUT ANY WARRANTY; without even the implied warranty of 
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 *      Public License for more details. You should have received a copy of the
 *      GNU General Public License along with this program; if not, write to the
 *      Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 *      02111-1307 USA
 */

package ch.randelshofer.scorm;

import ch.randelshofer.scorm.cam.*;
import java.awt.*;
import java.awt.print.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.tree.TreeNode;

/**
 * CourseModelPrinter.
 * Prints all items of the selected organization.
 *
 * @author  Werner Randelshofer, Staldenmattweg 2, CH-6405 Immensee, Switzerland
 * @version 1.0 March 17, 2003 Created.
 */
public class CourseModelPrinter implements Pageable, Printable{
    private CourseModel model;
    private ArrayList<ItemElement> pageList;
    private URL baseURL;
    private JTextPane textPane;
    
    /** Creates a new instance. */
    public CourseModelPrinter(CourseModel model) {
        this.model = model;
        
        baseURL = model.getIMSManifestDocument().getManifestURL();
        
        textPane = new JTextPane();
        EditorKit kit = new HTMLEditorKit() {
            public Document createDefaultDocument() {
                Document d = super.createDefaultDocument();
                ((AbstractDocument) d).setAsynchronousLoadPriority(-1);
                return d;
            }
        };
        textPane.setEditorKitForContentType("content/unknown",kit);
        textPane.setEditorKit(kit);
        textPane.setEditable(false);
        
    }
    public void print() throws PrinterException {
        OrganizationElement org = model.getSelectedOrganization();
        
        pageList = new ArrayList<>();
        if (model.getStructure() == CourseModel.STRUCTURE_LAYERED) {
            boolean isFirstLayer = true;
            for (String layerTitle : org.getDistinctColumnTitles()) {
                for (ItemElement rowItem : org.getItemList()) {
                    if (isFirstLayer && rowItem.getItemList().size() == 0) {
                        if (rowItem.getIdentifierref() != null) {
                            pageList.add(rowItem);
                        }
                    } else {
                        for (ItemElement columnItem : rowItem.getItemList()) {
                            if (columnItem.getTitle().equals(layerTitle)) {
                                Enumeration<TreeNode> enm = columnItem.preorderEnumeration();
                                while (enm.hasMoreElements()) {
                                    Object o = enm.nextElement();
                                    if (o instanceof ItemElement) {
                                        ItemElement item = (ItemElement) o;
                                        if (item.getIdentifierref() != null) {
                                            pageList.add(item);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                isFirstLayer = false;
            }
        } else {
            Enumeration<TreeNode> enm = org.preorderEnumeration();
            while (enm.hasMoreElements()) {
                Object o = enm.nextElement();
                if (o instanceof ItemElement) {
                    ItemElement item = (ItemElement) o;
                    if (item.getIdentifierref() != null) {
                        pageList.add(item);
                    }
                }
            }
        }
        
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName(org.getTitle());
        job.setPageable(this);
        
        if (job.printDialog()) job.print();
    }
    public int getNumberOfPages() {
        return pageList.size();
    }
    public PageFormat getPageFormat(int pageIndex) {
        Paper paper = new Paper();
        double ppcm = 72.0 / 2.54;
        paper.setSize(21 * ppcm, 29.7 * ppcm);
        paper.setImageableArea(2 * ppcm, 1 * ppcm, 17 * ppcm, 27.7 * ppcm);
        
        PageFormat format = new PageFormat();
        format.setOrientation(PageFormat.PORTRAIT);
        format.setPaper(paper);
        return format;
    }
    public Printable getPrintable(int pageIndex)
    throws IndexOutOfBoundsException {
        if (pageIndex < 0 || pageIndex >= pageList.size()) throw new IndexOutOfBoundsException();
        return this;
    }
    public int print(Graphics g, PageFormat format, int pageIndex)
    throws PrinterException {
        if (pageIndex < 0 || pageIndex >= pageList.size()) return Printable.NO_SUCH_PAGE;
        /*
        System.out.print("PageFormat orientation:");
        switch (format.getOrientation()) {
            case PageFormat.LANDSCAPE : System.out.print("Landscape"); break;
            case PageFormat.PORTRAIT : System.out.print("Portrait"); break;
            case PageFormat.REVERSE_LANDSCAPE : System.out.print("Reverse Landscape"); break;
            default : System.out.print("Unknown ?"); break;
        }
        System.out.println(" width:"+format.getWidth()+" height:"+format.getHeight());
        System.out.print(" imgx:"+format.getImageableX()+" imgy:"+format.getImageableY());
        System.out.print(" imgwidth:"+format.getImageableWidth()+" imgheight:"+format.getImageableHeight());
         */
        
        // Get the ItemElement and the ResourceElement
        // -------------------------------------------
        ItemElement item = pageList.get(pageIndex);
        Enumeration<TreeNode> enm = model.getIMSManifestDocument().getResourcesElement().preorderEnumeration();
        ResourceElement resource = null;
        while (enm.hasMoreElements()) {
            Object o = enm.nextElement();
            if (o instanceof ResourceElement) {
                resource = (ResourceElement) o;
                if (item.getIdentifierref().equals(resource.getIdentifier())) {
                    break;
                }
            }
        }
        // Print the header
        // ----------------
        Graphics2D headerGraphics = (Graphics2D) g.create(
        (int) format.getImageableX(), (int) format.getImageableY(),
        (int) format.getImageableWidth(), getHeaderHeight()
        );
        printHeader(headerGraphics, format, pageIndex, item, resource);
        headerGraphics.dispose();
        
        // Print the content area
        // ----------------------
        try {
            if (item.getIdentifierref().equals(resource.getIdentifier())) {
                
                // The following is done to make sure the content of the text
                // pane fits into the page
                Graphics2D contentGraphics = (Graphics2D) g.create(
                (int) format.getImageableX(),
                (int) format.getImageableY() + getHeaderHeight(),
                (int) format.getImageableWidth(),
                (int) format.getImageableHeight() - getHeaderHeight() - getFooterHeight()
                );
                Dimension preferredSize = textPane.getPreferredSize();
                textPane.setBounds(
                0, 0, preferredSize.width, preferredSize.height
                );
                double xfactor = (format.getImageableWidth()) / (double) preferredSize.width;
                double yfactor = (format.getImageableHeight() - getHeaderHeight() - getFooterHeight()) / (double) preferredSize.height;
                double factor = Math.min(xfactor, yfactor);
                contentGraphics.scale(factor, factor);
                
                URL url = new URL(baseURL, resource.getHRef());
                textPane.setPage(url);
                textPane.print(contentGraphics);
                
                // dispose the graphics object.
                contentGraphics.dispose();
                
                g.drawRect(
                (int) format.getImageableX() + 1,
                (int) format.getImageableY() + getHeaderHeight() + 1,
                (int) (preferredSize.width * factor) - 2,
                (int) (preferredSize.height * factor) - 2
                );
            } else {
                g.drawString("Resource missing: Item identifierref="+item.getIdentifierref(), 100, 100);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Print the footer
        // ----------------
        Graphics2D footerGraphics = (Graphics2D) g.create(
        (int) format.getImageableX(),
        (int) (format.getImageableY() + format.getImageableHeight()) - getFooterHeight(),
        (int) format.getImageableWidth(),
        getFooterHeight()
        );
        printFooter(footerGraphics, format, pageIndex, item, resource);
        footerGraphics.dispose();
        
        return Printable.PAGE_EXISTS;
    }
    
    private void printHeader(Graphics2D g, PageFormat format, int pageIndex, ItemElement item, ResourceElement resource) {
        int y = 0;
        int x = 0;
        int width = (int) format.getImageableWidth();
        
        g.setFont(new Font("Dialog", Font.PLAIN, 10));
        FontMetrics fm = g.getFontMetrics();
        
        StringBuffer buf = new StringBuffer(item.getTitle());
        AbstractElement element = (AbstractElement) item.getParent();
        while (element != null && element instanceof ItemElement) {
            buf.insert(0, ':');
            buf.insert(0, ((ItemElement) element).getTitle());
            element = (AbstractElement) element.getParent();
        }
        g.drawString(buf.toString() , 0 ,y += 12);
        
        String str = resource.getHRef();
        x = width - fm.stringWidth(str);
        g.drawString(str, x , y);
        
    }
    private int getHeaderHeight() {
        return 50;
    }
    private void printFooter(Graphics2D g, PageFormat format, int pageIndex, ItemElement item, ResourceElement resource) {
        int y = 0;
        int x = 0;
        int width = (int) format.getImageableWidth();
        g.setFont(new Font("Dialog", Font.PLAIN, 10));
        FontMetrics fm = g.getFontMetrics();
        
        g.setColor(Color.black);
        String str = model.getSelectedOrganization().getTitle();
        if (str != null) {
            g.drawString(str, x, y += 12);
        } else {
            y += 12;
        }
        
        str = DateFormat.getDateInstance().format(new Date());
        x = (width - fm.stringWidth(str)) / 2;
        g.drawString(str, x, y);
        
        str = (pageIndex + 1) + "/" + getNumberOfPages();
        x = width - fm.stringWidth(str);
        g.drawString(str, x, y);
    }
    private int getFooterHeight() {
        return 50;
    }
}
