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
 * @version $Id: METHOD.java,v 1.1.2.4 1999-08-06 16:35:28 pnkfelix Exp $
 */
public class METHOD extends Stm {
    /** The temporary variables used for method formals. */
    public TEMP[] params;
    
    /** Creates a <code>METHOD</code> object. 
     * @param params   temporaries which map directly to the 
     *                 parameters of this <code>METHOD</code>.
     *                 The first element should be a pointer to the 
     *                 exception-handling code for this method.  For 
     *                 non-static methods, the second parameter should be the 
     *                 <code>this</code> pointer of the caller.  Subsequent 
     *                 elements of the array should be the formal parameters
     *                 of the method, in the order which they are declared. 
     */
    public METHOD(TreeFactory tf, HCodeElement source, TEMP[] params) { 
        super(tf, source);
	Util.assert(params!=null); Util.assert(params.length>0);
	for (int i=0; i<params.length; i++) Util.assert(params[i].tf == tf);
	this.params = params;
    }

    public ExpList kids() {
	ExpList retval = new ExpList(params[params.length-1], null);
	for (int i=params.length-2; i>=0; i--)
	    retval = new ExpList(params[i], retval);
	return retval;
    }

    public int kind() { return TreeKind.METHOD; }

    public Stm build(ExpList kids) { return build(tf, kids); } 

    public Stm build(TreeFactory tf, ExpList kids) { 
	List sParams = new ArrayList();
	for (ExpList e=kids; e!=null; e=e.tail) {
	    Util.assert(e.head.tf == tf);
	    sParams.add(e.head);
	}
	TEMP[] tParams = (TEMP[])sParams.toArray(new TEMP[0]);
	return new METHOD(tf, this, tParams);
    }

    public Tree rename(TreeFactory tf, CloningTempMap ctm) { 
	TEMP[] newTmps = new TEMP[params.length];
	for (int i=0; i<params.length; i++) 
	    newTmps[i] = (TEMP)params[i].rename(tf, ctm);
	return new METHOD(tf,this,newTmps);
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
	sb.append(params[params.length-1].toString());
	sb.append(")");
	return sb.toString();
    }
}
