
// Induction.java, created Mon Jun 28 13:36:40 1999 by root
// Copyright (C) 1999 root <root@kikashi.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.LowQuad.Loop;

import harpoon.Temp.Temp;
import harpoon.ClassFile.HClass;

import java.util.ArrayList;

/**
 * <code>Induction</code>
 * 
 * @author  root <root@kikashi.lcs.mit.edu>
 * @version $Id: Induction.java,v 1.1.2.1 1999-06-28 22:55:08 bdemsky Exp $
 */
public class Induction {
    
    /*  Want to be able to store structures of the form:
	pi+pb
	or
	(i*a+b)
	    or
	    (i*a+b)*pa+pb
    */
    
    /* pi+pb */
    
    Induction(Temp variable, ArrayList pointeroffset, int intmultiplier) {
	this.pointerindex=true;
	this.variable=variable;
	this.pointeroffset=new ArrayList(pointeroffset);
	this.objectsize=null;
	this.intmultiplier=intmultiplier;
	this.offset=0;
    }
    
    /* (i*a+b) */
    Induction(Temp variable, int offset, int intmultiplier) {
	this.pointerindex=false;
	this.variable=variable;
	this.offset=offset;
	this.intmultiplier=intmultiplier;
	this.objectsize=null;
	this.pointeroffset=new ArrayList();
    }
    
    /* (i*a+b)*pa+pb */
    Induction(Temp variable, int offset, int intmultiplier, HClass objectsize, ArrayList pointeroffset) {
	this.pointerindex=false;
	this.variable=variable;
	this.offset=offset;
	this.intmultiplier=intmultiplier;
	this.objectsize=objectsize;
	this.pointeroffset=new ArrayList(pointeroffset);
    }

    Induction(Induction x) {
	this.pointerindex=x.pointerindex;
	this.variable=x.variable;
	this.offset=x.offset;
	this.intmultiplier=x.intmultiplier;
	this.objectsize=x.objectsize;
	this.pointeroffset=new ArrayList(x.pointeroffset);
    }
    
    public boolean pointerindex;
    public Temp variable;
    public int offset;
    public int intmultiplier;
    public HClass objectsize;
    public ArrayList pointeroffset;

}












