/*
 * @(#)BevelRenderer.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.gui.plaf;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

/**
 * Takes an image and insets. The image must show a
 * bevel and a fill area.
 * The insets and the size of the image are
 * used do determine which parts of the image shall be
 * used to draw the corners and edges of the bevel as
 * well the fill area.
 *
 * <p>For example, if you provide an image of size 10,10
 * and a insets of size 2, 2, 4, 4, then the corners of
 * the border are made up of top left: 2,2, top right: 2,4,
 * bottom left: 2,4, bottom right: 4,4 rectangle of the image.
 * The inner area of the image is used to fill the inner area.
 *
 * @author Werner Randelshofer
 */
public class BevelRenderer {
    private final static boolean VERBOSE = false;
    /**
     * The image to be used for drawing.
     */
    private Image image;

    /**
     * The insets of the image.
     */
    private Insets imageInsets;

    /**
     * Cached image dimensions.
     */
    private int imageHeight, imageWidth;

    /**
     * Creates a new BevelRenderer without an image and without zero insets.
     */
    public BevelRenderer() {
        imageInsets = new Insets(0, 0, 0, 0);
    }

    /**
     * Creates a new BevelRenderer without the given image and insets.
     */
    public BevelRenderer(Image img, Insets insets) {
        setImage(img);
        imageInsets = insets;
    }

    /**
     * Sets the image to be used for border painting.
     */
    public void setImage(Image img) {
        image = img;

        // Load the image
        if (image != null) {
            Frame f = new Frame();
            f.pack();
            MediaTracker t = new MediaTracker(f);
            t.addImage(img, 0);
            try {
                t.waitForAll();
            } catch (InterruptedException e) {
            }
            imageWidth = image.getWidth(f);
            imageHeight = image.getHeight(f);
            f.dispose();
        }

    }

