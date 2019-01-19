/* @(#)AbstractButton.java * * Copyright (c) 1999-2002 Werner Randelshofer * Staldenmattweg 2, CH-6405 Immensee, Switzerland * All rights reserved. * * This software is the confidential and proprietary information of * Werner Randelshofer. ("Confidential Information").  You shall not * disclose such Confidential Information and shall use it only in * accordance with the terms of the license agreement you entered into * with Werner Randelshofer. */package ch.randelshofer.gui;import java.awt.*;import ch.randelshofer.gui.event.*;import java.awt.ItemSelectable;import java.awt.event.ActionEvent;import java.awt.event.ActionListener;import java.awt.event.ItemEvent;import java.awt.event.ItemListener;import javax.swing.event.ChangeEvent;import javax.swing.event.ChangeListener;import javax.swing.event.EventListenerList;import javax.swing.BoundedRangeModel;import javax.swing.DefaultBoundedRangeModel;import javax.swing.Icon;import javax.swing.JComponent;import java.awt.event.MouseListener;import java.awt.event.MouseEvent;/** * Abstract super class for 'Swing'-like ligthweigths buttons on JDK 1.0.2. * * @author	Werner Randelshofer, Staldenmattweg 2, CH-6405 Immensee, Switzerland. * @version     1.2.1 2003-04-23 References to deprecated Java API's removed. * <br>1.2 2003-04-22  Support for background images added.	 * <br>1.1  	1999-05-30	DefaultButtonModel integrated into view. * <br>history	1.0.1	1999-05-02	Better compatibility with Netscape Navigator. * <br>history	1.0  	1999-02-21	Created. */public class AbstractButtonextends JComponent //Canvasimplements ItemSelectable, MouseListener {    private final static long serialVersionUID=1L;    private ChangeEvent changeEvent_;    private Dimension preferredSize_, minimumSize_;    private boolean isEnabled_  = true, isPressed_, isSelected_, isArmed_;    private EventListenerList listenerList_ = new EventListenerList();    private String actionCommand_;    private Icon selectedIcon_, unselectedIcon_;    private Image enabledBgImage_, pressedBgImage_;        public AbstractButton() {        addMouseListener(this);    }            /** Enables or disables the button. */    public void setEnabled(boolean b) {        if (b != isEnabled_) {            isEnabled_ = b;            fireStateChanged();            repaint();        }    }        /**     * Indicates if the button can be selected or pressed by an input device     * (such as a mouse pointer).     */    public boolean isEnabled() {        return isEnabled_;    }        /** Sets the button to pressed or unpressed. */    public void setPressed(boolean b) {        if (b != isPressed_) {            isPressed_ = b;            if (b == false) {                fireActionPerformed(                new ActionEvent(this,ActionEvent.ACTION_PERFORMED,getActionCommand())                );            }            fireStateChanged();            repaint();        }    }        /** Indicates if the button has been pressed. */    public boolean isPressed() {        return isPressed_;    }        /** Selects or deselects the button. */    public void setSelected(boolean b) {        if (b != isSelected_) {            isSelected_ = b;            fireItemStateChanged(            new ItemEvent(            this,ItemEvent.ITEM_STATE_CHANGED,this,            b ? ItemEvent.SELECTED : ItemEvent.DESELECTED            )            );            fireStateChanged();            repaint();        }    }        /** Indicates if the button has been selected. */    public boolean isSelected() {        return isSelected_;    }        /** Arms or unarms the button. */    public void setArmed(boolean b) {        if (b != isArmed_) {            isArmed_ = b;            fireStateChanged();            repaint();        }    }        /** Indicates if the button is armed. */    public boolean isArmed() {        return isArmed_;    }        /** Sets the action command of the button. */    public void setActionCommand(String command) {        actionCommand_ = command;    }        /** Gets the action command of the button. */    public String getActionCommand() {        return actionCommand_;    }        public void setSelectedIcon(Icon icon) {        selectedIcon_ = icon;    }        public void setUnselectedIcon(Icon icon) {        unselectedIcon_ = icon;    }        public void setEnabledBackgroundImage(Image img) {        enabledBgImage_ = img;    }    public void setPressedBackgroundImage(Image img) {        pressedBgImage_ = img;    }    public void setIcon(Icon icon) {        setUnselectedIcon(icon);    }        public Icon getIcon() {        return getUnselectedIcon();    }        public Icon getUnselectedIcon() {        return unselectedIcon_;    }        public Icon getSelectedIcon() {        return selectedIcon_;    }        //public void paint(Graphics g) {    public void paintComponent(Graphics g) {        Dimension s = getSize();        int width = s.width;        int height = s.height;                if (enabledBgImage_ == null) {        g.setColor(getForeground());        g.drawRect(0,0,width-1,height-1);                if (! isEnabled_) {            g.setColor(Color.lightGray);            g.fillRect(1,1,width-1,height-1);        }        if (isPressed_ && isArmed_) {            g.setColor(Color.gray.darker());            g.fillRect(1,1,width-3,height-3);            g.setColor(Color.darkGray);            g.drawLine(1,1,width-2,1);            g.drawLine(1,1,1,height-2);            g.setColor(Color.gray);            g.drawLine(2,height-2,width-2,height-2);            g.drawLine(width-2,height-2,width-2,2);        } else {            g.setColor(Color.lightGray);            g.fillRect(1,1,width-2,height-2);            g.setColor(Color.white);            g.drawLine(1,1,width-3,1);            g.drawLine(1,1,1,height-3);        }        } else {        if (isPressed_ && isArmed_ && pressedBgImage_ != null) {            g.drawImage(pressedBgImage_, 0, 0, this);        } else {            g.drawImage(enabledBgImage_, 0, 0, this);        }        }                Icon icon = (isSelected_ && selectedIcon_ != null) ? selectedIcon_ : unselectedIcon_;        if (icon != null) {            int x = (width - icon.getIconWidth()) / 2;             int y = (height - icon.getIconHeight()) / 2;             icon.paintIcon(this, g, x, y);        }    }        public Dimension getPreferredSize() {        if (preferredSize_ == null) {            Icon icon = getIcon();            if (icon != null) {                Dimension d = new Dimension(icon.getIconWidth()+4,icon.getIconHeight()+4);                return d;            } else {                return super.getPreferredSize();            }        }        return preferredSize_;    }        public void setPreferredSize(Dimension d) {        preferredSize_ = d;    }        public Dimension getMinimumSize() {        if (minimumSize_ == null) {            return super.getMinimumSize();        }        return minimumSize_;    }        public void setMinimumSize(Dimension d) {        minimumSize_ = d;    }    /*    public boolean mouseEnter(Event event, int x, int y) {        setArmed(true);        repaint();        return true;    }        public boolean mouseExit(Event event, int x, int y) {        setArmed(false);        repaint();        return true;    }        public boolean mouseDown(Event event, int x, int y) {        System.out.println("AbstractButton.mouseDown(...)");        setPressed(true);        repaint();        return true;    }        public boolean mouseUp(Event event, int x, int y) {        setPressed(false);        repaint();        return true;    }    */    /** Adds a change listener to the button. */    public void addChangeListener(ChangeListener l) {        listenerList_.add(ChangeListener.class,l);    }        /** Removes a change listener from the button. */    public void removeChangeListener(ChangeListener l) {        listenerList_.remove(ChangeListener.class,l);    }        /** Adds an action listener to the button. */    public void addActionListener(ActionListener l) {        listenerList_.add(ActionListener.class,l);    }        /** Removes an action listener from the button. */    public void removeActionListener(ActionListener l) {        listenerList_.remove(ActionListener.class,l);    }        /** Adds an item listener to the button. */    public void addItemListener(ItemListener l) {        listenerList_.add(ItemListener.class,l);    }        /** Removes an item listener from the button. */    public void removeItemListener(ItemListener l) {        listenerList_.remove(ItemListener.class,l);    }            /*         * Notify all listeners that have registered interest for         * notification on this event type.  The event instance         * is lazily created using the parameters passed into         * the fire method.         *         * @param e the ActionEvent to deliver to listeners         * @see EventListenerList         */    protected void fireActionPerformed(ActionEvent e) {        // Guaranteed to return a non-null array        Object[] listeners = listenerList_.getListenerList();        // Process the listeners last to first, notifying        // those that are interested in this event        for (int i = listeners.length-2; i>=0; i-=2) {            if (listeners[i]==ActionListener.class) {                // Lazily create the event:                // if (changeEvent == null)                // changeEvent = new ChangeEvent(this);                ((ActionListener) listeners[i+1]).actionPerformed(e);            }        }    }            /*         * Notify all listeners that have registered interest for         * notification on this event type.  The event instance         * is lazily created using the parameters passed into         * the fire method.         *         * @param e the ItemEvent to deliver to listeners         * @see EventListenerList         */    protected void fireItemStateChanged(ItemEvent e) {        // Guaranteed to return a non-null array        Object[] listeners = listenerList_.getListenerList();        // Process the listeners last to first, notifying        // those that are interested in this event        for (int i = listeners.length-2; i>=0; i-=2) {            if (listeners[i]==ItemListener.class) {                // Lazily create the event:                // if (changeEvent == null)                // changeEvent = new ChangeEvent(this);                ((ItemListener) listeners[i+1]).itemStateChanged(e);            }        }    }            /*         * Notify all listeners that have registered interest for         * notification on this event type.  The event instance         * is lazily created using the parameters passed into         * the fire method.         *         * @see EventListenerList         */    protected void fireStateChanged() {        // Guaranteed to return a non-null array        Object[] listeners = listenerList_.getListenerList();        // Process the listeners last to first, notifying        // those that are interested in this event        for (int i = listeners.length-2; i >= 0; i -= 2) {            if (listeners[i]==ChangeListener.class) {                // Lazily create the event:                if (changeEvent_ == null) {                    changeEvent_ = new ChangeEvent(this);                }                ((ChangeListener) listeners[i+1]).stateChanged(changeEvent_);            }        }    }        public void stateChanged(ChangeEvent event) {        repaint();        fireStateChanged();    }        public Object[] getSelectedObjects() {        return null;    }        public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {    }        public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {        setArmed(true);        repaint();    }        public void mouseExited(java.awt.event.MouseEvent mouseEvent) {        setArmed(false);        repaint();    }        public void mousePressed(java.awt.event.MouseEvent mouseEvent) {        setPressed(true);        repaint();    }        public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {        setPressed(false);        repaint();    }    }