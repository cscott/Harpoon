package harpoon.Type;

import java.lang.Class;
import java.lang.ClassNotFoundException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;

/**
 * This class implements arbitrary basic datatypes in our IR Tree.
 * This class handles most of the hard work of manipulating the
 * abstract values.  It also makes sure
 * that <CODE>BaseType</CODE>s are unique, and wraps the 
 * <CODE>Type.Value</CODE> constructor.
 * <p>
 * Each instance of this class represents a unique datatype.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: BaseType.java,v 1.2 1998-07-29 01:20:09 cananian Exp $
 * @see     Type
 * @see     Value
 */
public class BaseType extends Type {
  /** The executable java class which implements this datatype. */
  Class typeClass;

  /**
   * Creates a new type object by invoking the class loader.
   * <BR><STRONG>FOR INTERNAL USE ONLY.</STRONG>
   * @param name the name of the java class implementing this datatype.
   * @exception java.lang.ClassNotFoundException
   *            if the implementation class can not be located.
   */
  private BaseType(String name) throws ClassNotFoundException {
    this(Class.forName(name));
  }
  /** 
   * Creates a new type object from a datatype class object.
   * <BR><STRONG>FOR INTERNAL USE ONLY.</STRONG>
   * @param tc the datatype class object.
   */
  private BaseType(Class tc) {
    typeClass=tc;
  }

  /**
   * Get the <code>BaseType</code> object corresponding to a given 
   * datatype name.
   * <p>
   * <code>BaseType</code> objects are stored in a table to make sure
   * that invocation with a given string always returns the same object.  
   * Also, this avoids overuse of the class loader.
   *
   * @param name  the fully qualified class name of the user datatype.
   * @return      a unique <code>BaseType</code> object representing this
   *              datatype. <BR> Repeated calls to <code>type("abc")</code>
   *              will return the same <code>BaseType</code>.
   * @exception   java.lang.ClassNotFoundException 
   *              if the datatype cannot be found.
   */
  public static BaseType type(String name) throws ClassNotFoundException {
    if (!typeTable.containsKey(name))
      typeTable.put(name, new BaseType(name));
    return (BaseType) typeTable.get(name);
  }
  public static BaseType type(Class tc) {
    String name = tc.getName();
    if (!typeTable.containsKey(name))
      typeTable.put(name, new BaseType(tc));
    return (BaseType) typeTable.get(name);
  }
  private static Hashtable typeTable = new Hashtable();

  /** Instantiate this type with an integer constant.
   *  @param val the integer that this <code>Value</code> should represent.
   *  @exception ValueException
   *             see below.
   *  @exception ValueMethodException
   *             if this datatype cannot be constructed from an integer.
   *  @exception InvalidValueException
   *             if the supplied constant cannot be represented.
   */
  public BaseValue newValue(long val) throws ValueException
  { return new BaseValue(this, val); }

  /** Instantiate this type with a floating-point constant.
   *  @param val the floating-point number that this <code>Value</code> 
   *             should represent.
   *  @exception ValueException
   *             see below.
   *  @exception ValueMethodException
   *             if this datatype cannot be constructed from a floating-point
   *             number.
   *  @exception InvalidValueException
   *             if the supplied constant cannot be represented.
   */
  public BaseValue newValue(double val) throws ValueException
  { return new BaseValue(this, val); }

  /** Instantiate this type with a string.
   *  This is used to initialize objects that do not map cleanly to
   *  a java built-in type (state machines, bignums, etc).
   *  @param val the string representing the value of this datatype.
   *  @exception ValueException
   *             see below.
   *  @exception ValueMethodException
   *             if this datatype cannot be constructed from a string
   *             representation.
   *  @exception InvalidValueException
   *             if the supplied constant cannot be represented.
   */
  public BaseValue newValue(String val) throws ValueException
  { return new BaseValue(this, val); }

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
   * @exception InvalidValueException
   *            if both operands are not BaseValues.
   * @exception ValueException
   *            if the desired operation cannot be performed.
   */
  public Value computeValue(String opcode, Value operand[]) 
       throws ValueException
  {
    // Hunt down the method corresponding to this opcode.
    Class[] paramTypes = new Class [operand.length];
    Object[] params    = new Object[operand.length];
    for (int i=0; i<operand.length; i++) {
      if (!(operand[i] instanceof BaseValue))
	throw new InvalidValueException("BaseValue operation attempted on "+
					operand[i].type().toString());
      BaseValue bv = (BaseValue) operand[i];
      paramTypes[i] = bv.type.typeClass;
      params[i]     = bv.instance;
    }
    // Now invoke the (static) method to get a result object.
    Object result;
    try {
      Method m = typeClass.getMethod(opcode, paramTypes);
      result = m.invoke(null /*static*/, params);
    } catch (InvocationTargetException e) {
      throw new InvalidValueException("Invocation exception: " +
				      e.getTargetException().toString());
    } catch (Exception e) { // Only ones left are security exceptions.
      throw new ValueMethodException("Access denied: "+e.toString());
    }
    // and make a Value object out of it.
    return new BaseValue(result);
  }
  /**
   * Type-propagate an operation on operands of a given type.
   * @param opcode  the operation to perform.
   * @param operand an array of operand Types.
   * @return the type of the result.
   * @exception ValueMethodException
   *            if operands aren't of a <code>BaseType</code>.
   */
  public Type computeType(String opcode, Type operand[]) 
       throws ValueMethodException
  {
    // Hunt down the method corresponding to this opcode.
    Class[] params = new Class [operand.length];
    for (int i=0; i<operand.length; i++) {
      if (!(operand[i] instanceof BaseType))
	throw new ValueMethodException("Can't perform a BaseType operation "+
				       "on a "+operand[i].toString());
      params[i] = ((BaseType) operand[i]).typeClass;
    }
    try {
      // Get the corresponding Method object:
      Method m = typeClass.getMethod(opcode, params);
      // and make a Type object out of the return type.
      return Type.objType(m.getReturnType());
    } catch (NoSuchMethodException e) {
      throw new ValueMethodException("No method found: "+e.toString());
    }
  }

  /**
   * Gets the human-readable name of this datatype.
   */
  public String getName()  { return typeClass.getName(); }
}
