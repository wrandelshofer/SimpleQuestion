/*
 * @(#)ImageButtonUI.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.gui.plaf;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonListener;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;

/**
 * ImageButtonUI implementation
 *
 * @author Werner Randelshofer
 */
public class ImageButtonUI
        extends BasicButtonUI
        implements PlafConstants {


    private final static ImageButtonUI imageButtonUI = new ImageButtonUI();

    private boolean defaults_initialized = false;

    protected Color focusColor;
    protected Color selectColor;
    protected Color disabledTextColor;


    // ********************************
    //          Create PLAF
    // ********************************
    public ImageButtonUI() {
    }


    public static ComponentUI createUI(JComponent c) {
        return new ImageButtonUI();
    }

    // ********************************
    //          Install
    // ********************************
    public void installDefaults(AbstractButton b) {
        super.installDefaults(b);
        if (!defaults_initialized) {
            PlafUtils.installBevelBorder(b, getPropertyPrefix() + "border");

            focusColor = UIManager.getColor(getPropertyPrefix() + "focus");
            selectColor = UIManager.getColor(getPropertyPrefix() + "select");
            disabledTextColor = UIManager.getColor(getPropertyPrefix() + "disabledText");

            LookAndFeel.installColors(b, getPropertyPrefix() + ".background", getPropertyPrefix() + ".foreground");

            defaults_initialized = true;
        }
    }

    public void uninstallDefaults(AbstractButton b) {
        super.uninstallDefaults(b);
        defaults_initialized = false;
    }

    // ********************************
    //         Create Listeners
    // ********************************
    protected BasicButtonListener createButtonListener(AbstractButton b) {
        return new ImageButtonListener(b);
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
    //          Paint Methods
    // ********************************
    public void paint(Graphics g, JComponent c) {
        g.setColor(c.getBackground());
        g.fillRect(0, 0, c.getWidth(), c.getHeight());
        ButtonModel m = ((AbstractButton) c).getModel();
        PlafUtils.paintBevel(c, g, 0, 0, c.getWidth(), c.getHeight(), true /*m.isEnabled()*/, m.isPressed() & m.isArmed(), m.isSelected());

        super.paint(g, c);
    }

    protected void paintButtonPressed(Graphics g, AbstractButton b) {
        // We don't paint button pressed, because
        // this has already been done by PlafUtils.paintBevel
        // at the beginning of the paint method.
        /*
        if ( b.isContentAreaFilled() ) {
            Dimension size = b.getSize();
	    g.setColor(getSelectColor());
	    g.fillRect(0, 0, size.width, size.height);
	}
         */
    }

    protected void paintFocus(Graphics g, AbstractButton b,
                              Rectangle viewRect, Rectangle textRect, Rectangle iconRect) {
        // We don't paint focus
        /**
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


    protected void paintText(Graphics g, JComponent c, Rectangle textRect, String text) {
        AbstractButton b = (AbstractButton) c;
        ButtonModel model = b.getModel();
        FontMetrics fm = g.getFontMetrics();

        /* Draw the Text */
        if (model.isEnabled()) {
            /*** paint the text normally */
            g.setColor(b.getForeground());
            BasicGraphicsUtils.drawString(g, text, model.getMnemonic(),
                    textRect.x,
                    textRect.y + fm.getAscent());
        } else {
            /*** paint the text disabled ***/
            g.setColor(getDisabledTextColor());
            BasicGraphicsUtils.drawString(g, text, model.getMnemonic(),
                    textRect.x, textRect.y + fm.getAscent());

        }
    }

}

class ImageButtonListener extends BasicButtonListener {

    public ImageButtonListener(AbstractButton b) {
        super(b);
    }

    public void focusGained(FocusEvent e) {
        Component c = (Component) e.getSource();
        c.repaint();
    }
}
   

