// QuadLiveness.java, created Wed Oct 27 17:17:49 1999 by kkz
// Copyright (C) 1999 Karen K. Zee <kkz@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import harpoon.Analysis.Liveness;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.PHI;
import harpoon.Util.Util;
import harpoon.Util.Collections.WorkSet;
import harpoon.Temp.Temp;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

/**
 * <code>QuadLiveness</code> performs live variable analysis for a given
 * <code>HCode</code>. Since it caches results, you should create a new
 * <code>QuadLiveness</code> if you have changed the <code>HCode</code>.
 * 
 * @author Karen K. Zee <kkz@alum.mit.edu>
 * @version $Id: QuadLiveness.java,v 1.2 2002-02-25 20:59:23 cananian Exp $
 */
public class QuadLiveness extends Liveness {
    final Hashtable livein;
    final Hashtable liveout;
    final Hashtable tempin;
    final Hashtable tempout;
    final Hashtable tempinout;

    /** Creates a <code>QuadLiveness</code>. Requires 
     *  that the <code>HCode</code> be quad-no-ssa.
     */
    public QuadLiveness(HCode hc) {
	super(hc);
       	Hashtable[] live = this.analyze();
	this.livein = live[0];
	this.liveout = live[1];
	this.tempin = new Hashtable();
	this.tempout = new Hashtable();
	this.tempinout = new Hashtable();
    }

    /** Returns the <code>Set</code> of <code>Temp</code>s 
     *  that are live-in at the <code>HCodeElement</code>. 
     *  Requires that the <code>HCodeElement</code> be in 
     *  quad-no-ssa form. Returns <code>null</code> if there
     *  no live-in variables.
     */
    public Set getLiveIn(HCodeElement hce) {
	return new WorkSet((Set)livein.get((Quad)hce));
    }
    /** Same as getLiveIn, but returns array of <code>Temp</code>s.  This
	array is guaranteed to have the Temp's in the same order for a given
	QuadLiveness object,Quad pair.**/

    public Temp[] getLiveInArray(HCodeElement hce) {
	if (tempin.containsKey(hce))
	    return (Temp[]) Util.safeCopy(Temp.arrayFactory, (Temp[]) tempin.get(hce));
	else {
	    Set set=(Set) this.livein.get((Quad) hce);
	    Temp[] retval=(Temp[]) set.toArray(new Temp[set.size()]);
	    tempin.put(hce,retval);
	    return (Temp[]) Util.safeCopy(Temp.arrayFactory, retval);
	}
    }
    /** Returns the <code>Set</code> of <code>Temp</code>s 
     *  that are live-out at the <code>HCodeElement</code>. 
     *  Requires that the <code>HCodeElement</code> be in 
     *  quad-no-ssa form. Returns <code>null</code> if there
     *  no live-in variables.
     */
    public Set getLiveOut(HCodeElement hce) {
	return new WorkSet((Set)liveout.get((Quad)hce));
    }

    /** Same as getLiveOut, but returns array of <code>Temp</code>s.
	Makes the same order guarantees as <code>getLiveInArray</code>.**/
    public Temp[] getLiveOutArray(HCodeElement hce) {
	if (tempout.containsKey(hce))
	    return (Temp[]) Util.safeCopy(Temp.arrayFactory, (Temp[]) tempout.get(hce));
	else {
	    Set set=(Set) this.liveout.get((Quad) hce);
	    Temp[] retval=(Temp[]) set.toArray(new Temp[set.size()]);
	    tempout.put(hce, retval);
	    return (Temp[]) Util.safeCopy(Temp.arrayFactory, retval);
	}
    }

    public Temp[] getLiveInandOutArray(HCodeElement hce) {
	if (tempinout.containsKey(hce))
	    return (Temp[]) Util.safeCopy(Temp.arrayFactory, (Temp[]) tempinout.get(hce));
	else {
	    Set set=(Set) this.liveout.get((Quad) hce);
	    Set setin=(Set) this.livein.get((Quad) hce);
	    Iterator iter=set.iterator();
	    int count=0;
	    while (iter.hasNext())
		if (setin.contains(iter.next()))
		    count++;
	    Temp[] retval=new Temp[count];
	    iter=set.iterator();
	    count=0;
	    while (iter.hasNext()) {
		Temp tt=(Temp) iter.next();
		if (setin.contains(tt))
		    retval[count++]=tt;
	    }
	    tempinout.put(hce, retval);
	    return (Temp[]) Util.safeCopy(Temp.arrayFactory, retval);
	}
    }

    private Hashtable[] analyze() {
	//System.err.println("Entering QuadLiveness.analyze()");
	WorkSet ws = new WorkSet(this.hc.getElementsL());

	if (ws.isEmpty()) {
	    Hashtable[] retval = {new Hashtable(), new Hashtable()};
	    //System.err.println("Leaving QuadLiveness.analyze()");
	    return retval;
	}

	HCodeElement[] leaves = this.hc.getLeafElements();
	
	// For efficiency reasons, we want to start w/ a leaf
	// element if one exists. Otherwise, just start w/
	// any element.
	Quad hce = null;
	if (leaves != null) {
	    hce = (Quad)leaves[0];
	    ws.remove(hce);
	} else {
	    hce = (Quad)ws.pull();
	}

	Hashtable in = new Hashtable();
	Hashtable out = new Hashtable();

	while (true) {
	    WorkSet out_ = new WorkSet();


	    for (int i=0;i<hce.nextLength();i++) {
		Quad successor=hce.next(i);
		if (in.containsKey(successor)) {
		    if (successor instanceof PHI) {
			WorkSet w=new WorkSet((Set)out.get(successor));
			
			//search for successor
			int edge=hce.nextEdge(i).which_pred();
			PHI phi=(PHI)successor;
			
			for (int j=0;j<phi.numPhis();j++)
			    w.remove(phi.dst(j));
			
			for (int j=0;j<phi.numPhis();j++)
			    w.add(phi.src(j,edge));
			out_.addAll(w);
		    } else
			out_.addAll((WorkSet)in.get(successor));
		}
	    }

	    // Calculate "in" Set
	    WorkSet in_ = new WorkSet(out_);
	    in_.removeAll(hce.defC());
	    in_.addAll(hce.useC());

	    // If we have grown our live-in or live-out variables,
	    // we need to update all of its predecessors.
	    // TRICK: doing a put on a Hashtable returns the previous mapping
	    WorkSet old_in = (WorkSet)in.put(hce, in_);
	    WorkSet old_out = (WorkSet)out.put(hce, out_);
	    if ((old_in == null)|| (old_in.size() < in_.size())) {
		for (int i=0;i<hce.prevLength();i++)
		    ws.push(hce.prev(i));
	    } else if ((old_out == null)|| ( old_out.size() < out_.size())) {
		for (int i=0;i<hce.prevLength();i++)
		    ws.push(hce.prev(i));
	    }

	    if (ws.isEmpty()) break;
	    hce = (Quad)ws.pull();
	}
	return new Hashtable[] {in, out};
    }
}

