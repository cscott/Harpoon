// BasicCSE.java, created Sat Sep 26 04:27:12 1998 by marinov
// Copyright (C) 1998 Darko Marinov <marinov@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import harpoon.IR.Quads.CONST;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.MOVE;
import harpoon.IR.Quads.OPER;
import harpoon.IR.Quads.Qop;
import harpoon.IR.Quads.Quad;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.Temp.Temp;
import harpoon.Util.Collections.UniqueVector;
import java.util.Vector;
import java.util.Hashtable;

/**
 * <code>BasicCSE</code> is an attempt to perform
 * common subexpression elemination, but only within basic blocks.
 * 
 * @author  Darko Marinov <marinov@lcs.mit.edu>
 * @version $Id: BasicCSE.java,v 1.2 2002-02-25 20:59:22 cananian Exp $
 */

public class BasicCSE  {
    
    private BasicCSE() { }

    /** <code>BasicBlock</code> represents sequence of quadruples. */
    private static class BasicBlock {
	private Quad start,end;
	public Quad getStart() {return start;}
	public Quad getEnd() {return end;}
	public BasicBlock(Quad start, Quad end) {
	    this.start=start; this.end=end;
	}
	/** Returns quadruples of the basic block in execution order. */
	public Quad[] executionOrder() {
	    Vector c = new Vector();
	    for (Quad q=start; ;q=q.next(0)) {
		c.addElement(q);
		if (q==end) break;
	    }
	    Quad[] r=new Quad[c.size()];
	    c.copyInto(r); return r;
	}
    }

    /** Returns array of basic blocks in code starting with quad start. */ 
    private static BasicBlock[] findBasicBlocks (Quad start) {
	Vector c = new Vector();
	UniqueVector b = new UniqueVector(1);
	b.addElement(start);
	for (int i=0; i<b.size(); i++) {
	    Quad s=(Quad)b.elementAt(i);
	    Quad t=s;
	    while ((t.nextEdge().length==1)&&(t.next(0).prevEdge().length==1))
		t=t.next(0);
	    c.addElement(new BasicBlock(s,t));
	    Quad[] n=t.next();
	    for (int j=0; j<n.length; j++)
		b.addElement(n[j]);
	}
	BasicBlock[] r=new BasicBlock[c.size()];
	c.copyInto(r); return r;
    }

    /** Eliminates common subexpression within basic blocks 
     *	in quad-ssi <code>HCode</code>. Uses value numbering algorithm. */
    public static void optimize (HCode hc) {
	BasicBlock[] bbs=findBasicBlocks((Quad)hc.getRootElement());
	// for all basic blocks
	for (int i=0; i<bbs.length; i++) {
	    Hashtable var2val = new Hashtable();  // variable to value map
	    Hashtable exp2val = new Hashtable();  // expression to value
	    Hashtable exp2var = new Hashtable();  // expression to variable
	    Hashtable con2val = new Hashtable();  // constant to value
	    int virtval = 0;
	    Quad[] in = bbs[i].executionOrder();
	    // for all instructions
	    for (int j=0; j<in.length; j++) {
		// if instruction is (n-ary) operation dst=op(src1...srcn)
 		if (in[j] instanceof OPER) {
		    OPER ins=(OPER) in[j];
		    Vector exp = new Vector(1);
		    exp.addElement(Qop.toString(ins.opcode()));
		    // for all src 
		    for (int k=0; k<ins.operandsLength(); k++) {
			Integer v=(Integer)var2val.get(ins.operands(k));
			// if var2val(src)==no value than var2val=new value 
			if (v==null) { 
			    virtval++; Integer vv=new Integer(virtval);
			    var2val.put(ins.operands(k),vv);
			    exp.addElement(vv);
			} else exp.addElement(v);
		    }
		    String str=exp.toString();
		    Integer v=(Integer) exp2val.get(str);
		    // if exp2val(op(var2val(src1)...var2val(srcn)))==no value
		    if (v==null) {
			Temp t = new Temp(ins.dst());
			// insert MOVE t,dst quad
			Quad[] q=ins.next();
			if ((q.length==1)&&(q[0].prev().length==1)) {
			    MOVE m=new MOVE(ins.getFactory(),ins,t,ins.dst());
			    Quad.addEdge(m,0,q[0],0);
			    Quad.addEdge(ins,0,m,0);
			    exp2var.put(str,t);
			}
			virtval++; Integer vv=new Integer(virtval);
			exp2val.put(str,vv);
			var2val.put(ins.dst(),vv);
		    } else {
			Temp t=(Temp) exp2var.get(str);
			// change OPER quad to a MOVE one
			if (ins.edges().length==2) { 
			    MOVE m=new MOVE(ins.getFactory(),ins,ins.dst(),t);
			    Quad next=ins.next(0); Quad prev=ins.prev(0);
			    if ((prev.next(0)==ins)&&(next.prev(0)==ins)) {
				Quad.addEdge(m,0,next,0);
				Quad.addEdge(prev,0,m,0);
			    }
			}
			var2val.put(ins.dst(),v);
		    }
		    // if instruction is move, just set var2val for src and dst
		} else if (in[j] instanceof MOVE) {
		    MOVE ins = (MOVE) in[j];
		    Integer v=(Integer) var2val.get(ins.src());
		    if (v==null) {
			virtval++; Integer vv=new Integer(virtval);
			var2val.put(ins.src(),vv);
			var2val.put(ins.dst(),vv);
		    }
		    else var2val.put(ins.dst(),v);
		    // if instruction is const, set var2val for dst, but also con2val for src
		} else if (in[j] instanceof CONST) {
		    CONST ins = (CONST) in[j];
		    String str=ins.type().toString() + 
			((ins.value()==null)?"null":ins.value().toString());
		    Integer v=(Integer) con2val.get(str);
		    if (v==null) {
			virtval++; Integer vv=new Integer(virtval);
			con2val.put(str,vv);
			var2val.put(ins.dst(),vv);
		    }
		    else var2val.put(ins.dst(),v);
		}
	    } //for all instructions
	} //foreach basic block
    } //optimize
    
}
