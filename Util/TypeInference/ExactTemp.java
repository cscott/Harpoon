// ExactTemp.java, created Sun Apr  2 16:06:58 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.TypeInference;

import harpoon.IR.Quads.Quad;
import harpoon.Temp.Temp;


/**
 * <code>ExactTemp</code> is simply a pair of a <code>Quad</code>
 and a <code>Temp</code>. This is usually used to represent the
 temp t defined in instruction q to make the distinction between this
 definition of t and some other one (this is particularly useful if
 the code is not in the SSA form).<br>
 However, since an <code>ExactTemp</code> is just a pair <q,t>, it can
 be used to simply denote the temp t used (not defined) in the instruction
 q (as for example in the constructor of <code>TypeInference</code>).
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: ExactTemp.java,v 1.1.2.4 2001-06-17 22:37:18 cananian Exp $
 */
public class ExactTemp implements java.io.Serializable {

    Quad q;
    Temp t;

    /** Creates a <code>ExactTemp</code>. */
    public ExactTemp(Quad q, Temp t) {
        this.q = q;
	this.t = t;
    }

    /** Checks the equality of two <code>ExactTemp</code>s. */
    public boolean equals(Object obj){
	if(obj == null) return false;
	
	ExactTemp et2 = (ExactTemp) obj;
	return q.equals(et2.q) && t.equals(et2.t);
    }

    // caching hack 
    private int hash = -1;
    public int hashCode(){
	if(hash == -1)
	    hash = q.hashCode() + t.hashCode();
	return hash;
    }

    /** Pretty printer for debug. */
    public String toString(){
	return "< " + q.getSourceFile() + ":" + q.getLineNumber() +
	    " " + q + " , " + t + " >"; 
    }

}
