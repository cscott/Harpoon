package harpoon.Type;

import java.util.Vector;

/**
 * This abstract class represents the three types of datatypes we
 * recognize: base data types, object data types, and array data types.
 * <p>
 * Each instance of this class represents a unique datatype.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Type.java,v 1.4 1998-08-01 22:55:17 cananian Exp $
 * @see     BaseType
 * @see     ArrayType
 * @see     ObjectType
 * @see     Value
 */
public abstract class Type {

  // OPERATIONS ON VALUES.

  /**
   * Perform an operation on operands of this datatype.
   * @param opcode the operation to perform.
   * @param operand an array of operand <code>Value</code>s.
   * <BR>   Usually the operands will be of the same Type as this object.
   * @return the result of the computation.
   * @exception ValueException
   *            if the desired operation cannot be performed.
   */
  abstract public Value computeValue(String opcode, Value operand[]) 
       throws ValueException;
  /**
   * Type-propagate an operation on operands of a given type.
   * @param opcode  the operation to perform.
   * @param operand an array of operand <code>Type</code>s.
   * <BR>   Usually the operands will agree in type with this object.
   * @return the type of the result of the operation.
   * @exception ValueException 
   *            if the desired operation cannot be performed.
   */
  abstract public Type computeType(String opcode, Type operand[]) 
       throws ValueException;

  /**
   * Gets the human-readable name of this datatype.
   */
  abstract public String getName();
  /** 
   * Returns a string which describes this datatype.
   * @see #getName
   */
  public String toString() { return getName(); }

  // STATIC FUNCTIONS:

  /** 
   * Given a class name, return either a <code>BaseType</code> or 
   * <code>ObjectType</code>, whichever is correct. <BR>
   * (<Code>BaseType</code>s are all subclassed from 
   *  <code>harpoon.Type.Basic.Datatype</code>.)
   * @exception ClassNotFoundException 
   *            if <code>className</code> does not correspond to a known class.
   */
  // XXX Currently assumes that Class.forName will work -- ideally
  // XXX java classes under compilation need not be in the CLASSPATH.
  public static Type objType(String className) throws ClassNotFoundException {
    return objType(Class.forName(className));
  }
  /** Given a class object, return the correct BaseType or ObjectType */
  static Type objType(Class theClass) {
    // Check to see if this class is a subclass of harpoon.Type.Basic.Datatype
    for (Class c=theClass; c!=null; c=c.getSuperclass())
      if (c == harpoon.Type.Basic.Datatype.class)
        return BaseType.type(theClass);
    return ObjectType.type(theClass.getName());
  }
  
  // PARSE VARIOUS KINDS OF SIGNATURES

  /** 
   * Parse the signature of a simple variable or field.
   * @param sig the signature to parse.
   * @return    the Type represented by the signature.
   */
  public static Type parseSimple(String sig)
  {
    switch (sig.charAt(0)) {
      // SHORTS, BYTES, and CHARS are INTeger computational types
      // See table 3.2 in the JVM spec.
    case 'S': // return BaseType.type("harpoon.Type.Basic.Short"); // FALL THRU
    case 'B': // return BaseType.type("harpoon.Type.Basic.Byte");  // FALL THRU
    case 'C': // return BaseType.type("harpoon.Type.Basic.Char");  // FALL THRU
    case 'Z': // return BaseType.type("harpoon.Type.Basic.Boolean");//FALL THRU
    case 'I': return BaseType.type(harpoon.Type.Basic.Int.class);

    case 'D': return BaseType.type(harpoon.Type.Basic.Double.class);
    case 'F': return BaseType.type(harpoon.Type.Basic.Float.class);
    case 'J': return BaseType.type(harpoon.Type.Basic.Long.class);
    case 'L': return ObjectType.type(sig.substring(1, sig.indexOf(';')));
    case '[': return ArrayType.type(parseSimple(sig.substring(1)));
    default: throw new Error("Illegal simple signature: "+sig);
    }
  }
  /**
   * Parse the parameter signatures of a method descriptor.
   * @param sig the method descriptor to parse.
   * @return an array of Types corresponding to the method parameters.
   */
  public static Type[] parseMethodParams(String sig) {
    if (sig.charAt(0)!='(') 
      throw new Error("Not a method signature: "+sig);
    Vector v = new Vector();
    int paren = sig.indexOf(')');
    for (int i=1; i<paren; i++) {
      // add this type to the vector.
      v.addElement(parseSimple(sig.substring(i)));
      // skip over class name for object type.
      if (sig.charAt(i)=='L')
	i=sig.indexOf(';', i);
      // skip over array dimensions for array type.
      while (sig.charAt(i)=='[')
	i++;
    }
    Type params[] = new Type[v.size()];
    v.copyInto(params);
    return params;
  }
  /**
   * Parse the return-type portion of a method descriptor.
   * @param sig the method descriptor.
   * @return the Type corresponding to the return-type of the method.
   */
  public static Type parseMethodResult(String sig) {
    if (sig.charAt(0)!='(') 
      throw new Error("Not a method signature: "+sig);
    return parseSimple(sig.substring(sig.indexOf(')')+1));
  }
}
