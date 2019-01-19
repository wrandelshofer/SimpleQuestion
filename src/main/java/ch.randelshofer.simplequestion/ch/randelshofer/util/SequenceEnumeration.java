/* @(#)SequenceEnumeration.java * * Copyright (c) 1999 Werner Randelshofer * Staldenmattweg 2, Immensee, CH-6405, Switzerland * All rights reserved. * * This software is the confidential and proprietary information of * Werner Randelshofer. ("Confidential Information").  You shall not * disclose such Confidential Information and shall use it only in * accordance with the terms of the license agreement you entered into * with Werner Randelshofer. */package ch.randelshofer.util;import java.util.Enumeration;import java.util.NoSuchElementException;/** * This class Encapsulates two Enumerations. * * @author Werner Randelshofer, Staldenmattweg 2, Immensee, CH-6405, Switzerland * @version 1.0 2001-07-26 */public class SequenceEnumeration        implements Enumeration {    /**     * The first enumeration.     */    private Enumeration first;    /**     * The second enumeration.     */    private Enumeration second;    /**     * @param first  The first enumeration.     * @param second The second enumeration.     */    public SequenceEnumeration(Enumeration first, Enumeration second) {        this.first = first;        this.second = second;    }    /**     * Tests if this enumeration contains next element.     *     * @return <code>true</code> if this enumeration contains it     * <code>false</code> otherwise.     */    public boolean hasMoreElements() {        return first.hasMoreElements() || second.hasMoreElements();    }    /**     * Returns the next element of this enumeration.     *     * @return the next element of this enumeration.     * @throws NoSuchElementException if no more elements exist.     */    public synchronized Object nextElement() {        if (first.hasMoreElements()) {            return first.nextElement();        } else {            return second.nextElement();        }    }}