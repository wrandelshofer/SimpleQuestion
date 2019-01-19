/* @(#)PlafUtils.java
 * Copyright © Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.gui.plaf;


import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Toolkit;
/**
 * This is a dumping ground for random stuff we want to use in several places.
 * @author  Werner Randelshofer
 * @version 
 */
public class PlafUtils 
implements PlafConstants {
    protected static BevelRenderer[][] bevelRenderer;
 /*= {
        // Bevel rounded at left and right
        { 
            new BevelRenderer( // !selected,!pressed,!disabled
                Toolkit.getDefaultToolkit().createImage(ImageButtonUI.class.getResource("images/Metal/Bevel.0.png")),
                new Insets(8, 10, 16, 10)
            ),
            new BevelRenderer( // !selected,!pressed, disabled
                Toolkit.getDefaultToolkit().createImage(ImageButtonUI.class.getResource("images/Metal/Bevel.1.png")),
                new Insets(8, 10, 16, 10)
            ),
            new BevelRenderer( // !selected, pressed,!disabled
                Toolkit.getDefaultToolkit().createImage(ImageButtonUI.class.getResource("images/Metal/Bevel.2.png")),
                new Insets(8, 10, 16, 10)
            ),
            
            null, // !selected, pressed, disabled -> illegal combination
            
            new BevelRenderer( //  selected,!pressed,!disabled
                Toolkit.getDefaultToolkit().createImage(ImageButtonUI.class.getResource("images/Metal/Bevel.4.png")),
                new Insets(8, 10, 16, 10)
            ),
            new BevelRenderer( // selected,!pressed, disabled
                Toolkit.getDefaultToolkit().createImage(ImageButtonUI.class.getResource("images/Metal/Bevel.5.png")),
                new Insets(8, 10, 16, 10)
            ),
            new BevelRenderer( // selected, pressed,!disabled
                Toolkit.getDefaultToolkit().createImage(ImageButtonUI.class.getResource("images/Metal/Bevel.6.png")),
                new Insets(8, 10, 16, 10)
            ),
        },
        
        // Bevel rounded at left only
        { 
            new BevelRenderer( // !selected,!pressed,!disabled
                Toolkit.getDefaultToolkit().createImage(ImageButtonUI.class.getResource("images/Metal/BevelLeft.0.png")),
                new Insets(8, 10, 16, 0)
            ),
            new BevelRenderer( // !selected,!pressed, disabled
                Toolkit.getDefaultToolkit().createImage(ImageButtonUI.class.getResource("images/Metal/BevelLeft.1.png")),
                new Insets(8, 10, 16, 0)
            ),
            new BevelRenderer( // !selected, pressed,!disabled
                Toolkit.getDefaultToolkit().createImage(ImageButtonUI.class.getResource("images/Metal/BevelLeft.2.png")),
                new Insets(8, 10, 16, 0)
            ),
            
            null, // !selected, pressed, disabled -> illegal combination
            
            new BevelRenderer( //  selected,!pressed,!disabled
                Toolkit.getDefaultToolkit().createImage(ImageButtonUI.class.getResource("images/Metal/BevelLeft.4.png")),
                new Insets(8, 10, 16, 0)
            ),
            new BevelRenderer( // selected,!pressed, disabled
                Toolkit.getDefaultToolkit().createImage(ImageButtonUI.class.getResource("images/Metal/BevelLeft.5.png")),
                new Insets(8, 10, 16, 0)
            ),
            new BevelRenderer( // selected, pressed,!disabled
                Toolkit.getDefaultToolkit().createImage(ImageButtonUI.class.getResource("images/Metal/BevelLeft.6.png")),
                new Insets(8, 10, 16, 0)
            ),
        },
        
        // Bevel rounded at right only
        { 
            new BevelRenderer( // !selected,!pressed,!disabled
                Toolkit.getDefaultToolkit().createImage(ImageButtonUI.class.getResource("images/Metal/BevelRight.0.png")),
                new Insets(8, 1, 16, 10)
            ),
/*            new BevelRenderer( // !selected,!pressed,!disabled
                Toolkit.getDefaultToolkit().createImage(ImageButtonUI.class.getResource("images/Metal/BevelRight.test.png")),
                new Insets(8, 1, 16, 10)
            ),
/*            new BevelRenderer( // !selected,!pressed,!disabled
                Toolkit.getDefaultToolkit().createImage(ImageButtonUI.class.getResource("images/Metal/test.png")),
                new Insets(8, 8, 8, 8)
            ),
* /
            new BevelRenderer( // !selected,!pressed, disabled
                Toolkit.getDefaultToolkit().createImage(ImageButtonUI.class.getResource("images/Metal/BevelRight.1.png")),
                new Insets(8, 1, 16, 10)
            ),
            new BevelRenderer( // !selected, pressed,!disabled
                Toolkit.getDefaultToolkit().createImage(ImageButtonUI.class.getResource("images/Metal/BevelRight.2.png")),
                new Insets(8, 1, 16, 10)
            ),
            
            null, // !selected, pressed, disabled -> illegal combination
            
            new BevelRenderer( //  selected,!pressed,!disabled
                Toolkit.getDefaultToolkit().createImage(ImageButtonUI.class.getResource("images/Metal/BevelRight.4.png")),
                new Insets(8, 1, 16, 10)
            ),
            new BevelRenderer( // selected,!pressed, disabled
                Toolkit.getDefaultToolkit().createImage(ImageButtonUI.class.getResource("images/Metal/BevelRight.5.png")),
                new Insets(8, 1, 16, 10)
            ),
            new BevelRenderer( // selected, pressed,!disabled
                Toolkit.getDefaultToolkit().createImage(ImageButtonUI.class.getResource("images/Metal/BevelRight.6.png")),
                new Insets(8, 1, 16, 10)
            ),
        },
        
        // Bevel none
        { 
            new BevelRenderer( // !selected,!pressed,!disabled
                Toolkit.getDefaultToolkit().createImage(ImageButtonUI.class.getResource("images/Metal/BevelNone.0.png")),
                new Insets(8, 1, 16, 0)
            ),
            new BevelRenderer( // !selected,!pressed, disabled
                Toolkit.getDefaultToolkit().createImage(ImageButtonUI.class.getResource("images/Metal/BevelNone.1.png")),
                new Insets(8, 1, 16, 0)
            ),
            new BevelRenderer( // !selected, pressed,!disabled
                Toolkit.getDefaultToolkit().createImage(ImageButtonUI.class.getResource("images/Metal/BevelNone.2.png")),
                new Insets(8, 1, 16, 0)
            ),
            
            null, // !selected, pressed, disabled -> illegal combination
            
            new BevelRenderer( //  selected,!pressed,!disabled
                Toolkit.getDefaultToolkit().createImage(ImageButtonUI.class.getResource("images/Metal/BevelNone.4.png")),
                new Insets(8, 1, 16, 0)
            ),
            new BevelRenderer( // selected,!pressed, disabled
                Toolkit.getDefaultToolkit().createImage(ImageButtonUI.class.getResource("images/Metal/BevelNone.5.png")),
                new Insets(8, 1, 16, 0)
            ),
            new BevelRenderer( // selected, pressed,!disabled
                Toolkit.getDefaultToolkit().createImage(ImageButtonUI.class.getResource("images/Metal/BevelNone.6.png")),
                new Insets(8, 1, 16, 0)
            ),
        }
    };
   */ 
    /**
     * Convenience method for installing a component's default Border object on the 
     * specified component if either the border is currently null or already an instance 
     * of UIResource. 
     *
     *
     * @param c the target component for installing default border
     * @param defaultBorderName - the key specifying the default border     
     */
    static void installBevelBorder(JComponent c, String defaultBorderName) {
        initBevels();
        Object bevelProperty = c.getClientProperty(PROP_BEVEL);
        Border border;
        if (bevelProperty == WEST) border = new EmptyBorder(6,8,8,4);
        else if (bevelProperty == EAST) border = new EmptyBorder(6,4,8,8);
        else if (bevelProperty == NONE || bevelProperty == CENTER) border = new EmptyBorder(6,4,8,4);
        else border = new EmptyBorder(6,8,8,8);
        c.setBorder(border);
    }
    
    static void paintBevel(JComponent c, Graphics g, int x, int y, int width, int height, boolean enabled, boolean pressed, boolean selected) {
        initBevels();
        Object bevelProperty = c.getClientProperty(PROP_BEVEL);
        int type;
        if (bevelProperty == WEST) type = 1;
        else if (bevelProperty == EAST) type = 2;
        else if (bevelProperty == NONE || bevelProperty == CENTER) type = 3;
        else type = 0;
        
        int state = ((enabled) ? 0 : 1) | ((pressed & enabled) ? 2 : 0) | ((selected) ? 4 : 0);
        
        bevelRenderer[type][state].paintBevel(c, g, x, y, width, height);
    }
    
    private static void initBevels() {
        if (bevelRenderer == null) {
            String id = UIManager.getLookAndFeel().getID();
            String path;
            if (id.equals("Metal")) {
                path = "images/Metal/";
            } else {
                path = "images/Mac/";
            }

            String[] names = {"Bevel", "BevelLeft", "BevelRight", "BevelNone"};
            Insets[] insets = {new Insets(8, 10, 16, 10), new Insets(8, 10, 16, 0), new Insets(8, 1, 16, 10), new Insets(8, 1, 16, 0)};


            bevelRenderer = new BevelRenderer[4][7];
            for (int i=0; i < 4; i++) {
                for (int j=0; j < 7; j++) {
                    if (j != 3) {
                        bevelRenderer[i][j] = new BevelRenderer(
                            Toolkit.getDefaultToolkit().createImage(
                                PlafUtils.class.getResource(path+names[i]+"."+j+".png")
                            ),
                            insets[i]
                        );
                    }
                }
            }
        }
    }

    /*
     * Convenience function for determining ComponentOrientation.  Helps us
     * avoid having Munge directives throughout the code.
     */
    static boolean isLeftToRight( Component c ) {
        /*if[JDK1.2]
        return c.getComponentOrientation().isLeftToRight();
        else[JDK1.2]*/
        return true;
        /*end[JDK1.2]*/
    }
    
}

