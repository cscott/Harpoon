// THROW.java, created Thu Feb 18 16:59:53 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

/**
 * <code>THROW</code> objects are used to represent a thrown exception.
 *
 * @author   Duncan Bryce <duncan@lcs.mit.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version  $Id: THROW.java,v 1.3 2002-02-26 22:46:11 cananian Exp $
 */
public class THROW extends Stm implements Typed {
    /** Constructor 
     *  @param retex   the exceptional value to return 
     *  @param handler the location of the exception-handling code to branch to
     */
    public THROW(TreeFactory tf, HCodeElement source, 
		 Exp retex, Exp handler) {
	super(tf, source, 2);
	this.setRetex(retex);
	this.setHandler(handler);

	Util.ASSERT(retex.type()==POINTER); 
	Util.ASSERT(handler.type()==POINTER);
	Util.ASSERT(tf == retex.tf, "This and Retex must have same tree factory");
	Util.ASSERT(tf == handler.tf, "This and Handler must have the same tree factory");
    }		
    
    /** The exceptional value to return */
    public Exp getRetex() { return (Exp) getChild(0); }
    /** The location of the exception-handling code */
    public Exp getHandler() { return (Exp) getChild(1); } 

    /** Set the exceptional value to return */
    public void setRetex(Exp retex) { setChild(0, retex); }
    /** Set the location of the exception-handling code */
    public void setHandler(Exp handler) { setChild(1, handler); }
  
    public int kind() { return TreeKind.THROW; }

    public Stm build(TreeFactory tf, ExpList kids) { 
	Util.ASSERT(kids!=null && kids.tail!=null && kids.tail.tail==null);
	Util.ASSERT(tf == kids.head.tf);
	Util.ASSERT(tf == kids.tail.head.tf);
	return new THROW(tf, this, kids.head, kids.tail.head);
    }

    /** Accept a visitor */
    public void accept(TreeVisitor v) { v.visit(this); }

    public Tree rename(TreeFactory tf, TempMap tm, CloneCallback cb) {
	return cb.callback(this,
			   new THROW(tf,this,
				     (Exp)getRetex().rename(tf, tm, cb),
				     (Exp)getHandler().rename(tf, tm, cb)),
			   tm);
    }

    /** @return <code>Type.POINTER</code> */
    public int type() { return POINTER; }
    public boolean isDoubleWord() { return Type.isDoubleWord(tf, POINTER); }
    public boolean isFloatingPoint() { return false; }
    
    public String toString() {
	return "THROW(#"+getRetex().getID()+", "+getHandler().getID()+")";
    }
}
