// DefaultNameMap.java, created Tue Aug 10 17:47:50 1999 by cananian
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Maps;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.Util.Util;

/**
 * <code>DefaultNameMap</code> implements a
 * <A HREF="http://java.sun.com/products/jdk/1.2/docs/guide/jni/index.html"
 * >JNI</a>-compliant method name mangling, and class and field name mangling
 * "in the spirit of" the JNI.<p>
 * The resulting names are C-compliant; that is, they can be referenced
 * from native code written in C.
 *
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DefaultNameMap.java,v 1.3.2.1 2002-02-27 08:34:48 cananian Exp $
 */
public class DefaultNameMap extends NameMap {
    private final boolean prependUnderscore;

    private static final String member_prefix = "_Flex_";
    private static final String class_prefix = "_Class_";
    private static final String primitive_prefix = "_Primitive_";
    private static final String string_prefix = "_String_";
    private static final String suffix_sep = "_9"; // "$" is another option.

    /** Creates a <code>DefaultNameMap</code>.  If 
     *  <code>prependUnderscore</code> is <code>true</code>, then
     *  underscores are prepended to c function names.  Otherwise,
     *  the appear in the assembly output exactly as they do in C.
     */
    public DefaultNameMap(boolean prependUnderscore) {
	this.prependUnderscore = prependUnderscore;
    }

    /* Map C function names to assembly label strings. */
    public String c_function_name(String fn) {
	if (prependUnderscore) return "_"+fn; else return fn;
    }

    /** Mangle a method name. */
    public String mangle(HMethod hm, String suffix) {
	String desc = hm.getDescriptor();
	return member_prefix +
	    encode(hm.getDeclaringClass().getName()) + "_" +
	    encode(hm.getName()) + "__" +
	    encode(desc.substring(1,desc.lastIndexOf(')'))) +
	    (suffix==null?"":(suffix_sep+encode(suffix)));
    }
    /** Mangle a field name. */
    public String mangle(HField hf, String suffix) {
	// won't conflict with method names because of "__" in
	// method name (see above). A field "_" of class "foo" would
	// be manged as "foo__1", and since there are not type
	// descriptor strings starting with a number, this can
	// not be confused with a method named "foo".
	return member_prefix +
	    encode(hf.getDeclaringClass().getName()) + "_" +
	    encode(hf.getName()) +
	    (suffix==null?"":(suffix_sep+encode(suffix)));
    }
    /** Mangle a class name. */
    public String mangle(HClass hc, String suffix) {
	String sufstr = (suffix==null?"":(suffix_sep+encode(suffix)));
	if (hc.isPrimitive())
	    return primitive_prefix + hc.getName() + sufstr;
	else
	    return class_prefix + encode(hc.getName()) + sufstr;
    }

    /** Mangle a string constant reference. */
    public String mangle(String string_constant, String suffix) {
	String base = string_prefix + toHex(string_constant.hashCode(), 8);
	String r = base;
	for (int i=0;
	     strMap.containsKey(r) && !strMap.get(r).equals(string_constant);
	     r = base + "x" + i++)
	    /* do nothing */;
	assert !strMap.containsKey(r) ||
		    strMap.get(r).equals(string_constant);
	strMap.put(r, string_constant);
	return r + (suffix==null?"":(suffix_sep+encode(suffix)));
    }
    private final java.util.Map strMap = new java.util.HashMap();
    
    //----------------------------------------------------------
    /** Apply the JNI-standard unicode-to-C encoding. */
    private static String encode(String s) {
	StringBuffer sb = new StringBuffer();
	for(int i=0; i<s.length(); i++) {
	    switch(s.charAt(i)) {
	    case '.':
	    case '/':
		sb.append("_");
		break;
	    case '_':
		sb.append("_1");
		break;
	    case ';':
		sb.append("_2");
		break;
	    case '[':
		sb.append("_3");
		break;
	    default:
		if ((s.charAt(i) >= 'a' &&
		     s.charAt(i) <= 'z') ||
		    (s.charAt(i) >= 'A' &&
		     s.charAt(i) <= 'Z') ||
		    (s.charAt(i) >= '0' &&
		     s.charAt(i) <= '9')) {
		    sb.append(s.charAt(i));
		} else {
		    sb.append("_0" + toHex(s.charAt(i), 4));
		}
		break;
	    }
	}
	return sb.toString();
    }
    /** Convert the integer <code>value</code> into a hexadecimal 
     *  string with at least <code>num_digits</code> digits. */
    private static String toHex(int value, int num_digits) {
	String hexval= // javah puts all hex vals in lowercase.
	    Integer.toHexString(value).toLowerCase();
	while(hexval.length()<num_digits) hexval="0"+hexval;
	return hexval;
    }
}
