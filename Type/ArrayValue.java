package harpoon.Type;

/**
 * This class represents a particular instantiation of an array with
 * constant size and values.   Element indexing can be performed with
 * the eval method to allow constant propagation and similar optimizations.
 *
 * @author  C. Scott Ananian (cananian@alumni.princeton.edu)
 * @version $Id: ArrayValue.java,v 1.2 1998-08-01 22:50:08 cananian Exp $
 * @see     Type
 * @see     ValueException
 */
public class ArrayValue extends Value {
  /** The parent datatype of this value. */
  ArrayType type;
  /** The array of constant values which this object represents. */
  Value[] valueArray;

  /** 
   * Creates an ArrayValue object from an array of Value objects.
   * All the <code>Value</code> objects should have the same type.
   * @param at the type of this array.
   * @param va the values and size of the array.
   */
  ArrayValue(ArrayType at, Value[] va) {
    this.type = type;
    this.valueArray = va;
  }

  /** Returns the <code>Type</code> of a <code>ArrayValue</code> object. */
  public Type type() { return this.type; }

  /** 
   * Converts the array into a human-readable format by invoking
   * toString() on its elements.
   */
  public String toString() { 
    StringBuffer sb = new StringBuffer("{ ");
    for (int i=0; i<valueArray.length; i++)
      sb.append(valueArray[i].toString()+", ");
    sb.append(" }");
    return sb.toString();
  }
}
