// NameMap.java, created Fri Aug  6 17:41:55 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Maps;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;

import harpoon.Temp.Label;
/**
 * <code>NameMap</code> gives a translation from methods, classes,
 * and fields to unique string labels legal in assembly code.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: NameMap.java,v 1.3 2003-04-19 01:03:54 salcianu Exp $
 */
public abstract class NameMap implements java.io.Serializable {
    /** Maps a C function name to the appropriate label string.  For
     *  many platforms, the label string has an underscore prepended.
     *  For others, the label string is the function name exactly. */
    public abstract String c_function_name(String function_name);

    /** Mangle a method name. */
    public String mangle(HMethod hm) { return mangle(hm, null); }
    /** Mangle a method name, adding a uniqueness suffix.
     *  The generated string is guaranteed not to conflict with
     *  any other mangled string from a different method, field,
     *  or class, or any other mangled string from this method
     *  with a different suffix. The suffix may be <code>null</code>;
     *  the string returned in this case is idential to that
     *  obtained by a call to <code>mangle(hm)</code> (with no
     *  specified suffix). */
    public abstract String mangle(HMethod hm, String suffix);

    /** Mangle a field name. */
    public String mangle(HField hf) { return mangle(hf, null); }
    /** Mangle a field name, adding a uniqueness suffix.
     *  The generated string is guaranteed not to conflict with
     *  any other mangled string from a different method, field,
     *  or class, or any other mangled string from this field
     *  with a different suffix. The suffix may be <code>null</code>;
     *  the string returned in this case is idential to that
     *  obtained by a call to <code>mangle(hf)</code> (with no
     *  specified suffix). */
    public abstract String mangle(HField hf, String suffix);

    /** Mangle a class name. */
    public String mangle(HClass hc) { return mangle(hc, null); }
    /** Mangle a class name, adding a uniqueness suffix.
     *  The generated string is guaranteed not to conflict with
     *  any other mangled string from a different method, field,
     *  or class, or any other mangled string from this class
     *  with a different suffix. The suffix may be <code>null</code>;
     *  the string returned in this case is idential to that
     *  obtained by a call to <code>mangle(hf)</code> (with no
     *  specified suffix). */
    public abstract String mangle(HClass hc, String suffix);

    /** Mangle a reference to a string constant. */
    public String mangle(String string_constant) {
	return mangle(string_constant, null);
    }
    /** Mangle a reference to a string constant, adding a uniqueness
     *  suffix.  The generated string is guaranteed not to conflict
     *  with any other mangled string from any method, field, class,
     *  or string constant reference, or any other mangled reference
     *  to this string constant with a different suffix.  The suffix
     *  may be <code>null</code>; the string returned in this case is
     *  idential to that obtained by a call to
     *  <code>mangle(string_constant)</code> (with no specified
     *  suffix). */
    public abstract String mangle(String string_constant, String suffix);

    // CONVENIENCE CLASSES FOR COMMON CASES:
    /** Maps an <code>HClass</code> to a <code>Label</code> representing the 
     *  location of its class pointer  */
    public Label label(HClass hc) { return new Label(mangle(hc)); }
    /** Maps an <code>HClass</code> to a <code>Label</code> representing the
     *  class data structure associated with the given suffix. */
    public Label label(HClass hc, String suffix)
    { return new Label(mangle(hc, suffix)); }
    /** Maps a static <code>HField</code> to a <code>Label</code>. */
    public Label label(HField hf) { return new Label(mangle(hf)); }
    /** Maps an <code>HField</code> to a <code>Label</code> representing the
     *  field information structure associated with the given suffix. */
    public Label label(HField hf, String suffix)
    { return new Label(mangle(hf, suffix)); }
    /** Maps an <code>HMethod</code> to a <code>Label</code>. Note that
     *  the method does not have to be static or final; in many cases we
     *  can determine the identity of a virtual function exactly using 
     *  type information, and <code>label()</code> should return a
     *  <code>Label</code> we can use to take advantage of this information. */
    public Label label(HMethod hm) { return new Label(mangle(hm)); }
    /** Maps an <code>HMethod</code> to a <code>Label</code> representing the
     *  method information structure associated with the given suffix. */
    public Label label(HMethod hm, String suffix)
    { return new Label(mangle(hm, suffix)); }
    /** Maps a <code>String</code> constant to a <code>Label</code>. */
    public Label label(String stringConstant) { return new Label(mangle(stringConstant)); }
    /** Maps a <code>String</code> constant to a <code>Label</code>
     *  representing the string data structure associated with the given
     *  suffix. */
    public Label label(String stringConstant, String suffix)
    { return new Label(mangle(stringConstant, suffix)); }
}
