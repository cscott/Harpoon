// TEMP.java, created Wed Jan 13 21:14:57 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.TempMap;
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
 * @version $Id: TEMP.java,v 1.4 2002-04-10 03:05:45 cananian Exp $
 */
public class TEMP extends Exp {
    /** The <code>Temp</code> which this <code>TEMP</code> refers to. */
    public final Temp temp;
    /** The type of this <code>Temp</code> expression. */
    public final int type;
    /** Constructor. */
    public TEMP(TreeFactory tf, HCodeElement source, int type, Temp temp) {
	super(tf, source, 0);
	this.type=type; this.temp=temp;
	assert Type.isValid(type);
	assert temp!=null;
	assert (temp.tempFactory() == tf.tempFactory()) ||
		    (tf.getFrame().getRegFileInfo().isRegister(temp)) : "Non-register temp with non-matching factory";
    }

    public int kind() { return TreeKind.TEMP; }

    public Exp build(TreeFactory tf, ExpList kids) {
	assert kids==null;
	assert tf.tempFactory() == temp.tempFactory() ||
		    tf.getFrame().getRegFileInfo().isRegister(temp);
	return new TEMP(tf, this, type, temp); 
    }

    // Typed interface:
    public int type() { return type; }

    /** Accept a visitor */
    public void accept(TreeVisitor v) { v.visit(this); }

    public Tree rename(TreeFactory tf, TempMap tm, CloneCallback cb) {
	Temp newTemp = this.temp;
	if (tm != null && // don't remap registers!
	    !getFactory().getFrame().getRegFileInfo().isRegister(this.temp))
	    newTemp = tm.tempMap(this.temp);
	return cb.callback(this,
			   new TEMP(tf, this, this.type, newTemp),
			   tm);
    }

    public String toString() {
        return "TEMP<"+Type.toString(type)+">("+temp+")";
    }
}
