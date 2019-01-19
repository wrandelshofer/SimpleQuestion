/* @(#)MultiShow.java * * Copyright (c) 1999 Werner Randelshofer * Staldenmattweg 2, CH-6405 Immensee, Switzerland * All rights reserved. * * This software is the confidential and proprietary information of  * Werner Randelshofer. ("Confidential Information").  You shall not * disclose such Confidential Information and shall use it only in * accordance with the terms of the license agreement you entered into * with Werner Randelshofer. */package ch.randelshofer.gui.image;import java.awt.image.DirectColorModel;/**ColorModel for HAM compressed images.@author  Werner Randelshofer, Staldenmattweg 2, CH-6405 Immensee, Switzerland@version  1.0  1999-10-19*/public class HAMColorModel extends java.awt.image.DirectColorModel{  //insert class definition here  public final static int    HAM6 = 6,    HAM8 = 8;  protected int HAMType;  protected int map_size;  protected boolean opaque;  protected int[] rgb;    public HAMColorModel(int aHAMType,int size,byte r[],byte g[],byte b[]) {    super(24,0x00ff0000,0x0000ff00,0x000000ff);    if (aHAMType != HAM6 && aHAMType != HAM8) {      throw new IllegalArgumentException("Unknown HAM Type: " + aHAMType);    }    HAMType = aHAMType;    setRGBs(size,r,g,b,null);  }  public int getHAMType() {    return HAMType;  }    protected void setRGBs(int size, byte r[], byte g[], byte b[], byte a[]) {  if (size > 256) {      throw new ArrayIndexOutOfBoundsException();  }  map_size = size;  rgb = new int[256];  int alpha = 0xff;  opaque = true;  for (int i = 0; i < size; i++) {      if (a != null) {    alpha = (a[i] & 0xff);    if (alpha != 0xff) {        opaque = false;    }      }      rgb[i] = (alpha << 24)    | ((r[i] & 0xff) << 16)    | ((g[i] & 0xff) << 8)    | (b[i] & 0xff);  }    }    /**     * Copies the array of red color components into the given array.  Only     * the initial entries of the array as specified by getMapSize() are     * written.     */    final public void getReds(byte r[]) {  for (int i = 0; i < map_size; i++) {      r[i] = (byte) (rgb[i] >> 16);  }    }    /**     * Copies the array of green color components into the given array.  Only     * the initial entries of the array as specified by getMapSize() are     *  written.     */    final public void getGreens(byte g[]) {  for (int i = 0; i < map_size; i++) {      g[i] = (byte) (rgb[i] >> 8);  }    }    /**     * Copies the array of blue color components into the given array.  Only     * the initial entries of the array as specified by getMapSize() will     * be written.     */    final public void getBlues(byte b[]) {  for (int i = 0; i < map_size; i++) {      b[i] = (byte) rgb[i];  }    }    /**     * Copies the array of color components into the given array.  Only     * the initial entries of the array as specified by getMapSize() will     * be written.     */    final public void getRGBs(int rgbs[]) {  for (int i = 0; i < map_size; i++) {      rgbs[i] = rgb[i];  }    }    /**     * Returns the size of the color component arrays in this IndexColorModel.     */    final public int getMapSize() {  return map_size;    }}