package harpoon.Interpret.Tree;

import harpoon.ClassFile.HMethod;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TEMP;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * <code>TreeStackFrame</code> is a stack frame for an interpreted method.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TreeStackFrame.java,v 1.1.2.3 1999-05-17 20:10:01 andyb Exp $
 */
final class TreeStackFrame extends StackFrame {
    /** current location in the method */
    Stm pc;
    /** current mapping from <code>Temp</code>s to values. */
    final Hashtable state = new Hashtable();
    /** an object to stand for <code>null</code>. */
    static final private Object Onull = new Object();

    /** Class constructor. */
    TreeStackFrame(Stm initial_pc) { this.pc = initial_pc; }

    /** Replaces all instances of <code>obj1</code> in this stack frame
     *  with <code>obj2</code>. */
    void replace(Object obj1, Object obj2) {
	for (Enumeration e = state.keys(); e.hasMoreElements();) {
	    Object nextKey = e.nextElement();
	    if (state.get(nextKey).equals(obj1)) {
		state.put(nextKey, obj2);
	    }
	}
	if (state.containsKey(obj1)) {
	    state.put(obj2, state.get(obj1));
	    state.remove(obj1);
	}
    }

    void update(Temp t, Object value) {
	//if (DEBUG) db("Updating: " + t + " --> " + value);
	state.put(t, (value==null)?Onull:value);
    }
     
    void update(Exp exp, Object value) {
	//if (DEBUG) db("Updating: " + exp + " --> " + value);
	state.put(exp, (value==null)?Onull:value);
    }

    Object get(Temp t) {
	Object o = state.get(t);
	if (o==null) {
	    throw new Error("Use before def of "+t+" at " + getMethod() + 
			    "("+ getSourceFile() +":"+ getLineNumber() +")"+
			    "::" + pc + "\n" + state.toString());
	}
	return (o==Onull)?Method.TREE_NULL:o;
    }

    Object get(Exp e) {
	if (e instanceof TEMP) { 
	    return get(((TEMP)e).temp);
	}
	else { 
	    Object o = state.get(e);
	    if (o==null) {
		throw new Error("Don't yet know the value of "+ e + " at "+ 
				getMethod() + "("+getSourceFile() + ":"+
				getLineNumber() + ")" + "::" + pc + "\n" + 
				state.toString());
	    }
	    return (o==Onull)?Method.TREE_NULL:o;
	}
    }
			  
    boolean isDefined(Exp e) {
	return state.containsKey(e);
    }

    final HMethod getMethod() { return pc.getFactory().getMethod(); }
    final String  getSourceFile() { return pc.getSourceFile(); }
    final int     getLineNumber() { return pc.getLineNumber(); }
}









