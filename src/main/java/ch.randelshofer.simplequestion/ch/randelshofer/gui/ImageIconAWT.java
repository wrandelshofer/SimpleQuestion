/*
 * @(#)ImageIconAWT.java	1.49 01/12/03
 *
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package ch.randelshofer.gui;

import java.awt.*;
import java.awt.image.*;
import java.net.URL;

import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

import java.util.Locale;
import javax.accessibility.*;


/**
 * An implementation of the Icon interface that paints Icons
 * from Images. Images that are created from a URL or filename
 * are preloaded using MediaTracker to monitor the loaded state
 * of the image.
 *
 * <p>
 * For further information and examples of using image icons, see
 * <a href="http://java.sun.com/docs/books/tutorial/uiswing/misc/icon.html">How to Use Icons</a>
 * in <em>The Java Tutorial.</em>
 *
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with
 * future Swing releases. The current serialization support is
 * appropriate for short term storage or RMI between applications running
 * the same version of Swing.  As of 1.4, support for long term storage
 * of all JavaBeans<sup><font size="-2">TM</font></sup>
 * has been added to the <code>java.beans</code> package.
 * Please see {@link java.beans.XMLEncoder}.
 * 
 * @version 1.49 12/03/01
 * @author Jeff Dinkins
 * @author Lynn Monsanto
 */
public class ImageIconAWT implements IconAWT {
    transient Image image;
    transient int loadStatus = 0;
    ImageObserver imageObserver;

    protected final static Component component = new Component() {};
    protected final static MediaTracker tracker = new MediaTracker(component);

    /**
     * Id used in loading images from MediaTracker.
     */
    private static int mediaTrackerID;

    int width = -1;
    int height = -1;

    /**
     * Creates an ImageIcon from the specified file. The image will
     * be preloaded by using MediaTracker to monitor the loading state
     * of the image.
     * @param filename the name of the file containing the image
     * @see #ImageIcon(String)
     */
    public ImageIconAWT(String filename) {
	image = Toolkit.getDefaultToolkit().getImage(filename);
        if (image == null) {
            return;
        }
	loadImage(image);
    }


    /**
     * Creates an ImageIcon from the specified URL. The image will
     * be preloaded by using MediaTracker to monitor the loaded state
     * of the image.
     * @param location the URL for the image
     * @param description a brief textual description of the image
     * @see #ImageIcon(String)
     */
    public ImageIconAWT(URL location) {
	image = Toolkit.getDefaultToolkit().getImage(location);
        if (image == null) {
            return;
        } 
	loadImage(image);
    }


    /**
     * Creates an ImageIcon from an image object. 
     * If the image has a "comment" property that is a string,
     * then the string is used as the description of this icon.
     * @param image the image
     * @see #getDescription
     * @see java.awt.Image#getProperty
     */
    public ImageIconAWT(Image image) {
	this.image = image;
	loadImage(image);
    }

    /**
     * Creates an ImageIcon from an array of bytes which were
     * read from an image file containing a supported image format,
     * such as GIF or JPEG.  Normally this array is created
     * by reading an image using Class.getResourceAsStream(), but
     * the byte array may also be statically stored in a class.
     *
     * @param  imageData an array of pixels in an image format supported
     *         by the AWT Toolkit, such as GIF or JPEG.
     * @see    java.awt.Toolkit#createImage
     */
    public ImageIconAWT(byte[] imageData) {
	this.image = Toolkit.getDefaultToolkit().createImage(imageData);
        if (image == null) {
            return;
        }
	loadImage(image);
    }

    /**
     * Creates an uninitialized image icon.
     */
    public ImageIconAWT() {
    }

    /**
     * Loads the image, returning only when the image is loaded.
     * @param image the image
     */
    protected void loadImage(Image image) {
	synchronized(tracker) {
            int id = getNextID();

	    tracker.addImage(image, id);
	    try {
		tracker.waitForID(id, 0);
	    } catch (InterruptedException e) {
		System.out.println("INTERRUPTED while loading Image");
	    }
            loadStatus = tracker.statusID(id, false);
	    tracker.removeImage(image, id);

	    width = image.getWidth(imageObserver);
	    height = image.getHeight(imageObserver);
	}
    }

    /**
     * Returns an ID to use with the MediaTracker in loading an image.
     */
    private int getNextID() {
        synchronized(tracker) {
            return ++mediaTrackerID;
        }
    }

    /**
     * Returns the status of the image loading operation.
     * @return the loading status as defined by java.awt.MediaTracker
     * @see java.awt.MediaTracker#ABORTED
     * @see java.awt.MediaTracker#ERRORED
     * @see java.awt.MediaTracker#COMPLETE
     */
    public int getImageLoadStatus() {
        return loadStatus;
    }

    /**
     * Returns this icon's <code>Image</code>.
     * @return the <code>Image</code> object for this <code>ImageIcon</code>
     */
    public Image getImage() {
	return image;
    }

    /**
     * Sets the image displayed by this icon.
     * @param image the image
     */
    public void setImage(Image image) {
	this.image = image;
	loadImage(image);
    }

    /**
     * Paints the icon.
     * The top-left corner of the icon is drawn at 
     * the point (<code>x</code>, <code>y</code>)
     * in the coordinate space of the graphics context <code>g</code>.
     * If this icon has no image observer,
     * this method uses the <code>c</code> component
     * as the observer.
     *
     * @param c the component to be used as the observer
     *          if this icon has no image observer
     * @param g the graphics context 
     * @param x the X coordinate of the icon's top-left corner
     * @param y the Y coordinate of the icon's top-left corner
     */
    public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
        if(imageObserver == null) {
           g.drawImage(image, x, y, c);
        } else {
	   g.drawImage(image, x, y, imageObserver);
        }
    }

    /**
     * Gets the width of the icon.
     *
     * @return the width in pixels of this icon
     */
    public int getIconWidth() {
	return width;
    }

    /**
     * Gets the height of the icon.
     *
     * @return the height in pixels of this icon
     */
    public int getIconHeight() {
	return height;
    }

    /** 
     * Sets the image observer for the image.  Set this
     * property if the ImageIcon contains an animated GIF, so
     * the observer is notified to update its display.
     * For example:
     * <pre>
     *     icon = new ImageIcon(...)
     *     button.setIcon(icon);
     *     icon.setImageObserver(button);
     * </pre>
     *
     * @param observer the image observer
     */
    public void setImageObserver(ImageObserver observer) {
        imageObserver = observer;
    }

    /**
     * Returns the image observer for the image.
     *
     * @return the image observer, which may be null
     */
    public ImageObserver getImageObserver() {
        return imageObserver;
    }

}

