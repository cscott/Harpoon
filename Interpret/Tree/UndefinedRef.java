// UndefinedRef.java, created Sat Mar 27 17:05:10 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Tree;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Tree.NAME;
import harpoon.Temp.Label;
import harpoon.Util.Util;

/**
 * <code>UndefinedRef</code> is a reference of an unknown type
 * 
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: UndefinedRef.java,v 1.2 2002-02-25 21:06:01 cananian Exp $
 */
public class UndefinedRef extends Ref {
    private Integer       length;
    private Integer       hashCode;
    private ClazPointer   classPtr;

    /** Class constructor. */
    public UndefinedRef(StaticState ss) {
	this(ss, null, null, null);
    }

    /** Class constructor. */
    public UndefinedRef(StaticState ss, Integer length, Integer hashCode,
			ClazPointer classPtr) {
	super(ss, null);
	this.length   = length;
	this.hashCode = hashCode==null?new Integer(this.hashCode()):hashCode;
	this.classPtr = classPtr;
    }

    /** Throws an error. */
    public Object get(HField h) {
	throw new Error("Dont access UndefinedRef");
    }

    public Object clone() {
	throw new Error("Dont clone UndefinedRef");
    }

    /** Updates the value of <code>ptr</code> to be <code>value</code>. */
    static void update(UndefinedPointer ptr, Object value) {
	UndefinedRef ref = (UndefinedRef)ptr.getBase();
	long offset = ptr.getOffset();
	StaticState ss = ref.ss;

	HClass dummy = ss.linker.forName("java.lang.Integer");
	HClass dummyA = ss.HCstringA;

	if (ss.map.hashCodeOffset(dummy)==offset) {
	    // hashcode field isn't used
	}
	else if (ss.map.lengthOffset(dummyA)==offset) {
	    ref.length = (Integer)value;
	    if (ref.classPtr != null) {
		throw new PointerTypeChangedException
		    (new ArrayPointer
		     (new ArrayRef(ss, 
				   ss.getHClass((Label)ref.classPtr.getBase()),
				   new int[] { ref.length.intValue() },
				   ref.length, ref.hashCode, ref.classPtr),
		      0));
	    }
	}
	else if (ss.map.clazzPtrOffset(dummy) == offset) {
	    ref.classPtr = 
	      new ClazPointer((Label)(((ConstPointer)value).getBase()), ss, 0);

	    HClass type = ss.getHClass((Label)ref.classPtr.getBase());
	    if (type.isArray()) { 
		if (ref.length != null) {
		    throw new PointerTypeChangedException
			(new ArrayPointer
			 (new ArrayRef(ss, type, 
				       new int[] { ref.length.intValue() },
				       ref.length, ref.hashCode, ref.classPtr),
			  0));
		}
	    }
	    else {
		throw new PointerTypeChangedException
		    (new FieldPointer
		     (new ObjectRef(ss, type, ref.hashCode, ref.classPtr),
		      0));
	    }
	}
	else {
	    throw new Error
		("Can only update length & type of an undef Pointer");
	}
    }
}








