/*
 * @(#)Random2.java  1.0  July 11, 2005
 *
 * Copyright (c) 2005 Werner Randelshofer
 * Staldenmattweg 2, Immensee, CH-6405, Switzerland.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Werner Randelshofer. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Werner Randelshofer.
 */

package ch.randelshofer.util;

import java.util.*;
/**
 * Random2.
 *
 * @author  Werner Randelshofer
 * @version 1.0 July 11, 2005 Created.
 */
public class Random2 extends Random {
    
    /**
     * Creates a new instance.
     */
    public Random2() {
        super();
    }
    public Random2(long seed) {
        super(seed);
    }
    public int nextInt(int n) {
        if (n<=0)
            throw new IllegalArgumentException("n must be positive");
        
        if ((n & -n) == n)  // i.e., n is a power of 2
            return (int)((n * (long)next(31)) >> 31);
        
        int bits, val;
        do {
            bits = next(31);
            val = bits % n;
        } while(bits - val + (n-1) < 0);
        return val;
    }
}
