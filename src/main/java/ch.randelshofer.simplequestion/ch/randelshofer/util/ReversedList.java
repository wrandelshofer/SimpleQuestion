/* @(#)ReversedList.java
 *
 * Copyright (c) 2006 Werner Randelshofer
 * Staldenmattweg 2, CH-6405 Immensee, Switzerland
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Werner Randelshofer ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Werner Randelshofer
 */

package ch.randelshofer.util;

import java.util.AbstractList;
import java.util.List;

/**
 * A ReversedList provides in unmodifiable view on a List in reverse order.
 *
 * @author wrandels
 */
public class ReversedList<T> extends AbstractList<T> {
    private List<T> target;

    /**
     * Creates a new instance of ReversedList
     */
    public ReversedList(List<T> target) {
        this.target = target;
    }

    public T get(int index) {
        return target.get(target.size() - 1 - index);
    }

    public int size() {
        return target.size();
    }

}
