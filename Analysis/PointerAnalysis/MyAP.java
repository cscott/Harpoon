// MyAP.java, created Mon Apr  3 18:22:05 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import harpoon.Analysis.Maps.AllocationInformation;
import harpoon.Analysis.DefaultAllocationInformation;
import harpoon.ClassFile.HClass;
import harpoon.Temp.Temp;

import harpoon.Util.Util;

/**
 * <code>MyAP</code> is my own implementation for the
 <code>AllocationProperties</code>. 
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: MyAP.java,v 1.2 2002-02-25 20:58:39 cananian Exp $
 */
public class MyAP implements AllocationInformation.AllocationProperties,
			     java.io.Serializable,
			     java.lang.Cloneable {
    
    // hasInteriorPointers
    public boolean hip = true;
    // canBeStackAllocated
    public boolean sa  = false;
    // canBeThreadAllocated
    public boolean ta  = false;
    // useOwnHeap
    public boolean uoh = false;
    // make heap (true at the thread object creation sites)
    public boolean mh  = false;
    // sync's on this object are unnecessary
    public boolean ns  = false;

    // the Temp pointing to the thread object on whose stack
    // the NEW quad is going to allocate the object; "null"
    // to indicate the current thread.
    public Temp ah = null;

    private HClass actualClass;

    /** Creates a <code>MyAP</code>. */
    public MyAP(HClass actualClass) {
	this.actualClass = actualClass;
	this.hip =
	    DefaultAllocationInformation.hasInteriorPointers(actualClass);
    }


    public boolean hasInteriorPointers(){
	return hip;
    }

    public boolean canBeStackAllocated(){
	return sa;
    }

    public boolean canBeThreadAllocated(){
	return ta;
    }

    public boolean makeHeap(){
	return mh;
    }

    public boolean noSync() {
	return sa || ta || ns;
    }

    public Temp allocationHeap(){
	return ah;
    }

    public HClass actualClass() {
	return actualClass;
    }


    public Object clone() {
	try{
	    return super.clone();
	}
	catch(CloneNotSupportedException excp){
	    System.exit(1);
	    return null; // should never happen
	}
    }

    /** Pretty printer for debug. */
    public String toString(){
	String hipstr = 
	    hasInteriorPointers() ? "interior ptrs; " : "no interior ptrs; ";

	if(noSync())
	    hipstr += " nosync ";

	if(makeHeap()) hipstr = hipstr + " [make heap] ";

	if(canBeStackAllocated())
	    return hipstr + "Stack allocation";

	if(canBeThreadAllocated()){
	    if(makeHeap())
		return hipstr + " Thread allocation on its own heap";
	    else{
		if(allocationHeap() != null)
		    return (hipstr + "Thread allocation on the heap of " +
			    allocationHeap());
		else
		    return
			hipstr + 
			"Thread allocation on the current thread's heap";
	    }
	}
	return hipstr + "Global heap allocation";
    }
}
