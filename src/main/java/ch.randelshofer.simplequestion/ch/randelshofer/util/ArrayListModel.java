/*
 * ArrayListModel.java
 *
 * Created on July 1, 2003, 9:00 PM
 */

package ch.randelshofer.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A ListModel backed by an ArrayList.
 *
 * @author werni
 */
public class ArrayListModel<E> extends javax.swing.AbstractListModel<E>
        implements List<E>/*, RandomAccess*/ {
    public final static long serialVersionUID = 1L;
    private ArrayList<E> delegate;

    /**
     * Creates a new instance of ArrayListModel
     */
    public ArrayListModel() {
        delegate = new ArrayList<>();
    }

    public E getElementAt(int index) {
        return delegate.get(index);
    }

    public int getSize() {
        return delegate.size();
    }

    public boolean add(E o) {
        int index = delegate.size();
        delegate.add(o);
        fireIntervalAdded(this, index, index);
        return true;
    }

    public void add(int index, E element) {
        delegate.add(index, element);
        fireIntervalAdded(this, index, index);
    }

    public boolean addAll(Collection<? extends E> c) {
        if (c.size() > 0) {
            int index = delegate.size();
            delegate.addAll(c);
            fireIntervalAdded(this, index, index + c.size() - 1);

            return true;
        } else {
            return false;
        }

    }

    public boolean addAll(E[] c) {
        return addAll(Arrays.asList(c));
    }

    public boolean addAll(int index, Collection<? extends E> c) {
        delegate.addAll(index, c);
        fireIntervalAdded(this, index, index + c.size() - 1);
        return true;
    }

    public void clear() {
        int index1 = delegate.size() - 1;
        delegate.clear();
        if (index1 >= 0) {
            fireIntervalRemoved(this, 0, index1);
        }
    }

    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    public boolean containsAll(Collection<?> c) {
        return delegate.containsAll(c);
    }

    public E get(int index) {
        return delegate.get(index);
    }

    public int indexOf(Object o) {
        return delegate.indexOf(o);
    }

    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    public Iterator<E> iterator() {
        return delegate.iterator();
    }

    public int lastIndexOf(Object o) {
        return delegate.lastIndexOf(o);
    }

    public ListIterator<E> listIterator() {
        return delegate.listIterator();
    }

    public ListIterator<E> listIterator(int index) {
        return delegate.listIterator(index);
    }

    public boolean remove(Object o) {
        int index = delegate.indexOf(o);
        if (index != -1) {
            delegate.remove(index);
            fireIntervalRemoved(this, index, index);
            return true;
        }
        return false;
    }

    public E remove(int index) {
        E removed = delegate.remove(index);
        fireIntervalRemoved(this, index, index);
        return removed;
    }

    public boolean removeAll(Collection<?> c) {
        boolean hasRemoved = false;
        for (Object o : c) {
            hasRemoved = remove(o) || hasRemoved;
        }
        return hasRemoved;
    }

    public boolean retainAll(Collection<?> c) {
        boolean hasChanged;
        ArrayList<E> temp = delegate;
        delegate = new ArrayList<>();
        fireIntervalRemoved(this, 0, temp.size() - 1);
        hasChanged = temp.retainAll(c);
        delegate = temp;
        fireIntervalAdded(this, 0, delegate.size() - 1);
        return hasChanged;
    }

    public E set(int index, E element) {
        E result = delegate.set(index, element);
        fireContentsChanged(this, index, index);
        return result;
    }

    public int size() {
        return delegate.size();
    }

    public List<E> subList(int fromIndex, int toIndex) {
        return delegate.subList(fromIndex, toIndex);
    }

    public Object[] toArray() {
        return delegate.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return delegate.toArray(a);
    }

    /**
     * Returns a string that displays and identifies this
     * object's properties.
     *
     * @return a String representation of this object
     */
    public String toString() {
        return delegate.toString();
    }
}
