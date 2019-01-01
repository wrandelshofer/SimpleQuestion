/*
 * ImageToggleButtonUI.java
 *
 * Created on October 17, 2001, 10:24 AM
 */

package ch.randelshofer.gui.plaf;

import java.awt.*;
import java.awt.event.*;
import javax.swing.plaf.basic.BasicToggleButtonUI;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicGraphicsUtils;

import java.io.Serializable;

/**
 * MetalToggleButton implementation
 *
 * @author  Werner Randelshofer
 * @version 
 */
public class ImageToggleButtonUI extends BasicToggleButtonUI implements PlafConstants {

    private static final ImageToggleButtonUI imageToggleButtonUI = new ImageToggleButtonUI();

    protected Color focusColor;
    protected Color selectColor;
    protected Color disabledTextColor;

    private boolean defaults_initialized = false;

    // ********************************
    //        Create PLAF
    // ********************************
    public ImageToggleButtonUI() {
    }

    public static ComponentUI createUI(JComponent b) {
        //return imageToggleButtonUI; why does this not work?
        return new ImageToggleButtonUI();
    }

    // ********************************
    //        Install Defaults 
    // ********************************
    public void installDefaults(AbstractButton b) {
        super.installDefaults(b);
	if(!defaults_initialized) {
            //LookAndFeel.installBorder(b, getPropertyPrefix() + "border");
            PlafUtils.installBevelBorder(b, getPropertyPrefix() + "border");

            LookAndFeel.installColors(b, getPropertyPrefix()+".background", getPropertyPrefix()+".foreground");

            focusColor = UIManager.getColor(getPropertyPrefix() + "focus");
	    selectColor = UIManager.getColor(getPropertyPrefix() + "select");
	    disabledTextColor = UIManager.getColor(getPropertyPrefix() + "disabledText");
	    defaults_initialized = true;
	}
    }

    protected void uninstallDefaults(AbstractButton b) {
	super.uninstallDefaults(b);
	defaults_initialized = false;
    }

    // ********************************
    //         Default Accessors 
    // ********************************
    protected Color getSelectColor() {
	return selectColor;
    }

    protected Color getDisabledTextColor() {
	return disabledTextColor;
    }

    protected Color getFocusColor() {
	return focusColor;
    }


    // ********************************
    //        Paint Methods
    // ********************************
    public void paint(Graphics g, JComponent c) {
        AbstractButton b = (AbstractButton) c;
        ButtonModel model = b.getModel();

        g.setColor(c.getBackground());
        g.fillRect(0, 0, c.getWidth(), c.getHeight());
        PlafUtils.paintBevel(c, g, 0, 0, c.getWidth(), c.getHeight(), true/* model.isEnabled()*/, model.isPressed() & model.isArmed(), model.isSelected());

	
        Dimension size = b.getSize();
        FontMetrics fm = g.getFontMetrics();

        Insets i = c.getInsets();

        Rectangle viewRect = new Rectangle(size);

        viewRect.x += i.left;
        viewRect.y += i.top;
        viewRect.width -= (i.right + viewRect.x);
        viewRect.height -= (i.bottom + viewRect.y);

        Rectangle iconRect = new Rectangle();
        Rectangle textRect = new Rectangle();

        Font f = c.getFont();
        g.setFont(f);

        // layout the text and icon
        String text = SwingUtilities.layoutCompoundLabel(
            c, fm, b.getText(), b.getIcon(),
            b.getVerticalAlignment(), b.getHorizontalAlignment(),
            b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
            viewRect, iconRect, textRect, b.getText() == null ? 0 : getDefaultTextIconGap(b)
        );


        g.setColor(b.getBackground());

        if (model.isArmed() && model.isPressed() || model.isSelected()) {
            paintButtonPressed(g,b);
	} else {
	    Insets insets = b.getInsets();
	    Insets margin = b.getMargin();
            /*
	    g.fillRect(insets.left - margin.left,
		       insets.top - margin.top, 
		       size.width - (insets.left-margin.left) - (insets.right - margin.right),
		       size.height - (insets.top-margin.top) - (insets.bottom - margin.bottom));
             */
	}
	
        // Paint the Icon
        if(b.getIcon() != null) { 
            paintIcon(g, b, iconRect);
        }
	
        // Draw the Text
        if(text != null && !text.equals("")) {
            paintText(g, b, textRect, text);
        }
	
        // draw the dashed focus line.
        if (b.isFocusPainted() && b.hasFocus()) {
	    paintFocus(g, b, viewRect, textRect, iconRect);
        }
    }


    protected void paintButtonPressed(Graphics g, AbstractButton b) {
        // We don't paint button pressed,
        // because this is done by PlafUtils.paintBevel
        /*
        if ( b.isContentAreaFilled() ) {
            Dimension size = b.getSize();
	    g.setColor(getSelectColor());
	    g.fillRect(0, 0, size.width, size.height);
	}
         */
    }

    protected void paintText(Graphics g, AbstractButton b, Rectangle textRect, String text) {
	ButtonModel model = b.getModel();
	FontMetrics fm = g.getFontMetrics();

	/* Draw the Text */
	if(model.isEnabled()) {
	    /*** paint the text normally */
	    g.setColor(b.getForeground());
	    BasicGraphicsUtils.drawString(g,text, model.getMnemonic(), textRect.x, textRect.y + fm.getAscent());
	}
	else {
	    /*** paint the text disabled ***/
	    if (model.isSelected()) {
		g.setColor(b.getBackground());
	    } else {
	        g.setColor(getDisabledTextColor());
	    }
	    BasicGraphicsUtils.drawString(g, text, model.getMnemonic(), textRect.x, textRect.y + fm.getAscent());

	}
    }

    protected void paintFocus(Graphics g, AbstractButton b,
			      Rectangle viewRect, Rectangle textRect, Rectangle iconRect){
        // Don't paint focus
        /*                              
        Rectangle focusRect = new Rectangle();
	String text = b.getText();
	boolean isIcon = b.getIcon() != null;

        // If there is text
        if ( text != null && !text.equals( "" ) ) {
  	    if ( !isIcon ) {
	        focusRect.setBounds( textRect );
	    }
	    else {
	        focusRect.setBounds( iconRect.union( textRect ) );
	    }
        }
        // If there is an icon and no text
        else if ( isIcon ) {
  	    focusRect.setBounds( iconRect );
        }

        g.setColor(getFocusColor());
	g.drawRect((focusRect.x-1), (focusRect.y-1),
		  focusRect.width+1, focusRect.height+1);
         */
	
    }
}
