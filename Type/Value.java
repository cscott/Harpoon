package harpoon.Type;

/**
 * This class represents a particular value for a particular datatype.
 * Constant leaves in the IR Tree are represented as instances of this
 * class; arithmetic can be performed using methods here to allow
 * implementation of constant propagation and similar optimizations.
 * <p>
 * We generalize datatypes and values to one of three subclasses of
 * <code>Value</code>: <code>BaseValue</code>, <code>ArrayValue</code>, 
 * and <code>ObjectValue</code>.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Value.java,v 1.1 1998-07-29 00:56:47 cananian Exp $
 * @see     Type
 * @see     BaseValue
 * @see     ArrayValue
 * @see     ObjectValue
 */
abstract public class Value implements TypedObject {
  /** Returns the <code>Type</code> of a <code>Value</code> object. */
  abstract public Type type();

  /** Returns a string representation of the <code>Value</code>. */
  abstract public String toString();
}
