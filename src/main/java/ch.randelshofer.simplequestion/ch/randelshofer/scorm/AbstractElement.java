/* @(#)AbstractElement.java
 *
 * Copyright (c) 2003-2006 Werner Randelshofer
 * Staldenmattweg 2, Immensee, CH-6405, Switzerland.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Werner Randelshofer. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Werner Randelshofer.
 */


package ch.randelshofer.scorm;

import ch.randelshofer.scorm.cam.*;
import java.io.*;
import java.util.*;



import javax.swing.tree.*;
import org.jhotdraw.util.ResourceBundleUtil;

/**
 * Represents a SCORM CAM element.
 *
 * Reference:
 * ADL (2001c). Advanced Distributed Learning.
 * Sharable Content Object Reference Model (SCORM(TM)) Version 1.2.
 * The SCORM Content Aggregation Model. October 1, 2001.
 * Internet (2003-01-20): http://www.adlnet.org
 *
 * @author Werner Randelshofer, Staldenmattweg 2, Immensee, CH-6405, Switzerland
 * @version 1.2 2006-10-07 Got rid of HTML output in method getInfo because
 * of bug http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4988885
 * <br>1.1 2003-11-03 Method consumeFileNames added.
 * <br>1.0 2003-10-30 HTML output in method toString changed. 
 * <br>0.19.5 2003-05-09 Method validate() did not return false when
 * the identifer was invalid.
 * <br>0.19.4 2003-04-02 Method validateSubtree added.
 * <br>0.1 2003-02-02 Created.
 */
public abstract class AbstractElement extends DefaultMutableTreeNode {
    private final static long serialVersionUID=1L;
    /**
     * This attribute is set by validate().
     */
    private boolean isIdentifierValid = true;
    protected boolean isValid = false;
    protected static ResourceBundleUtil labels =new ResourceBundleUtil( ResourceBundle.getBundle("ch.randelshofer.scorm.Labels"));
    
    /** Creates a new instance of AbstractElement */
    public AbstractElement() {
    }
    
    
    /**
     * Dumps the contents of this subtree into the provided string buffer.
     */
    public abstract void dump(StringBuffer buf, int depth);
    
    public String getIdentifier() {
        return null;
    }
    
    /**
     * Searches this subtree for a AbstractElement with the specified identifier.
     * This node is regarded as part of the subtree.
     * Performance note: this method uses a linear search algorithm!
     */
    public AbstractElement findSubtreeByIdentifier(String identifier) {
        Enumeration<TreeNode> enm = preorderEnumeration();
        while (enm.hasMoreElements()) {
            AbstractElement element = (AbstractElement) enm.nextElement();
            if (element.getIdentifier() != null 
            && element.getIdentifier().equals(identifier)) {
                return element;
            }
        }
        return null;
    }
    /**
     * Searches the children of this node for a AbstractElement with the specified
     * identifier. Performance note: this method uses a linear search algorithm!
     */
    public AbstractElement findChildByIdentifier(String identifier) {
        Enumeration<TreeNode> enm = children();
        while (enm.hasMoreElements()) {
            AbstractElement element = (AbstractElement) enm.nextElement();
            if (element.getIdentifier() != null 
            && element.getIdentifier().equals(identifier)) {
                return element;
            }
        }
        return null;
    }
    
    public void indent(PrintWriter out, int depth) {
        for (int i=0; i < depth; i++) { out.print("  "); }
    }
    
    /**
     * Validates this CAM element and the subtree starting from this element
     * recursively.
     *
     * This method calls recursively the validate() methods of all nodes in
     * this subtree.
     *
     * @return Returns true if all elements in this subtree are valid.
     * Returns false if one or more elements are invalid.
     */
    public boolean validateSubtree() {
        boolean isValid = validate();
        Enumeration<TreeNode> enm = children();
        while (enm.hasMoreElements()) {
            AbstractElement child = (AbstractElement) enm.nextElement();
            if (! child.validateSubtree()) isValid = false;
        }
        return isValid;
    }
    /**
     * Validates this CAM element.
     *
     * @return Returns true if this elements is valid.
     */
    public boolean validate() {
        String thisID = this.getIdentifier() ;
        if (thisID == null) return isValid = true;
        
        isIdentifierValid = true;
        IMSManifestDocument root = getIMSManifestDocument();
        Enumeration<TreeNode> enm = root.preorderEnumeration();
        while (enm.hasMoreElements()) {
            AbstractElement node = (AbstractElement) enm.nextElement();
            if (node != this && node.getIdentifier() != null
            && node.getIdentifier().equals(thisID)
            && thisID != null) {
                isIdentifierValid = false;
                break;
            }
        }
        return isValid = isIdentifierValid;
    }
    
    public boolean isValid() {
        return isValid;
    }
    
    public IMSManifestDocument getIMSManifestDocument() {
        return (getRoot().getChildCount() == 0) ? null : (IMSManifestDocument) getRoot().getChildAt(0);
    }
    
    /**
     * The return value of this method is unspecified until
     * validate() has been done.
     *
     * @return Returns true if the identifier of this element is unique in
     * this tree.
     */
    public boolean isIdentifierValid() {
        return isIdentifierValid;
    }
    
    public String getInfo() {
        return (isValid()) 
        ? labels.getString("cam.elementIsValid")
        : labels.getString("cam.elementIsInvalid")
        ;
    }
    
    /**
     * Removes all file names in the set, which are referenced by this
     * CAM Element.
     */
    public void consumeFileNames(Set<String> fileNames) {
    }
}
