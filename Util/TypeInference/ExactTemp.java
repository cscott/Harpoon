// ExactTemp.java, created Sun Apr  2 16:06:58 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.TypeInference;

import harpoon.IR.Quads.Quad;
import harpoon.Temp.Temp;


/**
 * <code>ExactTemp</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: ExactTemp.java,v 1.1.2.1 2000-04-03 02:29:15 salcianu Exp $
 */
public class ExactTemp {
    
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

    private int hash = -1;
    public int hashCode(){
	if(hash == -1)
	    hash = q.hashCode() + t.hashCode();
	return hash;
    }

    /** Pretty printer for debug. */
    public String toString(){
	return "< " + q + " , " + t + " >"; 
    }

}
