// ObjectBuilder.java, created Sun Mar 10 23:40:59 2002 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.RuntimeTiny;

import harpoon.Backend.Generic.Runtime.ObjectBuilder.Info;
import harpoon.Backend.Generic.Runtime.ObjectBuilder.ArrayInfo;
import harpoon.Backend.Generic.Runtime.ObjectBuilder.ObjectInfo;
import harpoon.Backend.Maps.FieldMap;
import harpoon.Backend.Maps.NameMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.ALIGN;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.DATUM;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.SEGMENT;
import harpoon.Temp.Label;
import harpoon.Util.ArrayIterator;
import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
/**
 * <code>ObjectBuilder</code>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ObjectBuilder.java,v 1.1.2.1 2002-03-11 21:18:02 cananian Exp $
 */
public class ObjectBuilder extends harpoon.Backend.Runtime1.ObjectBuilder {
    Runtime runtime;
    
    /** Creates a <code>ObjectBuilder</code>. */
    public ObjectBuilder(Runtime runtime) {
        super(runtime);
	this.runtime=runtime;
    }
    public ObjectBuilder(Runtime runtime, RootOracle ro) {
        super(runtime, ro);
	this.runtime=runtime;
    }
    protected Stm makeHeader(TreeFactory tf, Info info, boolean exported)
    {
	List stmlist = new ArrayList(4);
	// align to word boundary.
	stmlist.add(new ALIGN(tf, null, 4));
	// label:
	stmlist.add(new LABEL(tf, null, info.label(), exported));
	// claz *index* XXX this is the unique part for RuntimeTiny.
	if (runtime.clazShrink) {
	    int num = runtime.cn.clazNumber(info.type());
	    stmlist.add(new DATUM(tf, null, new CONST(tf, null, num)));
	} else {
	    // boring, same as superclass
	    stmlist.add(new DATUM(tf, null, new NAME(tf, null,
				  runtime.getNameMap().label(info.type()))));
	}
	// hash code.
	// this is of pointer size, and must have the low bit set.  we *could*
	// emit a symbolic reference to info.label()+1 or some such, but
	// this would complicate the pattern-matching instruction selector.
	// so instead we'll just select a random number of the right length
	// and set the low bit.
	stmlist.add(pointersAreLong ?
		    new DATUM(tf, null, new CONST(tf, null, 1|rnd.nextLong())):
		    new DATUM(tf, null, new CONST(tf, null, 1|rnd.nextInt())));
	// okay, done with header.
	return Stm.toStm(stmlist);
    }
}
