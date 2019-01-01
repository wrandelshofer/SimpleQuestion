/* * @(#)ScaleFactorLayout.java  1.2  2002-02-05 * * Copyright (c) 1999-2000 Werner Randelshofer * Staldenmattweg 2, CH-6405 Immensee, Switzerland * mailto:werner.randelshofer@bluewin.ch * All rights reserved. * * This software is the confidential and proprietary information of  * Werner Randelshofer. ("Confidential Information").  You shall not * disclose such Confidential Information and shall use it only in * accordance with the terms of the license agreement you entered into * with Werner Randelshofer. */package ch.randelshofer.gui;import java.awt.*;/** * A scale factor layout lays out a container, arranging one single * component to fit into its parent while maintaining the aspect of * the component. * * Note: This layout can handle only one single component. * * The preferred size of the layout is computed by multiplying * the preferred size of the component with the scale factor * provided by the scale factor constraints. * * If the layout constraint 'scale to fit' is set to true, the * size of the component is adjusted to the current size of the * container. If the 'maintain aspect ratio' constraint is set * to true, the component keeps its aspect ratio. * * @author  Werner Randelshofer, Staldenmattweg 2, CH-6405 Immensee, Switzerland * @version 1.2 2002-02-06 'Scale to fit' property added. * <br>1.0.2   2000-06-13  Implementation of #layoutContainer improved. * <br>1.0.1   2000-06-12 reworked. * <br>1.0  1999-10-19 */public class ScaleFactorLayoutimplements LayoutManager2, java.io.Serializable {    private Component component;    private ScaleFactorConstraints constraints;    public void addLayoutComponent(Component comp, Object constraints) {        synchronized (comp.getTreeLock()) {            if ((constraints == null) || (constraints instanceof ScaleFactorConstraints)) {                component = comp;                this.constraints = (ScaleFactorConstraints)constraints;            } else {                throw new IllegalArgumentException("cannot add to layout: constraint must be a ScaleFactorConstraint (or null)");            }        }     }    /**     * @deprecated  replaced by <code>addLayoutComponent(Component, Object)</code>.     */    public void addLayoutComponent(String name, Component comp) {        synchronized (comp.getTreeLock()) {            if (name != null) {                addLayoutComponent(comp, null);            } else {                throw new IllegalArgumentException("cannot add to layout: constraint must be a ScaleFactorConstraint (or null)");            }        }    }    public void removeLayoutComponent(Component comp) {        synchronized (comp.getTreeLock()) {            if (component == comp) {                component = null;            }        }    }    public Dimension minimumLayoutSize(Container target) {        synchronized (target.getTreeLock()) {            Insets insets = target.getInsets();            return new Dimension(insets.left + insets.right, insets.top + insets.bottom);        }    }    public Dimension preferredLayoutSize(Container target) {        synchronized (target.getTreeLock()) {            if (component != null) {                Insets insets = target.getInsets();                Dimension preferred = component.getPreferredSize();                int width;                int height;                if (constraints == null) {                    width = insets.left + insets.right + preferred.width;                    height = insets.top + insets.bottom + preferred.height;                } else {                    width = insets.left + insets.right + (int) (preferred.width * constraints.getFactorX());                    height = insets.top + insets.bottom + (int) (preferred.height * constraints.getFactorY());                }                                return new Dimension(width, height);            } else {                return minimumLayoutSize(target);            }        }    }    public Dimension maximumLayoutSize(Container target) {        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);    }    public float getLayoutAlignmentX(Container parent) {        return 0.5f;    }    public float getLayoutAlignmentY(Container parent) {        return 0.5f;    }    public void invalidateLayout(Container target) {    }    public void layoutContainer(Container target) {        synchronized (target.getTreeLock()) {            if (component != null) {                Insets insets = target.getInsets();                Dimension size = target.getSize();                Dimension preferred = component.getPreferredSize();                int width;                int height;                double scaleFactor;                double factorX, factorY;                boolean maintainAspectRatio;                boolean scaleToFit;                if (constraints != null) {                    factorX = constraints.getFactorX();                    factorY = constraints.getFactorY();                    maintainAspectRatio = constraints.isMaintainAspectRatio();                    scaleToFit = constraints.isScaleToFit();                } else {                    factorX = 1.0;                    factorY = 1.0;                    maintainAspectRatio = true;                    scaleToFit = true;                }                if (scaleToFit) {                    if (maintainAspectRatio) {                        scaleFactor = Math.min(                            (size.width - insets.left - insets.top) / (preferred.width * factorX),                            (size.height - insets.top - insets.bottom) / (preferred.height * factorY)                        );                        width = (int) (preferred.width * factorX * scaleFactor);                        height = (int) (preferred.height * factorY * scaleFactor);                        component.setBounds(                            insets.left + (size.width - insets.left - insets.right - width) / 2,                            insets.top +  (size.height - insets.top - insets.bottom - height) / 2,                            width,                            height                        );                    } else {                        component.setBounds(                            insets.left,                            insets.top,                            size.width - insets.left - insets.right,                            size.height - insets.top - insets.bottom                        );                    }                } else {                    width = (int) (preferred.width * factorX);                    height = (int) (preferred.height * factorY);                    component.setBounds(                        (size.width - width) / 2,                        (size.height - height) / 2,                        width,                        height                    );                }            }        }    }}