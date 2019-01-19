/* @(#)StudentModel.java
 *
 * Copyright (c) 2003 Werner Randelshofer
 * Staldenmattweg 2, Immensee, CH-6405, Switzerland.
 * http://www.randelshofer.ch
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Werner Randelshofer. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Werner Randelshofer.
 */

package ch.randelshofer.scorm;

import ch.randelshofer.beans.AbstractBean;
import ch.randelshofer.util.ArrayUtil;

import java.beans.PropertyChangeSupport;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * StudentModel.
 *
 * @author Werner Randelshofer
 * @version 1.0 August 24, 2003  Created.
 */
public class StudentModel extends AbstractBean implements Cloneable {
    public final static long serialVersionUID = 1L;
    private String id, password, lastName, firstName, middleInitial;
    /**
     * The electronic fingerprint of the password.
     * 32 bit hex encoded salt concatenated with the password
     * and munched with SHA1.
     */
    private String passwordDigest;
    private static SecureRandom random;

    /**
     * Creates a new instance.
     */
    public StudentModel() {
    }

    public StudentModel(String id, String lastName, String firstName, String middleInitial) {
        this.id = id;
        this.password = password;
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleInitial = middleInitial;
    }

    public void setID(String newValue) {
        String oldValue = id;
        id = newValue;
        propertySupport.firePropertyChange("id", oldValue, newValue);
    }

    public void setPassword(String newValue) {
        String oldValue = password;
        password = newValue;
        propertySupport.firePropertyChange("password", oldValue, newValue);
        passwordDigest = null;
    }

    public void setLastName(String newValue) {
        String oldValue = lastName;
        lastName = newValue;
        propertySupport.firePropertyChange("lastName", oldValue, newValue);
    }

    public void setFirstName(String newValue) {
        String oldValue = firstName;
        firstName = newValue;
        propertySupport.firePropertyChange("firstName", oldValue, newValue);
    }

    public void setMiddleInitial(String newValue) {
        String oldValue = middleInitial;
        middleInitial = newValue;
        propertySupport.firePropertyChange("middleInitial", oldValue, newValue);
    }

    public String getID() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getMiddleInitial() {
        return middleInitial;
    }

    public String getPassword() {
        return password;
    }

    public void setPasswordDigest(String newValue) {
        String oldValue = passwordDigest;
        passwordDigest = newValue;
        // For security reasons we erase the password if we have a digest.
        if (newValue != null) {
            password = "******";
        }
    }

    public String getPasswordDigest() {
        try {
            if (passwordDigest == null && password != null && password.trim().length() != 0) {
                // Lazily create the random generator
                if (random == null) {
                    random = new SecureRandom();
                }

                // Create 32 bits of salt
                String salt = Integer.toString(random.nextInt(), 16);

                // Compute the message digest for the password
                // We use UTF-8 encoding because this encoding is supported
                // by the String.charCodeAt(index) method of JavaScript.
                MessageDigest cript = MessageDigest.getInstance("SHA-1");
                cript.reset();
                cript.update(salt.getBytes(StandardCharsets.UTF_8));
                cript.update(password.getBytes(StandardCharsets.UTF_8));
                passwordDigest = salt + "." + ArrayUtil.toHexString(cript.digest());
                //password = null;
            }
            return passwordDigest;

        } catch (NoSuchAlgorithmException e) {
            InternalError t = new InternalError(e.toString());
            t.initCause(e);
            throw t;
        }
    }

    public Object clone() {
        try {
            StudentModel that = (StudentModel) super.clone();
            that.propertySupport = new PropertyChangeSupport(that);
            return that;
        } catch (CloneNotSupportedException e) {
            InternalError t = new InternalError(e.toString());
            t.initCause(e);
            throw t;
        }
    }
}
