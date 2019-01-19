/* @(#)Icons.java
 *
 * Copyright (c) 2001 Werner Randelshofer
 * Staldenmattweg 2, Immensee, CH-6405, Switzerland.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Werner Randelshofer. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Werner Randelshofer.
 */

package ch.randelshofer.gui;

import ch.randelshofer.gui.*;

import java.awt.*;
import java.awt.geom.*;

import javax.swing.*;

/**
 * Provides constants for commonly used Icons.
 *
 * @author Werner Randelshofer
 * @version 1.0.2 2003-08-24 Disabled popup button icon acced.
 * <br>1.0.1 2002-11-17 Appeareance of POPUP_ICON changed.
 * <br>1.0 2002-05-09 Created.
 */
public class Icons {
    /**
     * The reset icon consists of a rectangle (at west)
     * and a triangle pointing to the rectangle (towards west).
     */
    public final static Icon RESET_ICON = new VectorIcon(
    new Shape[] {
        //new Rectangle(2, 1, 1, 8),
        new Polygon(
        new int[] { 2, 3, 3, 2 },
        new int[] { 2, 2, 9, 9 },
        4
        ),
        new Polygon(
        new int[] {  9, 9, 6, 6 },
        new int[] {  2, 9, 6, 5 },
        4
        )
    },
    10, 10, Color.black, Color.black
    );
    
    /**
     * The record icon consists of a filled circle.
     */
    public final static Icon RECORD_ICON = new VectorIcon(
    new Ellipse2D.Float(2f, 2f, 7f, 7f),
    10, 10, Color.black, Color.black
    );
    
    /**
     * The pressed record consists of a filled circle.
     */
    public final static Icon RECORD_ICON_PRESSED = new VectorIcon(
    new Ellipse2D.Float(2f, 2f, 7f, 7f),
    10, 10, Color.black, Color.black
    );
    /**
     * The check icon consists of a checkmark.
     */
    public final static Icon CHECK_ICON = new VectorIcon(
    new Polygon(
    new int[] { 0, 1, 3, 8, 9, 4, 3 },
    new int[] { 5, 5, 8, 0, 0, 9, 9 },
    7
    ),
    10, 10, Color.black, Color.black
    );
    
    /**
     * The small popup icon consists of a small triangle pointing
     * to south.
     */
    public final static Icon SMALL_POPUP_ICON = new VectorIcon(
    new Shape[] {
        new Polygon(
        new int[] {0, 7, 3},
        new int[] {4, 4, 8},
        3
        )
    },
    6, 10, Color.black, null
    );
    
    /**
     * The popup icon consists of four stacked rectangles.
     */
    public final static Icon POPUP_ICON = new ImageIcon(
    Icons.class.getResource("images/Popup.icon.gif")
    );
    /**
     * The popup icon consists of four stacked rectangles.
     */
    public final static Icon POPUP_DISABLED_ICON = new ImageIcon(
    Icons.class.getResource("images/Popup.disabled.icon.gif")
    );
    /*
    public final static Icon POPUP_ICON = new VectorIcon(
    new Shape[] {
        new Rectangle(2, 1, 6, 1),
        new Rectangle(2, 2, 6, 2),
        new Rectangle(2, 4, 6, 2),
        new Rectangle(2, 6, 6, 2)
    },
    10, 10, null, Color.black
    );*/
    /**
     * The start icon consists of a triangle pointing
     * to east.
     */
    public final static Icon START_ICON = new VectorIcon(
    new Polygon(
    new int[] { 4, 7, 7, 4 },
    new int[] { 2, 5, 6, 9 },
    4
    ),
    10, 10, Color.black, Color.black
    );
    /**
     * The pause icon consists of two rectangles.
     */
    public final static Icon PAUSE_ICON = new VectorIcon(
                new Polygon[] {
                    new Polygon(
                        new int[] { 3, 4, 4, 3 },
                        new int[] { 2, 2, 9, 9 },
                        4
                    ),
                    new Polygon(
                        new int[] { 7, 8, 8, 7 },
                        new int[] { 2, 2, 9, 9 },
                        4
                    )
                },
    10, 10, Color.black, Color.black
            );
    /**
     * The back icon consists of rectangle (at east)
     * and of a triangle pointing to west.
     */
    public final static Icon BACK_ICON = new VectorIcon(
                new Polygon[] {
                    new Polygon(
                        new int[] {  4, 4, 1, 1 },
                        new int[] {  2, 9, 6, 5 },
                        4
                    ),
                    new Polygon(
                        new int[] { 7, 8, 8, 7 },
                        new int[] { 2, 2, 9, 9 },
                        4
                    )
                },
    10, 10, Color.black, Color.black
            );
                
    /**
     * The forward icon consists of rectangle (at east)
     * and of a triangle pointing to west.
     */
    public final static Icon FORWARD_ICON = new VectorIcon(
                new Polygon[] {
                    new Polygon(
                        new int[] { 2, 3, 3, 2 },
                        new int[] { 2, 2, 9, 9 },
                        4
                    ),
                    new Polygon(
                        new int[] { 6, 9, 9, 6 },
                        new int[] { 2, 5, 6, 9 },
                        4
                    )
                },
    10, 10, Color.black, Color.black
            );
    
    /** Private to prevent creation of instances. */
    private Icons() {
    }
    
}
