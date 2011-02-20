// HParameterizedType.java, created Mon Mar 17 20:19:39 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

/**
 * The <code>HParameterizedType</code> interface describes the behavior
 * of parameterized types such as <code>Collection&lt;Applet&gt;</code> or
 * <code>HashMap&lt;String, Class&gt;</code>.
 * <p>
 * Note that implementations of this interface are free to return
 * distinct objects for the same parameterized type; the identity of
 * an objects implementing this interface may not be used to test for identity 
 * among the parameterized types they represent. (In other words,
 * this interface does not extend <code>ReferenceUnique</code>.)
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HParameterizedType.java,v 1.1 2003-03-18 02:27:02 cananian Exp $
 * @see java.lang.reflect.ParameterizedType
 */
public interface HParameterizedType extends HType {
    /**
     * Returns an array of objects representing the type arguments used
     * to instantiate this parameterized type. The length of the array
     * is the exact number of type arguments to this parameterized type,
     * hence it is always at least 1.
     * @return the actual type arguments to the parameterized type
     * represented by this object.
    */
    public HType[] getActualTypeArguments();
    /**
     * Returns an <code>HClass</code> object representing the generic
     * class or interface that this parameterized type instantiates.
     * @return the class of interface that this parameterized type
     * instantiates.
     */
    public HClass getRawClass();
}
