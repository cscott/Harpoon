package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;
import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * <code>METHOD</code> objects encode method-specific information:
 * the mapping of method formals to temporary variables, and
 * links to the exception handlers for the method. 
 * 
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: METHOD.java,v 1.1.2.1 1999-08-05 07:54:47 duncan Exp $
 */
public class METHOD extends Stm {
    /** A pointer to the exception handler for this 
     *  <code>METHOD</code> */
    public NAME   handler;
    /** The temporary variables used for method formals. */
    public TEMP[] params;
    
    /** Creates a <code>METHOD</code> object. 
     * @param params   temporaries which map directly to the 
     *                 parameters of this <code>METHOD</code>.
     *                 For non-static methods, the first parameter
     *                 should be the <code>this</code> pointer of 
     *                 the caller.
     * @param handler  a pointer to the exception-handling code
     *                 for this <code>METHOD</code>.
     * */
    public METHOD(TreeFactory tf, HCodeElement source,
		  TEMP[] params, NAME handler) {
        super(tf, source);
	Util.assert(params!=null && handler!=null); 
	Util.assert(handler.tf == tf);
	for (int i=0; i<params.length; i++) 
	    Util.assert(params[i].tf == tf);
	this.params  = params;
	this.handler = handler;
    }

    public ExpList kids() {
	ExpList retval = new ExpList(null, null);
	for (int i=params.length-1; i>=0; i--)
	    retval = new ExpList(params[i], retval);
	return new ExpList(handler, retval);
    }

    public int kind() { return TreeKind.METHOD; }

    public Stm build(ExpList kids) { return build(tf, kids); } 

    public Stm build(TreeFactory tf, ExpList kids) { 
	NAME handler = (NAME)kids.head; Util.assert(handler.tf == tf);
	List sParams = new ArrayList();
	for (ExpList e=kids.tail; e.head!=null; e=e.tail) {
	    Util.assert(e.head.tf == tf);
	    sParams.add(e.head);
	}
	TEMP[] tParams = (TEMP[])sParams.toArray(new TEMP[0]);
	return new METHOD(tf, this, tParams, handler);
    }

    public Tree rename(TreeFactory tf, CloningTempMap ctm) { 
	TEMP[] newTmps = new TEMP[params.length];
	for (int i=0; i<params.length; i++) 
	    newTmps[i] = (TEMP)params[i].rename(tf, ctm);
	return new METHOD
	    (tf,this,newTmps,(NAME)handler.rename(tf,ctm));
    }

    /** Accept a visitor. */
    public void visit(TreeVisitor v) { v.visit(this); }

    protected Set defSet() { 
	Set def = new HashSet();
	for (int i=0; i<params.length; i++)
	    def.add(params[i].temp);
	return def;
    }

    protected Set useSet() { 
	return new HashSet(); 
    }

    /** Returns human-readable representation of this <code>Quad</code>. */
    public String toString() {
	StringBuffer sb = new StringBuffer("METHOD(");
	for (int i=0; i<params.length-1; i++) {
	    sb.append(params[i].toString());
	    sb.append(", ");
	}
	sb.append(params[params.length].toString());
	sb.append(")");
	return sb.toString();
    }
}
