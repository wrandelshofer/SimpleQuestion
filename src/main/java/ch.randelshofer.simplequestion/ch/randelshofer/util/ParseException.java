/* @(#)ParseException.java * * Copyright (c) 1999 Werner Randelshofer * Staldenmattweg 2, CH-6405 Immensee, Switzerland * All rights reserved. * * This software is the confidential and proprietary information of * Werner Randelshofer. ("Confidential Information").  You shall not * disclose such Confidential Information and shall use it only in * accordance with the terms of the license agreement you entered into * with Werner Randelshofer. */package ch.randelshofer.util;/** * Exception thrown by IFFParse. * * @author Werner Randelshofer, Staldenmattweg 2, CH-6405 Immensee, Switzerland * @version 1.0  1999-10-19 */public class ParseException        extends Exception {    public ParseException(String message) {        super(message);    }}