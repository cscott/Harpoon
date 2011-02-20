// Induction.java, created Mon Jun 28 13:36:40 1999 by root
// Copyright (C) 1999 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.LowQuad.Loop;

import harpoon.Temp.Temp;
import harpoon.ClassFile.HClass;
import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * <code>Induction</code>
 * 
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: Induction.java,v 1.4 2002-04-10 02:59:57 cananian Exp $
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
    Induction(Temp ptrvariable, ArrayList pointeroffset, boolean ptrsign) {
	this.ptrvariable=ptrvariable;
	this.pointeroffset=new ArrayList(pointeroffset);
	this.integers=null;
	this.objectsize=null;
	this.ptrsign=ptrsign;
	this.copied=false;
    }
    
    /* Creates basic integer induction variable.*/
    Induction(Temp variable, int offset, int intmultiplier) {
	this.ptrvariable=null;
	this.integers=new IntMultAdd(variable,intmultiplier,offset);
	this.ptrsign=true;
	this.objectsize=null;
	this.pointeroffset=new ArrayList();
	this.copied=false;
    }
    
    
    /** Copy Constructor*/
    Induction(Induction x) {
	this.ptrvariable=x.ptrvariable;
	this.integers=new IntMultAdd(x.integers);
	this.ptrsign=x.ptrsign;
	this.objectsize=x.objectsize;
	this.pointeroffset=new ArrayList(x.pointeroffset);
	this.copied=false;
	x.copied=true;
    }
    
    /** Copy Constructor*/
    Induction(Induction x, HClass objectsize) {
	this.ptrvariable=x.ptrvariable;
	this.integers=new IntMultAdd(x.integers);
	this.ptrsign=x.ptrsign;
	this.objectsize=objectsize;
	this.pointeroffset=new ArrayList(x.pointeroffset);
	this.copied=false;
	x.copied=true;
    }

    /** Constant operator*/
    private Induction(Induction x, boolean operation, int operand) {
	//bool
	this.ptrvariable=x.ptrvariable;
	this.integers=new IntMultAdd(x.integers);
	this.ptrsign=x.ptrsign;
	this.objectsize=x.objectsize;
	this.pointeroffset=new ArrayList(x.pointeroffset);
	this.copied=false;
	x.copied=true;
	if (operation) {
	    //Multiply
	    if (this.integers.loopinvariant()!=null) {
		this.integers=new IntMultAdd(x.integers, operand,0);
	    } else
		this.integers.multiply(operand);
	} else {
	    //add
	    if (this.integers.loopinvariant()!=null) {
		this.integers=new IntMultAdd(x.integers, 1, operand);
	    } else 
		this.integers.add(operand);
	}
    }

    /** Loop invariant operator*/
    private Induction(Induction x, boolean operation, Temp operand) {
	this.ptrvariable=x.ptrvariable;
	this.integers=new IntMultAdd(x.integers);
	this.ptrsign=x.ptrsign;
	this.objectsize=x.objectsize;
	this.pointeroffset=new ArrayList(x.pointeroffset);
	this.copied=false;
	x.copied=true;
	if (this.integers.loopinvariant()!=null) {
	    this.integers=new IntMultAdd(x.integers, operand, operation);
	} else {
	    this.integers.loopinvariant(operand);
	    this.integers.multiply(operation);
	}
    }


    public Induction add(int operand) {
	return new Induction(this, false, operand );
    }

    public Induction multiply(int operand) {
	return new Induction(this, true, operand);
    }

    public Induction add(Temp operand) {
	return new Induction(this, false, operand);
    }

    public Induction multiply(Temp operand) {
	return new Induction(this, true, operand);
    }

    public void padd(Temp operand) {
	pointeroffset.add(new Object[] {operand, new Boolean(true)});
    }

    public IntMultAdd bottom() {
	return integers.bottom();
    }

    public Induction negate() {
	Induction temp=new Induction(this);
	if (temp.ptrvariable!=null)
	    temp.ptrsign=(!temp.ptrsign);
	else 
	    temp.integers.negate();
	Iterator iterate=temp.pointeroffset.iterator();
	temp.pointeroffset=new ArrayList();
	while (iterate.hasNext()) {
	    Object[] ptr=(Object[])iterate.next();
	    temp.pointeroffset.add(new Object[] {ptr[0], new Boolean(!((Boolean)ptr[1]).booleanValue())});
	}
	return temp;
    }

    /** toString method returns string describing contents of the class.*/
    public String toString() {
	String temp;
	temp=" iv: "+variable().toString()+" offset: ";
	if (constant())
	    temp+=(new Integer(offset())).toString() +" intmultiplier: "+
		(new Integer(intmultiplier())).toString();
	if (objectsize!=null)
	    temp+=" os: "+objectsize.toString();
	if (pointeroffset!=null)
	    temp+=" poff: "+pointeroffset.toString();
	return temp;
    }
    

    /** The <code>pointerindex</code> <code>boolean</code> describes whether
     *  the Temp induction variable is a pointer [true] or an 
     *  integer [false].*/
    public boolean pointerindex;

    /** The <code>variable</code> <code>Temp</code> stores the basic 
     *  induction variable.*/ 
    public Temp variable() {
	IntMultAdd ptr=integers;
	while (ptr.child()!=null)
	    ptr=ptr.child();
	return ptr.inductionvar();
    }
    
    public boolean constant() {
	return integers.constant();
    }

    public int intmultiplier() {
	assert this.constant();
	return integers.intmultiplier();
    }

    public int offset() {
	assert this.constant();
	return integers.offset();
    }

    public int depth() {
	if (integers!=null)
	    return integers.depth();
	else return 0;
    }

    /** The <code>intmultadd</code> int saves integer arithmetic.*/
    private IntMultAdd integers;

    public Temp ptrvariable;

    /** The <code>ptrsign</code> saves the relative sign. 
     *  True being the same sign*/
    public boolean ptrsign;


    /** The <code>objectsize</code> <code>HClass</code> 
     *  saves the array type.*/
    public HClass objectsize;


    /** The <code>ArrayList</code> saves pointer <code>Temp</code>s
     *  that are added in.*/
    public ArrayList pointeroffset;
    public boolean copied;


    public class IntMultAdd {
	//Form:
	//(ax+b)?loopinvariant
	IntMultAdd(Temp inductionvar, int intmultiplier, int offset) {
	    this.intmultiplier=intmultiplier;
	    this.offset=offset;
	    this.inductionvar=inductionvar;
	}

	IntMultAdd(IntMultAdd x, int intmultiplier, int offset) {
	    this.intmultiplier=intmultiplier;
	    this.offset=offset;
	    this.inductionvar=null;
	    this.child=new IntMultAdd(x);
	    this.child.parent=this;
	}

	IntMultAdd(IntMultAdd x) {
	    this.intmultiplier=x.intmultiplier;
	    this.offset=x.offset;
	    this.inductionvar=x.inductionvar;
	    this.multiply=x.multiply;
	    this.loopinvariant=x.loopinvariant;
	    this.invariantsign=x.invariantsign;
	    if (x.child!=null) {
		this.child=new IntMultAdd(x.child);
		this.child.parent=this;
	    }
	    else
		this.child=null;
	}

	IntMultAdd(IntMultAdd x, Temp operand, boolean multiply) {
	    this.intmultiplier=1;
	    this.offset=0;
	    this.inductionvar=null;
	    this.loopinvariant=operand;
	    this.invariantsign=true;
	    this.multiply=multiply;
	    this.child=new IntMultAdd(x);
	    this.child.parent=this;
	}


	public IntMultAdd bottom() {
	    IntMultAdd ptr=this;
	    while (ptr.child()!=null)
		ptr=ptr.child();
	    return ptr;
	}

	public int depth() {
	    IntMultAdd ptr=this;
	    int count=1;
	    while (ptr.child()!=null) {
		ptr=ptr.child;
		count++;
	    }
	    return count;
	}

	public void multiply(int x) {
	    intmultiplier*=x;
	    offset*=x;
	}

	public void add(int x) {
	    offset+=x;
	}

	public void negate() {
	    intmultiplier=-intmultiplier;
	    offset=-offset;
	    invariantsign=!invariantsign;
	}

	public boolean constant() {
	    if ((child==null)&&(loopinvariant==null))
		return true;
	    else
		return false;
	}

	public IntMultAdd parent() {
	    return parent;
	}

	public int intmultiplier() {
	    return intmultiplier;
	}

	public int offset() {
	    return offset;
	}
	
	public Temp inductionvar() {
	    return inductionvar;
	}
	
	public IntMultAdd child() {
	    return child;
	}

	public void multiply(boolean operation) {
	    this.multiply=operation;
	}

	public boolean multiply() {
	    return multiply;
	}

	public Temp loopinvariant() {
	    return loopinvariant;
	}

	public void loopinvariant(Temp loopinvariant) {
	    this.loopinvariant=loopinvariant;
	}
	
        public boolean invariantsign() {
	    return invariantsign;
	}

	private boolean multiply; //operation true: ?=*/ false: ?=+
	private Temp loopinvariant; //loop invariant
	private boolean invariantsign;

        private int intmultiplier;//a
	private int offset;//b
	private IntMultAdd parent;
      	private IntMultAdd child;//x
	private Temp inductionvar;//x
    }
}
