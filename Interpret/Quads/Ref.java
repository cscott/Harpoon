// Ref.java, created Mon Dec 28 00:29:30 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Quads;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.Util.Util;

/**
 * <code>Ref</code> is an abstract superclass for object and array references.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Ref.java,v 1.2 2002-02-25 21:05:46 cananian Exp $
 */
abstract class Ref implements Cloneable, java.io.Serializable {
    /** The type of the object. */
    final HClass type;
    /** A pointer to the static state, so we can finalize. */
    final StaticState ss;
    /** A monitor lock. */
    //boolean lock;
    /** Profiling information. */
    /*final*/ long creation_time;

    /** create a new ref.
     * @exception InterpretedThrowable
     *            if class initializer throws an exception.  */
    Ref(StaticState ss, HClass type) {
	this.ss = ss; this.type = type;
	/*this.lock = false;*/
	// load class into StaticState, if needed.
	if (!ss.isLoaded(type)) ss.load(type);
	this.creation_time = ss.getInstructionCount();
	// yay, done.
    }

    synchronized void lock() { /* FIXME */ }
    synchronized void unlock() { /* FIXME */ }

    // arrays have a single final field.
    abstract Object get(HField f);

    public abstract Object clone();
   
    // for profiling only
    protected abstract int size();

    protected void finalize() throws Throwable {
	// profile
	ss.profile(this.type, this.creation_time, ss.getInstructionCount(),
		   this.size());
	// finalize the actual object.
	super.finalize();
    }

    // UTILITY:
    static final Object defaultValue(HField f) {
	if (f.isConstant()) return f.getConstant();
	return defaultValue(f.getType());
    }
    static final Object defaultValue(HClass ty) {
	if (!ty.isPrimitive()) return null;
	if (ty == HClass.Boolean) return new Boolean(false);
	if (ty == HClass.Byte) return new Byte((byte)0);
	if (ty == HClass.Char) return new Character((char)0);
	if (ty == HClass.Double) return new Double(0);
	if (ty == HClass.Float) return new Float(0);
	if (ty == HClass.Int) return new Integer(0);
	if (ty == HClass.Long) return new Long(0);
	if (ty == HClass.Short) return new Short((short)0);
	throw new Error("Ack!  What kinda default value is this?!");
    }
}
