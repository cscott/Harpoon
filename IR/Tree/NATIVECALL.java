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
 * @version $Id: NATIVECALL.java,v 1.1.2.21 2000-01-12 18:02:28 duncan Exp $
 * @see harpoon.IR.Quads.CALL
 * @see CALL
 * @see INVOCATION
 */
public class NATIVECALL extends INVOCATION {
    /** Constructor. */
    private CONST nullRetval; 

    public NATIVECALL(TreeFactory tf, HCodeElement source,
		      TEMP retval, Exp func, ExpList args) {
	super(tf, source, retval, func, args);
	this.setRetval(this.getRetval());	
	this.setFunc(this.getFunc()); 
	this.setArgs(this.getArgs()); 
	if (retval == null) { this.nullRetval = new CONST(tf, null); } 
    }

    public boolean isNative() { return true; }

    // FIXME:  this is an ugly hack which should be cleaned up. 
    public ExpList kids() { 
	ExpList result = new ExpList(this.getFunc(), this.getArgs()); 
	if (this.getRetval() == null) { 
	    result = new ExpList(nullRetval, result); 
	} else { 
	    result = new ExpList(this.getRetval(), result); 
	}
	return result; 
    }

    public int kind() { return TreeKind.NATIVECALL; }

    public Stm build(ExpList kids) { return build(tf, kids); } 
  
    public Stm build(TreeFactory tf, ExpList kids) {
	TEMP kids_retval=null;
	for (ExpList e = kids; e!=null; e=e.tail)
	    Util.assert(tf == e.head.tf);
	if (kids.head.kind()==TreeKind.TEMP)
	    kids_retval = (TEMP) kids.head;
	kids = kids.tail;
	return new NATIVECALL(tf, this,
			      kids_retval,// retval
			      kids.head,  // func
			      kids.tail); // args
    }

    /** Accept a visitor */
    public void accept(TreeVisitor v) { v.visit(this); }

    public Tree rename(TreeFactory tf, CloningTempMap ctm) {
        return new NATIVECALL(tf, this, 
			      (this.getRetval()==null) ? null :
			      (TEMP)this.getRetval().rename(tf, ctm),
			      (Exp)this.getFunc().rename(tf, ctm),
			      ExpList.rename(this.getArgs(), tf, ctm));
    }

    public Tree getFirstChild() { return this.getRetval(); } 
  
    public void setRetval(TEMP retval) { 
	super.setRetval(retval); 
	if (retval != null) { 
	    retval.parent  = this;
	    retval.sibling = this.getFunc(); 
	}
    }

    public void setFunc(Exp func) { 
	super.setFunc(func); 
	func.parent  = this;
	func.sibling = this.getArgs().head; 
	TEMP retval = this.getRetval();
	if (retval != null) { retval.sibling = func; }
    }

    public void setArgs(ExpList args) { 
	super.setArgs(args); 
	Exp prev = this.getFunc(), current; 

	prev.sibling = null; 
	for (ExpList e = args; e != null; e = e.tail) { 
	    current        = e.head; 
	    prev.sibling   = current; 
	    current.parent = this;
	    prev           = current;
	}
    }


    public String toString() {
        StringBuffer s = new StringBuffer();

        s.append("NATIVECALL(");
	if (this.getRetval()!=null) 
	    { s.append(" # "+this.getRetval().getID()+","); } 
	s.append(" #"+this.getFunc().getID()+", {");
        for (ExpList list = this.getArgs(); list != null; list=list.tail) {
            s.append(" #" + list.head.getID());
            if (list.tail != null) s.append(",");
        }
        s.append(" })");
        return new String(s);
    }
}
