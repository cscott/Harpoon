// TEMP.java, created Wed Jan 13 21:14:57 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

import java.util.HashSet;
import java.util.Set;

/**
 * <code>TEMP</code> objects are expressions which stand for a
 * value in a virtual register.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: TEMP.java,v 1.1.2.22 2000-01-09 01:04:41 duncan Exp $
 */
public class TEMP extends Exp {
    /** The <code>Temp</code> which this <code>TEMP</code> refers to. */
    public final Temp temp;
    /** The type of this <code>Temp</code> expression. */
    public final int type;
    /** Constructor. */
    public TEMP(TreeFactory tf, HCodeElement source, int type, Temp temp) {
	super(tf, source);
	this.type=type; this.temp=temp;
	Util.assert(Type.isValid(type));
	Util.assert(temp!=null);
	Util.assert((temp.tempFactory() == tf.tempFactory()) ||
		    (temp.tempFactory() == tf.getFrame().getRegFileInfo().regTempFactory()),
		    (tf.getFrame().getRegFileInfo().isRegister(temp)?"Register factory":
		    "Non-register factory") + " is not equal");
    }

    public Tree getFirstChild() { return null; } 
    
    public Set useSet() {
	Set set = new HashSet();
	set.add(temp);
	return set;
    }
  
    public int kind() { return TreeKind.TEMP; }

    public Exp build(ExpList kids) { return build(tf, kids); } 
    public Exp build(TreeFactory tf, ExpList kids) {
	Util.assert(tf.tempFactory() == temp.tempFactory() ||
		    tf.getFrame().getRegFileInfo().regTempFactory() == temp.tempFactory());
	return new TEMP(tf, this, type, temp); 
    }

    // Typed interface:
    public int type() { return type; }

    /** Accept a visitor */
    public void accept(TreeVisitor v) { v.visit(this); }

    public Tree rename(TreeFactory tf, CloningTempMap ctm) {
	// Registers are immutable
	if (getFactory().getFrame().getRegFileInfo().isRegister(this.temp)) {
	    Util.assert(tf.getFrame().getRegFileInfo().isRegister(temp));
	    return new TEMP(tf, this, this.type, this.temp);
	}
	else {
	    Util.assert(getFactory().tempFactory() == this.temp.tempFactory());
	    return new TEMP(tf, this, this.type, map(ctm, this.temp));
	}
    }

    public String toString() {
        return "TEMP<"+Type.toString(type)+">("+temp+")";
    }
}
