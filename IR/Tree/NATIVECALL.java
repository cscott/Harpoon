// CALL.java, created Wed Jan 13 21:14:57 1999 by cananian
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;
import harpoon.Util.Util;

import java.util.HashSet;
import java.util.Set;

/**
 * <code>NATIVECALL</code> objects are statements which stand for
 * function calls using standard C calling convention.  These are
 * typically used to implement parts of the runtime system
 * (for example, to invoke the garbage collector) and <i>not</i>
 * for java native method calls (which must use the standard java
 * method calling convention).
 * 
 * @author  Duncan Bryce <duncan@lcs.mit.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: NATIVECALL.java,v 1.1.2.14 1999-10-23 14:19:52 cananian Exp $
 * @see harpoon.IR.Quads.CALL
 * @see CALL
 * @see INVOCATION
 */
public class NATIVECALL extends INVOCATION {
    /** Constructor. */
    public NATIVECALL(TreeFactory tf, HCodeElement source,
		      TEMP retval, Exp func, ExpList args) {
	super(tf, source, retval, func, args);
    }

    public boolean isNative() { return true; }
  
    public ExpList kids() { 
	ExpList result = new ExpList(func, args);
	if (retval!=null)
	    return new ExpList(retval, result);
	else // make placeholder.
	    return new ExpList(new CONST(tf, this), result);
    }

    public int kind() { return TreeKind.NATIVECALL; }

    public Stm build(ExpList kids) { return build(tf, kids); } 
  
    public Stm build(TreeFactory tf, ExpList kids) {
	TEMP kids_retval=null;
	for (ExpList e = kids; e!=null; e=e.tail)
	    Util.assert(tf == e.head.tf);
	if (kids.head.kind()==TreeKind.TEMP) {
	    kids_retval = (TEMP) kids.head;
	    kids = kids.tail;
	}
	return new NATIVECALL(tf, this,
			      kids_retval,// retval
			      kids.head,  // func
			      kids.tail); // args
    }

    /** Accept a visitor */
    public void accept(TreeVisitor v) { v.visit(this); }

    public Tree rename(TreeFactory tf, CloningTempMap ctm) {
        return new NATIVECALL(tf, this, 
			      (retval==null) ? null :
			      (TEMP)retval.rename(tf, ctm),
			      (Exp)func.rename(tf, ctm),
			      ExpList.rename(args, tf, ctm));
    }

    public String toString() {
        StringBuffer s = new StringBuffer();

        s.append("NATIVECALL(");
	if (retval!=null) s.append(" # "+retval.getID()+",");
	s.append(" #"+func.getID()+", {");
        for (ExpList list = args; list != null; list=list.tail) {
            s.append(" #" + list.head.getID());
            if (list.tail != null) s.append(",");
        }
        s.append(" })");
        return new String(s);
    }
}
