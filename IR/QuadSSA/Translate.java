// Translate.java, created Sat Aug  8 10:53:03 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.Temp.Temp;
import harpoon.ClassFile.*;
import harpoon.ClassFile.Bytecode.Code.ExceptionEntry;

import java.lang.reflect.Modifier;
/**
 * <code>Translate</code> is a utility class to implement the
 * actual Bytecode-to-QuadSSA translation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Translate.java,v 1.2 1998-08-20 22:43:25 cananian Exp $
 */

/*
 * To do: implement Cloneable for State. 
 *        figure out interface for trans(State, ...)
 *        does state mean state *before* opcode exec or *after*?
 */

class Translate  { // not public.
    static class State { // inner class
	/** Current temps used for each position of stack */
	Temp stack[];
	/** Current temps used for local variables */
	Temp lv[];
	/** Current try/catch contexts */
	ExceptionEntry tryBlock[];

	private State(Temp stack[], Temp lv[], ExceptionEntry tryBlock[]) {
	    this.stack = stack; this.lv = lv; this.tryBlock = tryBlock;
	}
	/** Make new state by popping top of stack */
	State pop() {
	    Temp stk[] = new Temp[this.stack.length-1];
	    System.arraycopy(this.stack, 1, stk, 0, stk.length);
	    return new State(stk, lv, tryBlock);
	}
	/** Make new state by pushing temp onto top of stack */
	State push(Temp t) {
	    Temp stk[] = new Temp[this.stack.length+1];
	    System.arraycopy(this.stack, 0, stk, 1, this.stack.length);
	    stk[0] = t;
	    return new State(stk, lv, tryBlock);
	}
	/** Make new state by exiting innermost try/catch context. */
	State exitTry() {
	    ExceptionEntry tb[] = new ExceptionEntry[this.tryBlock.length-1];
	    System.arraycopy(this.tryBlock, 1, tb, 0, tb.length);
	    return new State(stack, lv, tb);
	}
	/** Make new state by entering a new try/catch context. */
	State enterTry(ExceptionEntry ee) {
	    ExceptionEntry tb[] = new ExceptionEntry[this.tryBlock.length+1];
	    System.arraycopy(this.tryBlock, 0, tb, 1, this.tryBlock.length);
	    tb[0] = ee;
	    return new State(stack, lv, tb);
	}
	/** Make new state by changing the temp corresponding to an lv. */
	State assignLV(int lv_index, Temp t) {
	    Temp nlv[] = (Temp[]) lv.clone();
	    nlv[lv_index] = t;
	    return new State(stack, nlv, tryBlock);
	}
	/** Initialize state with temps corresponding to parameters. */
	State(Temp[] locals) {
	    this(new Temp[0], locals, new ExceptionEntry[0]);
	}
    }

    static final Quad trans(harpoon.ClassFile.Bytecode.Code bytecode) {
	// set up initial state.
	String[] paramNames = bytecode.getMethod().getParameterNames();
	Temp[] params = new Temp[paramNames.length];
	for (int i=0; i<params.length; i++)
	    params[i] = new Temp((paramNames[i]==null)?"param":paramNames[i]);

	Temp[] locals = new Temp[bytecode.getMaxLocals()];
	if (!Modifier.isStatic(bytecode.getMethod().getModifiers())) {
	    locals[0] = new Temp("this");
	    System.arraycopy(params, 0, locals, 1, params.length);
	    for (int i=params.length+1; i<locals.length; i++)
		locals[i] = new Temp("lv");
	} else {
	    System.arraycopy(params, 0, locals, 0, params.length);
	    for (int i=params.length; i<locals.length; i++)
		locals[i] = new Temp("lv");
	}

	State s = new State(locals);

	Quad quads = new METHODHEADER(params);

	// translate using state.
	trans(s);

	// return result.
	return quads;
    }
    static final void trans(State s) {
	// FIXME do schtuff here.
    }
}
