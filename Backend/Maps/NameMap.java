// NameMap.java, created Fri Aug  6 17:41:55 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Maps;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;

/**
 * <code>NameMap</code> gives a translation from methods, classes,
 * and fields to unique string labels legal in assembly code.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: NameMap.java,v 1.1.2.4 1999-08-11 03:51:48 cananian Exp $
 */
public abstract class NameMap {
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
}
