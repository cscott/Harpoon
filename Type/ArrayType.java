package harpoon.Type;

import java.util.Hashtable;

/**
 * This class implements arrays of arbitrary datatypes.
 * It also ensures that array types are unique.
 * <p>
 * Each instance of this class represents a unique datatype.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ArrayType.java,v 1.4 1998-08-01 22:55:17 cananian Exp $
 * @see     Type
 * @see     Value
 */
public class ArrayType extends Type {
  /** The <code>Type</code> of each element of this array. */
  Type elementType;

  /** 
   * Creates a new <code>ArrayType</code> object from a given
   * <code>Type</code> object.
   * <BR><STRONG>FOR INTERNAL USE ONLY.</STRONG>
   * <BR>Does not ensure uniqueness.
   * @param tc the datatype class object.
   */
  private ArrayType(Type et) {
    elementType = et;
  }

  /**
   * Get the <code>Type</code> object corresponding to an array of
   * the given element type.
   * <p>
   * <code>ArrayType</code> objects are stored in a table to make sure
   * that repeat invocations with a given element type always return the
   * same array type object.  
   *
   * @param et   the element type of the array.
   * @return     a unique <code>ArrayType</code> object representing an
   *             array of this element type. <BR> Repeated calls to
   *             this method with identical arguments will yield
   *             identical <code>ArrayType</code> objects.
   */
  public static ArrayType type(Type et) {
    if (!typeTable.containsKey(et))
      typeTable.put(et, new ArrayType(et));
    return (ArrayType) typeTable.get(et);
  }
  private static Hashtable typeTable = new Hashtable();

  /** Instantiate this type with an array of <code>Value</code> objects.
   *  @param valarr the array of values (of this type) to be represented.
   */
  public ArrayValue newValue(Value[] valarr)
  { return new ArrayValue(this, valarr); }

  // Public Accessor
  /** Returns the element type for this array type. */
  public Type elementType() { return this.elementType; }

  // OPERATIONS ON VALUES.

  /**
   * Perform an operation on an array.  The only operation supported
   * is array-indexing.
   * @param opcode the operation to perform.
   * <BR>   <STRONG>Only "getElement" and "setElement" supported.</STRONG>
   * @param operand an array of operand Values.
   * <BR>   <STRONG>The first must be an ArrayValue.
   *        The second must be a BaseValue that defines the "intValue"
   *        operation.</STRONG>
   * @return the appropriate element of the array.
   * @exception ValueException
   *            see below.
   * @exception InvalidValueException
   *            if the operands are not an <code>ArrayValue</code> and 
   *            <code>BaseValue</code>, respectively.
   * @exception ValueMethodException 
   *            if the BaseValue does not define the "intValue" operation, or
   *            the opcode not "getElement" or "setElement".
   */
  // XXX FIXME: only getElement implemented right now.
  public Value computeValue(String opcode, Value operand[])
       throws ValueException
  {
    // check opcode and operands.
    if (!opcode.equals("getElement"))
      throw new ValueMethodException(opcode + " not supported on ArrayTypes");
    if (operand.length != 2)
      throw new InvalidValueException(operand.length + 
				      " parameters to elementAt");
    if (!(operand[0] instanceof ArrayValue))
      throw new InvalidValueException("elementAt attempted on " +
				      operand[0].type().toString());
    if (!(operand[1] instanceof BaseValue))
      throw new InvalidValueException("elementAt indexed with " +
				      operand[1].type().toString());
    // type cast appropriately
    ArrayValue array = (ArrayValue) operand[0];
    BaseValue  index = (BaseValue)  operand[1];

    // dereference array.
    return array.valueArray[index.intValue()];
  }

  /**
   * Type-propagate an operation on an array.  The only operation supported
   * is array-indexing, which is opcode-string "elementAt".
   * @param opcode the operation to perform.
   * <BR>   <STRONG>Only "elementAt" supported.</STRONG>
   * @param operand an array of operand Types.
   * <BR>   <STRONG>The first must be an ArrayType.
   *        The second must be a BaseType.</STRONG>
   * @return the appropriate element of the array.
   * @exception ValueException
   *            see below.
   * @exception InvalidValueException
   *            if the operands are not an <code>ArrayType</code> and 
   *            <code>BaseType</code>, respectively.
   * @exception ValueMethodException 
   *            if the opcode is not "elementAt".
   */
  public Type computeType(String opcode, Type operand[])
       throws ValueException
  {
    // check opcode and operands.
    if (!opcode.equals("elementAt"))
      throw new ValueMethodException(opcode + " not supported on ArrayTypes");
    if (operand.length != 2)
      throw new InvalidValueException(operand.length + 
				      " parameters to elementAt");
    if (!(operand[0] instanceof ArrayType))
      throw new InvalidValueException("elementAt attempted on " +
				      operand[0].toString());
    if (!(operand[1] instanceof BaseType))
      throw new InvalidValueException("elementAt indexed with " +
				      operand[1].toString());
    // type cast appropriately
    ArrayType array = (ArrayType) operand[0];

    // dereference array.
    return array.elementType;
  }

  /**
   * Gets the human-readable name of this datatype.
   */
  public String getName()  { return elementType.getName() + "[]"; }
}
