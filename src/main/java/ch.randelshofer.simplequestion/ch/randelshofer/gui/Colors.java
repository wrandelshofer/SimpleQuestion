/* @(#)Colors.java
 * Copyright © Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.gui;

import java.awt.Color;
/**
 * Colors.
 *
 * @author  Werner Randelshofer
 * @version 1.0 8. April 2004  Created.
 */
public class Colors {
    
    /** Prevent instance creation. */
    private Colors() {
    }

    /**
     * Blackens the specified color by casting a black shadow of the specified 
     * amount on the color.
     */
    public static Color shadow(Color c, int amount) {
       return (c == null) ? null :
           new Color(
        Math.max(0, c.getRed() - amount),
        Math.max(0, c.getGreen() - amount),
        Math.max(0, c.getBlue() - amount),
        c.getAlpha()
        );
    }
    
}
