/*
 * @(#)ReversedList.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
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
