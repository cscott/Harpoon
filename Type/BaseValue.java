package harpoon.Type;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * This class represents a particular value for a base datatype.
 * Constant leaves in the IR Tree are represented as instances of this
 * class; arithmetic can be performed using methods here to allow
 * implementation of constant propagation and similar optimizations.
 * <p>
 * We generalize base datatype values to three forms:  types representing 
 * integers which can be initialized with a Java <code>long</code> value,
 * types representing floating-point numbers which can be represented with a 
 * java <code>double</code> value, and all other datatypes (including
 * integers and rational numbers exceeding the precision of the built-in
 * <code>long</code> and <code>double</code> types) which can be initialized
 * from a string encoding.
 * <p>
 * The user-defined datatype is expected to throw an exception if
 * the supplied initializer cannot be represented.  This will be
 * translated into an <code>InvalidValueException</code>.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: BaseValue.java,v 1.4 1998-08-01 22:55:17 cananian Exp $
 * @see     Type
 * @see     Value
 * @see     ValueException
 */
public class BaseValue extends Value {
  /** The parent datatype of this value. */
  BaseType type;
  /** An instance of a java class representing a constant value. */
  Object instance;

  /**
   * Create an instance of a datatype representing an integer constant.
   * @param type  the parent datatype.
   * @param value the integer constant.
   * @exception ValueException
   *            see below.
   * @exception ValueMethodException
   *            if the datatype does not support instantiation from an
   *            integer constant.
   * @exception InvalidValueException
   *            if the given constant cannot be represented by this
   *            datatype.
   */
  BaseValue(BaseType type, long value) throws ValueException
  {
    this.type = type;
    // Specify that we're looking for a constructor that takes a long.
    try {
      Constructor c = type.typeClass.getConstructor(new Class[] {long.class});
      this.instance = c.newInstance(new Object[] {new Long(value)});
    } catch (NoSuchMethodException e) { // thrown from getConstructor.
      throw new ValueMethodException(e.toString());
    } catch (InvocationTargetException e) { // thrown from datatype class
      throw new InvalidValueException(e.getTargetException().toString());
    } catch (Exception e) { // all other (access, etc) exceptions
      throw new ValueMethodException(e.toString());
    }
  }

  /**
   * Create an instance of a datatype representing an floating-point constant.
   * @param type  the parent datatype.
   * @param value the floating-point constant.
   * @exception ValueException
   *            see below.
   * @exception ValueMethodException
   *            if the datatype does not support instantiation from a
   *            floating-point constant.
   * @exception InvalidValueException
   *            if the given constant cannot be represented by this
   *            datatype.
   */
  BaseValue(BaseType type, double value) throws ValueException
  {
    this.type = type;
    // Specify that we're looking for a constructor that takes a double.
    try {
      Constructor c= type.typeClass.getConstructor(new Class[] {double.class});
      this.instance= c.newInstance(new Object[] {new Double(value)});
    } catch (NoSuchMethodException e) { // thrown from getConstructor.
      throw new ValueMethodException(e.toString());
    } catch (InvocationTargetException e) { // thrown from datatype class
      throw new InvalidValueException(e.getTargetException().toString());
    } catch (Exception e) { // all other (access, etc) exceptions
      throw new ValueMethodException(e.toString());
    }
  }

  /**
   * Create an instance of a datatype, using a string representation of 
   * the desired constant.
   * @param type  the parent datatype.
   * @param value a string representation of the constant.
   * @exception ValueException
   *            see below.
   * @exception ValueMethodException
   *            if the datatype does not support instantiation from a
   *            string representation.
   * @exception InvalidValueException
   *            if the given constant cannot be represented by this
   *            datatype.
   */
  BaseValue(BaseType type, String value) throws ValueException
  {
    this.type = type;
    // Specify that we're looking for a constructor that takes a string.
    try {
      Constructor c= type.typeClass.getConstructor(new Class[] {String.class});
      this.instance= c.newInstance(new Object[] {value});
    } catch (NoSuchMethodException e) { // thrown from getConstructor.
      throw new ValueMethodException(e.toString());
    } catch (InvocationTargetException e) { // thrown from datatype class
      throw new InvalidValueException(e.getTargetException().toString());
    } catch (Exception e) { // all other (access, etc) exceptions
      throw new ValueMethodException(e.toString());
    }
  }

  /**
   * Creates a value instance from a datatype object.
   * The type is discovered by interrogation of the object.
   * @param obj the datatype object.
   */
  BaseValue(Object obj) {
    this.type = BaseType.type(obj.getClass());
    this.instance = obj;
  }

  /** Returns the <code>Type</code> of a <code>BaseValue</code> object. */
  public Type type() { return this.type; }

  /** 
   * Converts value into a human-readable format (hopefully!) by
   * invoking toString() on the datatype object.
   */
  public String toString() { return instance.toString(); }

  /** Returns an integer representation of the object by calling
   *  its intValue() method.
   *  @exception InvalidValueException
   *             if intValue() invocation on <code>this</code>
   *             throws an exception (can't convert to int).
   *  @exception ValueMethodException
   *             if intValue() method of <code>this</code> either
   *             doesn't exist or is protected.
   */
  public int intValue() throws InvalidValueException, ValueMethodException { 
    try {
      Method  m = instance.getClass().getMethod("intValue", new Class[0]);
      Integer i = (Integer) m.invoke(instance, new Object[0]);
      return i.intValue();
    } catch (NoSuchMethodException e) {
      throw new ValueMethodException(e.toString());
    } catch (SecurityException e) {
      throw new ValueMethodException(e.toString());
    } catch (IllegalAccessException e) {
      throw new ValueMethodException(e.toString());
    } catch (InvocationTargetException e) {
      throw new InvalidValueException(e.toString());
    }
  }
}
