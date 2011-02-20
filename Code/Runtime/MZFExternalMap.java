// MZFExternalMap.java, created Wed Nov 14 00:03:06 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Runtime;

/**
 * The <code>MZFExternalMap</code> defines the interface which the
 * <code>MZFExternalize</code> code factory (part of 
 * <code>MZFCompressor</code>) uses to interface to an external
 * hashmap.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: MZFExternalMap.java,v 1.3 2002-04-10 03:06:21 cananian Exp $
 */
public abstract class MZFExternalMap {

    public static synchronized native
	int intGET(Object fieldkey, Object obj,
		   int default_value);
    public static synchronized native
	void intSET(Object fieldkey, Object obj,
		    int newvalue, int default_value);
    public static synchronized native
	long longGET(Object fieldkey, Object obj,
		     long default_value);
    public static synchronized native
	void longSET(Object fieldkey, Object obj,
		     long newvalue, long default_value);
    public static synchronized native
	Object ptrGET(Object fieldkey, Object obj,
		      Object default_value);
    public static synchronized native
	void ptrSET(Object fieldkey, Object obj,
		    Object newvalue, Object default_value);
    // convenience.
    public static synchronized final
	float floatGET(Object fieldkey, Object obj,
		       float default_value) {
	return Float.intBitsToFloat
	    (intGET(fieldkey, obj, Float.floatToIntBits(default_value)));
    }
    public static synchronized final
	void floatSET(Object fieldkey, Object obj,
		      float newvalue, float default_value) {
	intSET(fieldkey, obj,
	       Float.floatToIntBits(newvalue),
	       Float.floatToIntBits(default_value));
    }
    public static synchronized final
	double doubleGET(Object fieldkey, Object obj,
			 double default_value) {
	return Double.longBitsToDouble
	    (longGET(fieldkey, obj, Double.doubleToLongBits(default_value)));
    }
    public static synchronized final
	void doubleSET(Object fieldkey, Object obj,
		       double newvalue, double default_value) {
	longSET(fieldkey, obj,
		Double.doubleToLongBits(newvalue),
		Double.doubleToLongBits(default_value));
    }
}
