/*
 * ArrayListModel.java
 *
 * Created on July 1, 2003, 9:00 PM
 */

package ch.randelshofer.util;

import javax.swing.*;
import java.util.*;
/**
 * A ListModel backed by an ArrayList.
 *
 * @author  werni
 */
public class ArrayListModel extends javax.swing.AbstractListModel
implements List/*, RandomAccess*/ {
    private ArrayList delegate;
    
    /** Creates a new instance of ArrayListModel */
    public ArrayListModel() {
        delegate = new ArrayList();
    }
    
    public Object getElementAt(int index) {
        return delegate.get(index);
    }
    
    public int getSize() {
        return delegate.size();
    }
    
    public boolean add(Object o) {
        int index = delegate.size();
        delegate.add(o);
        fireIntervalAdded(this, index, index);
        return true;
    }
    
    public void add(int index, Object element) {
        delegate.add(index, element);
        fireIntervalAdded(this, index, index);
    }
    
    public boolean addAll(Collection c) {
        if (c.size() > 0) {
            int index = delegate.size();
            delegate.addAll(c);
            fireIntervalAdded(this, index, index + c.size() - 1);
            
            return true;
        } else {
            return false;
        }
        
    }
    public boolean addAll(Object[] c) {
        return addAll(Arrays.asList(c));
    }
    
    public boolean addAll(int index, Collection c) {
        delegate.addAll(index, c);
        fireIntervalAdded(this, index, index + c.size() - 1);
        return true;
    }
    
    public void clear() {
        int index1 = delegate.size()-1;
        delegate.clear();
        if (index1 >= 0) {
            fireIntervalRemoved(this, 0, index1);
        }
    }
    
    public boolean contains(Object o) {
        return delegate.contains(o);
    }
    
    public boolean containsAll(Collection c) {
        return delegate.containsAll(c);
    }
    
    public Object get(int index) {
        return delegate.get(index);
    }
    
    public int indexOf(Object o) {
        return delegate.indexOf(o);
    }
    
    public boolean isEmpty() {
        return delegate.isEmpty();
    }
    
    public Iterator iterator() {
        return delegate.iterator();
    }
    
    public int lastIndexOf(Object o) {
        return delegate.lastIndexOf(o);
    }
    
    public ListIterator listIterator() {
        return delegate.listIterator();
    }
    
    public ListIterator listIterator(int index) {
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
    
    public Object remove(int index) {
        Object removed = delegate.remove(index);
        fireIntervalRemoved(this, index, index);
        return removed;
    }
    
    public boolean removeAll(Collection c) {
        boolean hasRemoved = false;
        Iterator i = c.iterator();
        while (i.hasNext()) {
            hasRemoved = remove(i.next()) || hasRemoved;
        }
        return hasRemoved;
    }
    
    public boolean retainAll(Collection c) {
        boolean hasChanged;
        ArrayList temp = delegate;
        delegate = new ArrayList();
        fireIntervalRemoved(this, 0, temp.size() - 1);
        hasChanged = temp.retainAll(c);
        delegate = temp;
        fireIntervalAdded(this, 0, delegate.size() - 1);
        return hasChanged;
    }
    
    public Object set(int index, Object element) {
        Object result = delegate.set(index, element);
        fireContentsChanged(this, index, index);
        return result;
    }
    
    public int size() {
        return delegate.size();
    }
    
    public List subList(int fromIndex, int toIndex) {
        return delegate.subList(fromIndex, toIndex);
    }
    
    public Object[] toArray() {
        return delegate.toArray();
    }
    
    public Object[] toArray(Object[] a) {
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
