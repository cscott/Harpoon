package harpoon.Util;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 * A unique vector refuses to addElement duplicates.
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: UniqueVector.java,v 1.4 1998-08-01 22:55:18 cananian Exp $
 * @see java.util.Vector
 * @see java.util.Hashtable
 */
public class UniqueVector implements Cloneable {
  Vector    vect;
  Hashtable uniq;

  /** Constructs an empty UniqueVector. */
  public UniqueVector() 
  { vect = new Vector(); uniq = new Hashtable(); }
  /** Constructs an empty UniqueVector with the specified initial capacity. */
  public UniqueVector(int initialCapacity) 
  { vect = new Vector(initialCapacity); uniq=new Hashtable(initialCapacity); }

  /**
   * Adds the specified component to the end of this vector, increasing
   * its size by one, <b>if it doesn't already exist in the vector</b>.
   * Duplicate elements are thrown away.  The capacity of the vector
   * is increased if necessary.
   * @param obj the component to be added.
   */
  public synchronized void addElement(Object obj) {
    if (obj==null) throw new NullPointerException();
    if (uniq.get(obj) != null) return;
    vect.addElement(obj);
    uniq.put(obj, new Integer(vect.size()-1));
  }
  /**
   * Returns the current capacity of this vector.
   * @return the current capacity of this vector.
   */
  public int capacity() { return vect.capacity(); }
  /**
   * Returns a clone of this vector.
   * @return a clone of this vector.
   * @exception CloneNotSupportedException
   *            if the UniqueVector cannot be cloned.
   */
  public synchronized Object clone() throws CloneNotSupportedException {
    UniqueVector v = (UniqueVector)super.clone();
    v.vect = (Vector) vect.clone();
    v.uniq = (Hashtable) uniq.clone();
    return v;
  }
  /**
   * Tests if the specified object is a component in this vector.
   * @param elem an object
   * @return <code>true</code> if the specified object is a component in
   *         this vector; <code>false</code> otherwise.
   */
  public boolean contains(Object elem) {
    return uniq.contains(elem);
  }
  /**
   * Copies the components of this vector into the specified array.
   * The array must be big enough to hold all the objects in this vector.
   * @param anArray the array into which the components get copied.
   */
  public synchronized void copyInto(Object anArray[]) {
    vect.copyInto(anArray);
  }
  /**
   * Returns the component at the specified index.
   * @param index an index into this vector.
   * @return the component at the specified index.
   * @exception ArrayIndexOutOfBoundsException
   *            if an invalid index was given.
   */
  public synchronized Object elementAt(int index) {
    return vect.elementAt(index);
  }
  /**
   * Returns an enumeration of the components of this vector.
   * @return an enumeration of the components of this vector.
   */
  public synchronized Enumeration elements() {
    return vect.elements();
  }
  /**
   * Increases the capacity of this vector, if necessary, to ensure that
   * it can hold at least the number of components specified by the minimum
   * capacity argument.
   * @param minCapacity the desired minimum capacity.
   */
  public synchronized void ensureCapacity(int minCapacity) {
    vect.ensureCapacity(minCapacity);
  }
  /**
   * Returns the first component of this vector.
   * @return the first component of this vector.
   * @exception java.util.NoSuchElementException
   *            if this vector has no components.
   */
  public synchronized Object firstElement() { 
    return vect.firstElement(); 
  }
  /** 
   * Returns the first (and only) occurrence of the given argument, testing
   * for equality using the <code>equals</code> method.
   * @param elem an object
   * @return the index of the first occurrence of the argument in this
   *         vector; returns <code>-1</code> if the object is not found.
   */
  public synchronized int indexOf(Object elem) {
    Integer in = (Integer) uniq.get(elem);
    if (in==null) return -1;
    else return in.intValue();
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
  public synchronized void insertElementAt(Object obj, int index) {
    removeElement(obj);
    vect.insertElementAt(obj, index);
    for (int i=index; i<vect.size(); i++)
      uniq.put(elementAt(i), new Integer(i));
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
  public synchronized Object lastElement() { 
    return vect.elementAt(vect.size()-1); 
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
  public synchronized void removeAllElements() {
    vect.removeAllElements();
    uniq.clear();
  }
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
    if (index<0 || index >= vect.size()) 
      throw new ArrayIndexOutOfBoundsException();
    Object obj = vect.elementAt(index);
    vect.removeElementAt(index);
    uniq.remove(obj);
    // fixup indices.
    for (int i=index; i<vect.size(); i++)
      uniq.put(vect.elementAt(i), new Integer(i));
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
  public synchronized void setElementAt(Object obj, int index) {
    if (vect.elementAt(index).equals(obj)) return;
    removeElement(obj);
    uniq.remove(vect.elementAt(index));
    vect.setElementAt(obj, index);
    uniq.put(obj, new Integer(index));
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
  public synchronized void trimToSize() { vect.trimToSize(); }
}
