// QuadLiveness.java, created Wed Oct 27 17:17:49 1999 by kkz
// Copyright (C) 1999 Karen K. Zee <kkzee@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import harpoon.Analysis.Liveness;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.CALL;
import harpoon.Util.Util;
import harpoon.Util.WorkSet;
import harpoon.Temp.Temp;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

/**
 * <code>QuadLiveness</code> performs live variable analysis for a given
 * <code>HCode</code>. Since it caches results, you should create a new
 * <code>QuadLiveness</code> if you have changed the <code>HCode</code>.
 * 
 * @author Karen K. Zee <kkzee@alum.mit.edu>
 * @version $Id: QuadLiveness.java,v 1.1.2.4 2000-01-02 22:19:36 bdemsky Exp $
 */
public class QuadLiveness extends Liveness {
    final Hashtable livein;
    final Hashtable liveout;
    final Hashtable tempin;
    final Hashtable tempout;

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
    }

    /** Returns the <code>Set</code> of <code>Temp</code>s 
     *  that are live-in at the <code>HCodeElement</code>. 
     *  Requires that the <code>HCodeElement</code> be in 
     *  quad-no-ssa form. Returns <code>null</code> if there
     *  no live-in variables.
     */
    public Set getLiveIn(HCodeElement hce) {
	return (Set)this.livein.get((Quad)hce);
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
	return (Set)this.liveout.get((Quad)hce);
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

    private Hashtable[] analyze() {
	System.out.println("Entering QuadLiveness.analyze()");
	WorkSet ws = new WorkSet(this.hc.getElementsL());

	if (ws.isEmpty()) {
	    Hashtable[] retval = {new Hashtable(), new Hashtable()};
	    System.out.println("Leaving QuadLiveness.analyze()");
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

	    // Create a WorkSet of successors
	    WorkSet successors = new WorkSet(hce.succC());

	    for (Iterator i=successors.iterator(); i.hasNext(); ) {
		Quad successor = (Quad)((HCodeEdge)i.next()).to();
		if (in.containsKey(successor)) {
		    for (Iterator j=((WorkSet)in.get(successor)).iterator(); 
			 j.hasNext(); ) {
			out_.add(j.next());
		    }
		}
	    }

	    // Add our results to out
	    out.put(hce, out_);

	    WorkSet in_ = new WorkSet();
	    
	    // Create a WorkSet of Temps that read in the current HCodeElement
	    WorkSet use = new WorkSet(hce.useC());

	    for (Iterator i=use.iterator(); i.hasNext(); ) {
		in_.add(i.next());
	    }

	    // Create a WorkSet of (out_ - def) for the current HCodeElement
	    WorkSet tmp = new WorkSet();
	    for (Iterator i = out_.iterator(); i.hasNext(); ) {
		tmp.add(i.next());
	    }
	    WorkSet def = new WorkSet(hce.defC());
	    for (Iterator i=def.iterator(); i.hasNext(); ) {
		tmp.remove(i.next());
	    }

	    // Union (out_ - def) with in_
	    for (Iterator i=tmp.iterator(); i.hasNext(); ) {
		in_.add(i.next());
	    }

	    // If we have grown our live-in or live-out variables,
	    // we need to update all of its predecessors.
	    WorkSet predecessors = new WorkSet(hce.predC());
	    WorkSet old_in = (WorkSet)in.put(hce, in_);
	    WorkSet old_out = (WorkSet)out.put(hce, out_);
	    if (old_in != null && old_in.size() < in_.size()) {
		for (Iterator i=predecessors.iterator(); i.hasNext(); ) {
		    Quad predecessor = (Quad)((HCodeEdge)i.next()).from();
		    ws.add(predecessor);
		}
	    } else if (old_out != null && old_out.size() < out_.size()) {
		for (Iterator i=predecessors.iterator(); i.hasNext(); ) {
		    Quad predecessor = (Quad)i.next();
		    ws.add(predecessor);
		}
	    }

	    if (ws.isEmpty()) {
		Hashtable[] retval = {in, out};
		System.out.println("Leaving QuadLiveness.analyze()");
		return retval;
	    }
	    hce = (Quad)ws.pull();
	}
    }
}
