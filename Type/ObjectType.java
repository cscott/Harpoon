package harpoon.Type;

import java.util.Hashtable;

/**
 * This class implements an object datatype, for arbitrary objects.
 * It also ensures that object types are unique.
 * <p>
 * Each instance of this class represents a unique datatype.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ObjectType.java,v 1.2 1998-07-29 01:20:09 cananian Exp $
 * @see     Type
 * @see     Value
 */
public class ObjectType extends Type {
  /** The class name of this particular object type. */
  String objectClass; // MAYBE SHOULD BE A CLASS OBJECT?

  /** 
   * Creates a new <code>ObjectType</code> object from a given
   * object class name.
   * <BR><STRONG>FOR INTERNAL USE ONLY.</STRONG>
   * <BR>Does not ensure uniqueness.
   * @param oc the object class name.
   */
  private ObjectType(String oc) {
    objectClass = oc;
  }

  /**
   * Get the <code>Type</code> object corresponding to an object
   * of the given class.
   * <p>
   * <code>ObjectType</code> objects are stored in a table to make sure
   * that multiple invocations with a given object class name always
   * return the same object type object.
   *
   * @param et   the element type of the array.
   * @return     a unique <code>ArrayType</code> object representing an
   *             array of this element type. <BR> Repeated calls to
   *             this method with identical arguments will yield
   *             identical <code>ArrayType</code> objects.
   */
  public static ObjectType type(String oc) {
    if (!typeTable.containsKey(oc))
      typeTable.put(oc, new ObjectType(oc));
    return (ObjectType) typeTable.get(oc);
  }
  private static Hashtable typeTable = new Hashtable();

  /** Instantiate this type with an array of <code>Value</code> objects.
   *  @param o the object that this <code>Value</code> should represent.
   */
  public ObjectValue newValue(Object o)
  { return new ObjectValue(this, o); }

  // OPERATIONS ON VALUES.

  /**
   * Perform an operation on operands of this datatype.
   * @param opcode the operation to perform.
   * <BR>   Corresponds to a method name in the typeClass.
   * @param operand an array of operand Values.
   * <BR>   Usually the operands will be of the same Type as this object.
   * @return the result of the computation.
   * @exception ValueException
   *            see below.
   * @exception ValueMethodException
   *            if the desired operation cannot be performed.
   */
  public Value computeValue(String opcode, Value operand[]) 
       throws ValueException
  {
    throw new ValueMethodException("No methods supported on Objects.");
  }
  /**
   * Type-propagate an operation on operands of a given type.
   * @param opcode the operation to perform.
   * @param operand an array of operand types.
   * @return the type of the result of the computation.
   * @exception ValueException
   *            see below.
   * @exception ValueMethodException
   *            if the desired operation cannot be performed.
   */
  public Type computeType(String opcode, Type operand[])
       throws ValueException
  {
    throw new ValueMethodException("No methods supported on Objects.");
  }

  /**
   * Gets the human-readable name of this datatype.
   */
  public String getName()  { return objectClass; }
}
