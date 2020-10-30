/*
 * @(#)LocaleUtil.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.util;

import java.util.Locale;

/**
 * LocaleUtil provides a setDefault()/getDefault() wrapper to java.util.Locale
 * in order to overcome the security restriction preventing Applets from using
 * their own locale.
 *
 * @author Werner Randelshofer
 * @version 1.0 22. Mai 2006 Created.
 */
public class LocaleUtil {
    private static Locale defaultLocale;

    /**
     * Creates a new instance.
     */
    public LocaleUtil() {
    }

    public static void setDefault(Locale newValue) {
        defaultLocale = newValue;
    }

    public static Locale getDefault() {
        return (defaultLocale == null) ? Locale.getDefault() : defaultLocale;
    }
}
