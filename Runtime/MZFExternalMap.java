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
 * @version $Id: MZFExternalMap.java,v 1.2 2002-02-25 21:06:26 cananian Exp $
 */
public abstract class MZFExternalMap {

    public static synchronized native
	int intGET(Object obj, Object fieldkey,
		   int default_value);
    public static synchronized native
	void intSET(Object obj, Object fieldkey,
		    int newvalue, int default_value);
    public static synchronized native
	long longGET(Object obj, Object fieldkey,
		     long default_value);
    public static synchronized native
	void longSET(Object obj, Object fieldkey,
		     long newvalue, long default_value);
    public static synchronized native
	Object ptrGET(Object obj, Object fieldkey,
		      Object default_value);
    public static synchronized native
	void ptrSET(Object obj, Object fieldkey,
		    Object newvalue, Object default_value);
    // convenience.
    public static synchronized final
	float floatGET(Object obj, Object fieldkey,
		       float default_value) {
	return Float.intBitsToFloat
	    (intGET(obj, fieldkey, Float.floatToIntBits(default_value)));
    }
    public static synchronized final
	void floatSET(Object obj, Object fieldkey,
		      float newvalue, float default_value) {
	intSET(obj, fieldkey,
	       Float.floatToIntBits(newvalue),
	       Float.floatToIntBits(default_value));
    }
    public static synchronized final
	double doubleGET(Object obj, Object fieldkey,
			 double default_value) {
	return Double.longBitsToDouble
	    (longGET(obj, fieldkey, Double.doubleToLongBits(default_value)));
    }
    public static synchronized final
	void doubleSET(Object obj, Object fieldkey,
		       double newvalue, double default_value) {
	longSET(obj, fieldkey,
		Double.doubleToLongBits(newvalue),
		Double.doubleToLongBits(default_value));
    }
}
