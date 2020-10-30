/*
 * @(#)IdentifierGenerator.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.util;

import java.util.HashMap;

/**
 * @author werni
 * @version 1.0 2003-03-05 Revised.
 * <br>0.1 2003-02-09 Created.
 */
public class IdentifierGenerator {
    private HashMap<String, String> keyValueMap = new HashMap<>();
    private HashMap<String, String> valueKeyMap = new HashMap<>();
    int generator = 0;

    /**
     * Creates a new instance of IdentifierGenerator
     */
    public IdentifierGenerator() {
    }

    public String getIdentifier(String key) {
        if (key == null) {
            return null;
        }
        if (keyValueMap.containsKey(key)) {
            return keyValueMap.get(key);
        } else {
            int count = 0;
            String value;
            do {
                value = Integer.toString(generator++, Character.MAX_RADIX);
            } while (valueKeyMap.containsKey(value) && count++ < 100);
            if (valueKeyMap.containsKey(value)) {
                throw new InternalError();
            }
            keyValueMap.put(key, value);
            valueKeyMap.put(value, key);
            return value;
        }
    }

    public String getIdentifier(String key, String preferredIdentifier) {
        if (key == null) {
            return null;
        }
        if (keyValueMap.containsKey(key)) {
            return keyValueMap.get(key);
        } else {
            int count = 0;
            String value = preferredIdentifier;
            while (valueKeyMap.containsKey(value) && count < 100) {
                value = Integer.toString(generator++, Character.MAX_RADIX);
            }
            if (valueKeyMap.containsKey(value)) {
                throw new InternalError();
            }
            keyValueMap.put(key, value);
            valueKeyMap.put(value, key);
            return value;
        }
    }
}