    /**
     * Gets the image to be used for border painting.
     */
    public Image getImage() {
        return image;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public int getImageWidth() {
        return imageWidth;
    }


    /**
     * Sets the insets of the image.
     */
    public void setImageInsets(Insets insets) {
        this.imageInsets = insets;
    }

    /**
     * Gets the insets of the image.
     */
    public Insets getImageInsets() {
        return (Insets) imageInsets.clone();
    }

    /**
     * Paints the bevel image for the specified component with the
     * specified position and size.
     *
     * @param c      the component for which this border is being painted
     * @param g      the paint graphics
     * @param x      the x position of the painted border
     * @param y      the y position of the painted border
     * @param width  the width of the painted border
     * @param height the height of the painted border
     */
    public void paintBevel(Component c, Graphics gr, int x, int y, int width, int height) {
        try {
            if (image == null) {
                return;
            }

            // Cast Graphics to Graphics2D
            Graphics2D g = (Graphics2D) gr;
            /*
            g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
            g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
            g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

g.setColor(Color.red);
g.fillRect(x, y, width, height);
             */
            // Set some variables for easy access of insets and image size
            int top = imageInsets.top;
            int left = imageInsets.left;
            int bottom = imageInsets.bottom;
            int right = imageInsets.right;
            int imgWidth = imageWidth;//image.getWidth(c);
            int imgHeight = imageHeight;//image.getHeight(c);

            if (imgWidth == -1 || imgHeight == -1) {
                return;
            }

            // Optimisation: Draw image directly if it fits into the component
            if (width == imgWidth && height == imgHeight) {
                g.drawImage(image, x, y, c);
                return;
            }

            // Optimisation: Remove insets, if image width or image height fits
            if (width == imgWidth) {
                left = imgWidth;
                right = 0;
            }
            if (height == imgHeight) {
                top = imgHeight;
                bottom = 0;
            }

            // Adjust insets if component is too small
            if (width < left + right) {
                left = Math.min(left, width / 2); //Math.max(0, left + (width - left - right) / 2);
                right = width - left;
            }
            if (height < top + bottom) {
                top = Math.min(top, height / 2); //Math.max(0, top + (height - top - bottom) / 2);
                bottom = height - top;
            }

            // We need a buffered image for tyling
            BufferedImage buffImg;
            if (image instanceof BufferedImage) {
                buffImg = (BufferedImage) image;
            } else {
                //buffImg = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);
                buffImg = c.getGraphicsConfiguration().createCompatibleImage(imgWidth, imgHeight, Transparency.TRANSLUCENT);
                Graphics bg = buffImg.getGraphics();
                bg.drawImage(image, 0, 0, c);
                bg.dispose();
                image = buffImg;
            }


            // Draw the Corners
            if (top > 0 && left > 0) {
                g.drawImage(
                        buffImg,
                        x, y, x + left, y + top,
                        0, 0, left, top,
                        c
                );
                //    g.drawRect(x, y, x+left, y+top);
            }
            if (top > 0 && right > 0) {
                g.drawImage(
                        buffImg,
                        x + width - right, y, x + width, y + top,
                        imgWidth - right, 0, imgWidth, top,
                        c
                );
                //g.drawRect(x+width-right, y, x+width, y+top);
            }
            if (bottom > 0 && left > 0) {
                g.drawImage(
                        buffImg,
                        x, y + height - bottom, x + left, y + height,
                        0, imgHeight - bottom, left, imgHeight,
                        c
                );
                //g.drawRect(x, y+height-bottom, x+left, y+height);
            }
            if (bottom > 0 && right > 0) {
                g.drawImage(
                        buffImg,
                        x + width - right, y + height - bottom, x + width, y + height,
                        imgWidth - right, imgHeight - bottom, imgWidth, imgHeight,
                        c
                );
                //g.drawRect(x+width-right, y+height-bottom, x+width, y+height);
            }

            // Draw the edges
            BufferedImage subImg = null;
            TexturePaint paint;
            if (VERBOSE) {
                System.out.println("insets:" + imageInsets);
            }
            if (VERBOSE) {
                System.out.println("img dim w:" + imgWidth + " h:" + imgHeight);
            }
            if (VERBOSE) {
                System.out.println("cmp dim w:" + width + " h:" + height);
            }

            if (top > 0 && left + right < width) {
                if (VERBOSE) {
                    System.out.println("  top:" + c.getClass() + " x" + left + " y" + 0 + " w" + (imgWidth - right - left) + " h" + top);
                }
                subImg = buffImg.getSubimage(left, 0, imgWidth - right - left, top);
                paint = new TexturePaint(subImg, new Rectangle(x + left, y, imgWidth - left - right, top));
                g.setPaint(paint);
                g.fillRect(x + left, y, width - left - right, top);
            }
            if (bottom > 0 && left + right < width) {
                if (VERBOSE) {
                    System.out.println("  bottom:" + c.getClass() + " x" + left + " y" + (imgHeight - bottom) + " w" + (imgWidth - right - left) + " h" + bottom);
                }
                subImg = buffImg.getSubimage(left, imgHeight - bottom, imgWidth - right - left, bottom);
                paint = new TexturePaint(subImg, new Rectangle(x + left, y + height - bottom, imgWidth - left - right, bottom));
                g.setPaint(paint);
                g.fillRect(x + left, y + height - bottom, width - left - right, bottom);
            }
            if (left > 0 && top + bottom < height) {
                if (VERBOSE) {
                    System.out.println("  left:" + c.getClass() + " x" + 0 + " y" + top + " w" + (left) + " h" + (imgHeight - top - bottom));
                }
                subImg = buffImg.getSubimage(0, top, left, imgHeight - top - bottom);
                paint = new TexturePaint(subImg, new Rectangle(x, y + top, left, imgHeight - top - bottom));
                g.setPaint(paint);
                g.fillRect(x, y + top, left, height - top - bottom);
            }
            if (right > 0 && top + bottom < height) {
                if (VERBOSE) {
                    System.out.println("  right:" + c.getClass() + " x" + (imgWidth - right) + " y" + top + " w" + (right) + " h" + (imgHeight - top - bottom));
                }
                subImg = buffImg.getSubimage(imgWidth - right, top, right, imgHeight - top - bottom);
                paint = new TexturePaint(subImg, new Rectangle(x + width - right, y + top, right, imgHeight - top - bottom));
                g.setPaint(paint);
                g.fillRect(x + width - right, y + top, right, height - top - bottom);
            }

            // Fill the center
            if (left + right < width
                    && top + bottom < height) {
                if (VERBOSE) {
                    System.out.println("  center:" + c.getClass() + " x" + (left) + " y" + top + " w" + (imgWidth - right - left) + " h" + (imgHeight - top - bottom));
                }

                subImg = buffImg.getSubimage(left, top, imgWidth - right - left, imgHeight - top - bottom);
                paint = new TexturePaint(subImg, new Rectangle(x + left, y + top, imgWidth - right - left, imgHeight - top - bottom));
                g.setPaint(paint);
                g.fillRect(x + left, y + top, width - right - left, height - top - bottom);
            }
        } catch (Throwable e) {
            System.out.println(e);
        }
    }
}
