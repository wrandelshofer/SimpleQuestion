/* @(#)CornerLayout.java * * Copyright (c) 1999 Werner Randelshofer * Staldenmattweg 2, CH-6405 Immensee, Switzerland * All rights reserved. * * This software is the confidential and proprietary information of * Werner Randelshofer. ("Confidential Information").  You shall not * disclose such Confidential Information and shall use it only in * accordance with the terms of the license agreement you entered into * with Werner Randelshofer. */package ch.randelshofer.gui;import java.awt.BorderLayout;import java.awt.Component;import java.awt.Container;import java.awt.Dimension;import java.awt.Insets;import java.awt.LayoutManager2;/** * A layout manager similar to BorderLayout but with additional * slots for the four corners of a panel. * * @author Werner Randelshofer, Staldenmattweg 2, CH-6405 Immensee, Switzerland * @version 1.0  1999-10-19 */public class CornerLayout        extends BorderLayout        implements LayoutManager2, java.io.Serializable {    private final static long serialVersionUID = 1L;    Component north_, west_, east_, south_;    Component center_;    Component ne_, nw_, se_, sw_;    public static final String NORTH = "North";    public static final String SOUTH = "South";    public static final String EAST = "East";    public static final String WEST = "West";    public static final String CENTER = "Center";    public static final String NORTHEAST = "NE";    public static final String NORTHWEST = "NW";    public static final String SOUTHEAST = "SE";    public static final String SOUTHWEST = "SE";    /**     * Ugly trick to save some bytecode.     *     * @see #minimumLayoutSize     * @see #preferredsLayoutSize     */    private final static Dimension NULL_DIMENSION = new Dimension(0, 0);    public CornerLayout() {    }    public void addLayoutComponent(Component comp, Object constraints) {        synchronized (comp.getTreeLock()) {            if ((constraints == null) || (constraints instanceof String)) {                addLayoutComponent((String) constraints, comp);            } else {                throw new IllegalArgumentException("cannot add to layout: constraint must be a string (or null)");            }        }    }    /**     * @deprecated replaced by <code>addLayoutComponent(Component, Object)</code>.     */    public void addLayoutComponent(String name, Component comp) {        synchronized (comp.getTreeLock()) {            /* Special case:  treat null the same as "Center". */            if (name == null) {                name = CENTER;            }            /* Assign the component to one of the known regions of the layout.             */            if (name == CENTER) {                center_ = comp;            } else if (name == NORTH) {                north_ = comp;            } else if (name == SOUTH) {                south_ = comp;            } else if (name == EAST) {                east_ = comp;            } else if (name == WEST) {                west_ = comp;            } else if (name == NORTHEAST) {                ne_ = comp;            } else if (name == NORTHWEST) {                nw_ = comp;            } else if (name == SOUTHEAST) {                se_ = comp;            } else if (name == SOUTHWEST) {                sw_ = comp;            } else {                throw new IllegalArgumentException("cannot add to layout: unknown constraint: " + name);            }        }    }    public void removeLayoutComponent(Component comp) {        synchronized (comp.getTreeLock()) {            if (comp == center_) {                center_ = null;            } else if (comp == north_) {                north_ = null;            } else if (comp == south_) {                south_ = null;            } else if (comp == east_) {                east_ = null;            } else if (comp == west_) {                west_ = null;            } else if (comp == ne_) {                ne_ = null;            } else if (comp == nw_) {                nw_ = null;            } else if (comp == se_) {                se_ = null;            } else if (comp == sw_) {                sw_ = null;            }        }    }    public Dimension minimumLayoutSize(Container target) {        synchronized (target.getTreeLock()) {            Dimension dim = new Dimension(0, 0);            Component c = null;            Dimension dEast, dWest, dCenter, dNorth, dSouth;            if ((c = east_) != null) {                dEast = c.getMinimumSize();            } else {                dEast = NULL_DIMENSION;            }            if ((c = west_) != null) {                dWest = c.getMinimumSize();            } else {                dWest = NULL_DIMENSION;            }            if ((c = center_) != null) {                dCenter = c.getMinimumSize();            } else {                dCenter = NULL_DIMENSION;            }            if ((c = north_) != null) {                dNorth = c.getMinimumSize();            } else {                dNorth = NULL_DIMENSION;            }            if ((c = south_) != null) {                dSouth = c.getMinimumSize();            } else {                dSouth = NULL_DIMENSION;            }            dim.width = dWest.width + Math.max(Math.max(dNorth.width, dCenter.width), dSouth.width) + dEast.width;            dim.height = dNorth.height + Math.max(Math.max(dWest.height, dCenter.height), dEast.height) + dSouth.height;            Insets insets = target.getInsets();            dim.width += insets.left + insets.right;            dim.height += insets.top + insets.bottom;            return dim;        }    }    public Dimension preferredLayoutSize(Container target) {        synchronized (target.getTreeLock()) {            Dimension dim = new Dimension(0, 0);            Component c = null;            Dimension dEast, dWest, dCenter, dNorth, dSouth;            if ((c = east_) != null) {                dEast = c.getPreferredSize();            } else {                dEast = NULL_DIMENSION;            }            if ((c = west_) != null) {                dWest = c.getPreferredSize();            } else {                dWest = NULL_DIMENSION;            }            if ((c = center_) != null) {                dCenter = c.getPreferredSize();            } else {                dCenter = NULL_DIMENSION;            }            if ((c = north_) != null) {                dNorth = c.getPreferredSize();            } else {                dNorth = NULL_DIMENSION;            }            if ((c = south_) != null) {                dSouth = c.getPreferredSize();            } else {                dSouth = NULL_DIMENSION;            }            dim.width = dWest.width + Math.max(Math.max(dNorth.width, dCenter.width), dSouth.width) + dEast.width;            dim.height = dNorth.height + Math.max(Math.max(dWest.height, dCenter.height), dEast.height) + dSouth.height;            Insets insets = target.getInsets();            dim.width += insets.left + insets.right;            dim.height += insets.top + insets.bottom;            return dim;        }    }    public Dimension maximumLayoutSize(Container target) {        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);    }    public float getLayoutAlignmentX(Container parent) {        return 0.5f;    }    public float getLayoutAlignmentY(Container parent) {        return 0.5f;    }    public void invalidateLayout(Container target) {    }    public void layoutContainer(Container target) {        synchronized (target.getTreeLock()) {            Insets insets = target.getInsets();            int top = insets.top;            int bottom = target.getSize().height - insets.bottom;            int left = insets.left;            int right = target.getSize().width - insets.right;            int innerTop = north_ == null ? top : top + north_.getPreferredSize().height;            int innerBottom = south_ == null ? bottom : bottom - south_.getPreferredSize().height;            int innerLeft = west_ == null ? left : left + west_.getPreferredSize().width;            int innerRight = east_ == null ? right : right - east_.getPreferredSize().width;            if (north_ != null) {                north_.setBounds(innerLeft, top, innerRight - innerLeft, innerTop - top);            }            if (south_ != null) {                south_.setBounds(innerLeft, innerBottom, innerRight - innerLeft, bottom - innerBottom);            }            if (east_ != null) {                east_.setBounds(innerRight, innerTop, right - innerRight, innerBottom - innerTop);            }            if (west_ != null) {                west_.setBounds(left, innerTop, innerLeft - left, innerBottom - innerTop);            }            if (center_ != null) {                center_.setBounds(innerLeft, innerTop, innerRight - innerLeft, innerBottom - innerTop);            }            if (ne_ != null) {                ne_.setBounds(innerRight, top, right - innerRight, innerTop - top);            }            if (nw_ != null) {                nw_.setBounds(left, top, innerLeft - left, innerTop - top);            }            if (se_ != null) {                se_.setBounds(innerRight, innerBottom, right - innerRight, bottom - innerBottom);            }            if (sw_ != null) {                sw_.setBounds(left, innerBottom, innerLeft - left, bottom - innerBottom);            }        }    }}