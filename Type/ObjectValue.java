package harpoon.Type;

/**
 * This class represents a particular instantiation of a constant object.
 * I'm not sure exactly what methods can be invoked.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ObjectValue.java,v 1.3 1998-08-01 22:55:17 cananian Exp $
 * @see     Type
 * @see     ValueException
 */
public class ObjectValue {
  /** The parent datatype of this value. */
  ObjectType type;
  /** The constant object. */
  Object obj;

  /** 
   * Creates an ObjectValue object from an object.
   * @param ot  the type of this object.
   * @param obj the value of the object.
   */
  ObjectValue(ObjectType ot, Object obj) {
    this.type = ot;
    this.obj  = obj;
  }

  /** Returns the <code>Type</code> of a <code>ObjectValue</code> object. */
  public Type type() { return this.type; }

  /** 
   * Converts value into a human-readable format (hopefully!) by
   * invoking toString() on the constant object.
   */
  public String toString() { return obj.toString(); }
}
