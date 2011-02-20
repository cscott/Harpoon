// UniqueVector.java, created Sat Aug  1  1:09:02 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import harpoon.Util.IteratorEnumerator;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A unique vector refuses to addElement duplicates.
 * <p>Conforms to the JDK 1.2 Collections API.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: UniqueVector.java,v 1.3 2002-04-10 03:07:14 cananian Exp $
 * @see java.util.Vector
 * @see java.util.Hashtable
 */
public class UniqueVector<E> extends AbstractList<E>
  implements Set<E>, Cloneable {
  List<E>  vect;
  Map<E,Integer>   uniq;

  /** Constructs an empty UniqueVector. */
  public UniqueVector() 
  { vect = new ArrayList<E>(); uniq = new HashMap<E,Integer>(); }
  /** Constructs an empty UniqueVector with the specified initial capacity. */
  public UniqueVector(int initialCapacity) 
  { vect = new ArrayList<E>(initialCapacity); uniq=new HashMap<E,Integer>(initialCapacity); }
  /** Constructs a vector containing the elements of the specified 
   *  <code>Collection</code>, in the order they are returned by the
   *  collection's iterator.  Duplicate elements are skipped. */
  public <T extends E> UniqueVector(Collection<T> c) {
    this(c.size());
    for (Iterator<T> it=c.iterator(); it.hasNext(); )
      add(it.next());
  }

  /**
   * Adds the specified component to the end of this vector, increasing
   * its size by one, <b>if it doesn't already exist in the vector</b>.
   * Duplicate elements are thrown away.  The capacity of the vector
   * is increased if necessary.
   * @param obj the component to be added.
   */
  public synchronized void addElement(E obj) {
    add(obj);
  }
  /**
   * Inserts the specified element at the specified position in this list.
   * To maintain uniqueness, any previous instance of this element in the
   * vector is removed prior to insertion.
   * @param obj the element to be inserted.
   */
  public void add(int index, E element) {
    if (element==null) throw new NullPointerException();
    removeElement(element);
    vect.add(index, element);
    for (int i=index; i<vect.size(); i++)
      uniq.put(element, new Integer(i));
  }
  /**
   * Adds the specified component to the end of this vector, increasing
   * its size by one, <b>if it doesn't already exist in the vector</b>.
   * Duplicate elements are thrown away.  The capacity of the vector
   * is increased if necessary.
   * @param obj the component to be added.
   */
  public boolean add(E obj) {
    if (obj==null) throw new NullPointerException();
    if (contains(obj)) return false;
    vect.add(obj);
    uniq.put(obj, new Integer(vect.size()-1));
    return true;
  }

  /**
   * Returns the current capacity of this vector.
   * @exception UnsupportedOperationException not supported.
   */
  public int capacity() { throw new UnsupportedOperationException(); }
  /**
   * Returns a clone of this vector.
   * @return a clone of this vector.
   * @exception CloneNotSupportedException
   *            if the UniqueVector cannot be cloned.
   */
  public synchronized UniqueVector<E> clone() throws CloneNotSupportedException {
    return new UniqueVector<E>(this);
  }
  /**
   * Tests if the specified object is a component in this vector.
   * @param elem an object
   * @return <code>true</code> if the specified object is a component in
   *         this vector; <code>false</code> otherwise.
   */
  public boolean contains(Object elem) {
    return uniq.containsKey(elem);
  }
  /**
   * Copies the components of this vector into the specified array.
   * The array must be big enough to hold all the objects in this vector.
   * @param anArray the array into which the components get copied.
   */
  public synchronized void copyInto(Object anArray[]) {
    if (anArray.length < size()) throw new IndexOutOfBoundsException();
    vect.toArray(anArray);
  }
  public Object[] toArray() { return vect.toArray(); }
  public <T> T[] toArray(T[] a) { return vect.toArray(a); }

  /**
   * Returns the component at the specified index.
   * @param index an index into this vector.
   * @return the component at the specified index.
   * @exception ArrayIndexOutOfBoundsException
   *            if an invalid index was given.
   */
  public synchronized E elementAt(int index) {
    return get(index);
  }
  /** Returns the element at the specified posision in this vector. */
  public E get(int index) {
    return vect.get(index);
  }
  /**
   * Returns an enumeration of the components of this vector.
   * @return an enumeration of the components of this vector.
   */
  public synchronized Enumeration<E> elements() {
    return new IteratorEnumerator<E>(vect.iterator());
  }
     
  /**
   * Increases the capacity of this vector, if necessary, to ensure that
   * it can hold at least the number of components specified by the minimum
   * capacity argument.
   * @param minCapacity the desired minimum capacity.
   */
  public synchronized void ensureCapacity(int minCapacity) {
    ((ArrayList<E>)vect).ensureCapacity(minCapacity);
  }
  /**
   * Returns the first component of this vector.
   * @return the first component of this vector.
   * @exception java.util.NoSuchElementException
   *            if this vector has no components.
   */
  public synchronized E firstElement() { 
    return vect.get(0);
  }
  /** 
   * Returns the first (and only) occurrence of the given argument, testing
   * for equality using the <code>equals</code> method.
   * @param elem an object
   * @return the index of the first occurrence of the argument in this
   *         vector; returns <code>-1</code> if the object is not found.
   */
  public synchronized int indexOf(Object elem) {
    if (!contains(elem)) return -1;
    return uniq.get(elem).intValue();
  }
  /**
   * Returns the first occurrence of the given argument, beginning the search
   * at <code>index</code>, and testing for equality using the 
   * <code>equals</code> method.
   * @param elem an object.
   * @param index the index to start searching from.
   * @return the index of the first occurrence of the object argument in this
   *         vector at position <code>index</code> or later in the vector;
   *         returns <code>-1</code> if the object is not found.
   */
  public synchronized int indexOf(Object elem, int index) {
    int i=indexOf(elem);
    if (i<index) return -1;
    return i;
  }
  /**
   * Inserts the specified object as a component in this vector at the
   * specified <code>index</code>.  Each component in this
   * vector with an index greater or equal to the specified <code>index</code>
   * is shifted upward to have an index one greater than the value it had
   * previously.<p>
   * The index must be a value greater than or equal to <code>0</code> and
   * less than or equal to the current size of the vector.<p>
   * To maintain uniqueness, removed any previous instance of the component
   * in the vector before insertion.
   * @param obj the component to insert.
   * @param index where to insert the new component.
   * @exception ArrayIndexOutOfBoundsException
   *            if the index was invalid.
   */
  public synchronized void insertElementAt(E obj, int index) {
    add(index, obj);
  }
  /**
   * Tests if this vector has no components.
   * @return <code>true</code> if this vector has no components;
   *         <code>false</code> otherwise.
   */
  public boolean isEmpty() { return (vect.size()==0); }
  /**
   * Returns the last component of the vector.
   * @return the last component of the vector, i.e., the component at
   *         index <code>size()-1</code>.
   * @exception java.util.NoSuchElementException
   *            if this vector is empty.
   */
  public synchronized E lastElement() { 
    return vect.get(vect.size()-1); 
  }
  /**
   * Returns the index of the last (and only) occurrence of the specified 
   * object in this vector.
   * @param elem the desired component.
   * @return the index of the last occurrence of the specified object in
   *         this vector; returns <code>-1</code> if the object is not
   *         found.
   */
  public int lastIndexOf(Object elem) { return indexOf(elem); }
  /**
   * Searches backwards for the specified object, starting from the 
   * specified index, and returns an index to it.
   * @param elem the desired component.
   * @param index the index to start searching from.
   * @return the index of the last occurrence of the specified object in
   *         this vector at position less than <code>index</code> in
   *         the vector; <code>-1</code> if the object is not found.
   */
  public synchronized int lastIndexOf(Object elem, int index) {
    int in = lastIndexOf(elem);
    if (in > index) return -1;
    return in;
  }
  /**
   * Removes all components from this vector and sets its size to zero.
   */
  public synchronized void removeAllElements() { clear(); }
  /** Removes all of the elements from this collection. */
  public void clear() { vect.clear(); uniq.clear(); }

  /**
   * Removes the first (and only) occurance of the argument from this
   * vector.  If the object is found in this vector, each component
   * in the vector with an index greater or equal to the object's
   * index is shifted downward to have an index one smaller than 
   * the value it had previously.
   * @param obj the component to be removed.
   * @return <code>true</code> if the argument was a component of this vector;
   *         <code>false</code> otherwise.
   */
  public final synchronized boolean removeElement(Object obj) {
    if (!contains(obj)) return false;
    removeElementAt(indexOf(obj));
    return true;
  }
  /**
   * Deletes the component at the specified index.  Each component in this
   * vector with an index greater than or equal to the specified 
   * <code>index</code> is shifted downward to have an index one smaller
   * than the value it had previously. <p>
   * The index must be a value greater than or equal to <code>0</code> and
   * less than the current size of the vector.
   * @param index the index of the object to remove.
   * @exception ArrayIndexOutOfBoundsException if the index was invalid.
   */
  public synchronized void removeElementAt(int index) {
    try {
      remove(index);
    } catch (IndexOutOfBoundsException e) {
      throw new ArrayIndexOutOfBoundsException();
    }
  }
  /** Removes the element at the specified position in this vector. */
  public E remove(int index) {
    if (index<0 || index >= vect.size()) 
      throw new IndexOutOfBoundsException();
    E obj = vect.get(index);
    vect.remove(index);
    uniq.remove(obj);
    // fixup indices.
    for (int i=index; i<vect.size(); i++)
      uniq.put(vect.get(i), new Integer(i));
    return obj;
  }
  /**
   * Sets the component at the specified <code>index</code> of this vector
   * to be the specified object.  The previous component at that position
   * is discarded.<p>
   * The index must be a value greater than or equal to <code>0</code> and
   * less than the current size of the vector.<p>
   * Nothing is done if the component at index is equal to obj.
   * To maintain uniqueness, any component equal to obj is removed before
   * the setElementAt() is done.
   * @param obj what the component is to be set to.
   * @param index the specified index.
   * @exception ArrayIndexOutOfBoundsException if the index was invalid.
   */
  public synchronized void setElementAt(E obj, int index) {
    set(index, obj);
  }
  /** Replaces the element at the specified position in this vector with the
   *  specified element.
   */
  public E set(int index, E obj) {
    E old = vect.get(index);
    if (old.equals(obj)) return obj;
    remove(obj);
    uniq.put(obj, uniq.remove(old));
    vect.set(index, obj);
    return old;
  }
  /**
   * Returns the number of components in this vector.
   * @return the number of components in this vector.
   */
  public int size() { return vect.size(); }
  /**
   * Returns a string representation of this vector.
   * @return a string representation of this vector.
   */
  public synchronized String toString() { return vect.toString(); }
  /**
   * Trims the capacity of this vector to be the vector's current size.
   * An application can use this operation to minimize the storage of a
   * vector.
   */
  public synchronized void trimToSize() { ((ArrayList<E>) vect).trimToSize(); }
}
