
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
 * @version $Id: Induction.java,v 1.1.2.4 1999-07-01 15:50:48 bdemsky Exp $
 * This class allows us to store information on Basic/Derived Induction variables.
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
    
    /** Creates basic pointer induction variable.*/
    Induction(Temp variable, ArrayList pointeroffset, int intmultiplier) {
	this.pointerindex=true;
	this.variable=variable;
	this.pointeroffset=new ArrayList(pointeroffset);
	this.objectsize=null;
	this.intmultiplier=intmultiplier;
	this.offset=0;
	this.copied=false;
    }
    
    /* Creates basic integer induction variable.*/
    Induction(Temp variable, int offset, int intmultiplier) {
	this.pointerindex=false;
	this.variable=variable;
	this.offset=offset;
	this.intmultiplier=intmultiplier;
	this.objectsize=null;
	this.pointeroffset=new ArrayList();
	this.copied=false;
    }
    
    /* Creates derived generalized induction variable.*/
    Induction(Temp variable, int offset, int intmultiplier, HClass objectsize, ArrayList pointeroffset) {
	this.pointerindex=false;
	this.variable=variable;
	this.offset=offset;
	this.intmultiplier=intmultiplier;
	this.objectsize=objectsize;
	this.pointeroffset=new ArrayList(pointeroffset);
	this.copied=false;
    }

    /** Copy Constructor*/
    Induction(Induction x) {
	this.pointerindex=x.pointerindex;
	this.variable=x.variable;
	this.offset=x.offset;
	this.intmultiplier=x.intmultiplier;
	this.objectsize=x.objectsize;
	this.pointeroffset=new ArrayList(x.pointeroffset);
	this.copied=false;
	x.copied=true;
    }

    /** toString method returns string describing contents of the class.*/
    public String toString() {
	String temp;
	temp="pi: "+(new Boolean(pointerindex)).toString();
	temp+=" iv: "+variable.toString()+" offset: ";
	temp+=(new Integer(offset)).toString() +" intmultiplier: "+
	    (new Integer(intmultiplier)).toString();
	if (objectsize!=null)
	    temp+=" os: "+objectsize.toString();
	if (pointeroffset!=null)
	    temp+=" poff: "+pointeroffset.toString();
	return temp;
    }
    
    /** The <code>pointerindex</code> <code>boolean</code> describes whether
     *  the Temp induction variable is a pointer [true] or an integer [false].*/
    public boolean pointerindex;

    /** The <code>variable</code> <code>Temp</code> stores the basic induction variable.*/ 
    public Temp variable;
    
    /** The <code>offset</code> int saves an integer offset.*/
    public int offset;
    /** The <code>intmultiplier</code> int saves an integer multiplier, or in the case of derived
     *  pointer induction variables, the relative sign.*/
    public int intmultiplier;
    /** The <code>objectsize</code> <code>HClass</code> saves the array type.*/
    public HClass objectsize;
    /** The <code>ArrayList</code> saves pointer <code>Temp</code>s that are added in.*/
    public ArrayList pointeroffset;
    public boolean copied;
}















