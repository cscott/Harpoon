// PointsToQuadVisitor.java, created Sat Jul 13 17:33:45 2002 by kkz
// Copyright (C) 2000 Karen Zee <kkz@tmi.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PreciseGC;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HConstructor;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.IR.Properties.CFGEdge;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.ARRAYINIT;
import harpoon.IR.Quads.ASET;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.FOOTER;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.MOVE;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.SET;
import harpoon.IR.Quads.SIGMA;
import harpoon.IR.Quads.THROW;
import harpoon.Temp.Temp;
import harpoon.Util.Worklist;
import harpoon.Util.Collections.WorkSet;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * <code>PointsToQuadVisitor</code> performs local points to analysis,
 * and can be subclassed for more specific purposes.
 * 
 * @author  Karen Zee <kkz@tmi.lcs.mit.edu>
 * @version $Id: PointsToQuadVisitor.java,v 1.1 2002-07-18 21:06:00 kkz Exp $ */
public class PointsToQuadVisitor extends harpoon.IR.Quads.QuadVisitor {
    
    private final boolean DEBUG1 = false;

    protected final Worklist toDo = new WorkSet();
    protected final Code code;
    
    /** Creates a <code>PointsToQuadVisitor</code>. */
    public PointsToQuadVisitor(Code code) {
	this.code = code;
    }

    // map of CFGEdges to aliases (Sets of Temps)
    protected final Map EdgeToTemps = new HashMap();
	
    // Performs analysis with the given initial data flow fact for the
    // entry point of the method.
    protected void analyze(Set aliases) {
	METHOD start = ((HEADER) code.getRootElement()).method();
	assert start.nextLength() == 1;
	// initialize dataflow facts
	EdgeToTemps.put(start.nextEdge(0), aliases);
	// initialize to-do list
	toDo.push(start.next(0));
	// go, gadget, go!
	while(!toDo.isEmpty()) {
	    Quad q = (Quad) toDo.pull();
	    if (DEBUG1) System.out.println(q);
	    q.accept(this);
	}
    }

    public void visit(CALL q) {
	assert q.prevLength() == 1;
	Set aliases = get(q.prevEdge(0));
	// handle normal edge
	Temp retval = q.retval();
	if (retval != null)
	    aliases.remove(retval);
	handleSIGMAEdge(q, new HashSet(aliases), 0);
	// handle exception edge
	Temp retex = q.retex();
	if (retex != null) {
	    aliases.remove(retex);
	    // if retex == null, then the CALL has only
	    // one outgoing edge (only happens in quad-
	    // with-try), so only handle exception edge 
	    // if retex != null
	    handleSIGMAEdge(q, new HashSet(aliases), 1);
	}
    }

    public void visit(FOOTER q) { /* do nothing */ }
    
    public void visit(MOVE q) {
	assert q.prevLength() == 1 && q.nextLength() == 1;
	Set aliases = new HashSet(get(q.prevEdge(0)));
	if (aliases.contains(q.src()))
	    aliases.add(q.dst());
	else
	    aliases.remove(q.dst());
	raiseValue(q.nextEdge(0), aliases);
    }
    
    public void visit (PHI q) {
	// start with the first non-null edge
	int i;
	Set aliases = null;
	for (i = 0; i < q.arity(); i++) {
	    aliases = get(q.prevEdge(i));
	    if (aliases != null) break;
	}
	if  (aliases == null) {
	    raiseValue(q.nextEdge(0), null);
	    return;
	}
	Set renamed = new HashSet(aliases);
	for (int j = 0 ; j < q.numPhis(); j++) {
	    // rename aliases as needed
	    Temp src = q.src(j, i);
	    // perform check on original set
	    // in case an alias has multiple
	    // renames
	    if (aliases.contains(src)) {
		renamed.remove(src);
		renamed.add(q.dst(j));
	    }
	}
	// handle rest of edges
	for (i = i+1 ; i < q.arity(); i++) {
	    aliases = get(q.prevEdge(i));
	    if (aliases == null) continue;
	    Set renamed2 = new HashSet(aliases);
	    for(int j = 0; j < q.numPhis(); j++) {
		// rename aliases as needed
		Temp src = q.src(j, i);
		// perform check on original set
		// in case an alias has multiple
		// renames
		if (aliases.contains(src)) {
		    renamed2.remove(src);
		    renamed2.add(q.dst(j));
		}
	    }
	    // since this is a must analysis, 
	    // we use set intersection: keep
	    // only if present in both sets
	    renamed.retainAll(renamed2);
	}
	raiseValue(q.nextEdge(0), renamed);
    }

    public void visit(Quad q) {
	assert q.prevLength() == 1 && q.nextLength() == 1;
	Set aliases = new HashSet(get(q.prevEdge(0)));
	// remove redefined aliases, if any
	aliases.removeAll(q.defC());
	raiseValue(q.nextEdge(0), aliases);
    }
    
    public void visit(SIGMA q) {
	assert q.prevLength() == 1;
	Set aliases = get(q.prevEdge(0));
	// iterate over successor edges
	for(int i = 0; i < q.nextLength(); i++)
	    handleSIGMAEdge(q, new HashSet(aliases), i);
    }
    
    // retreive dataflow fact for CFGEdge e
    protected Set get(CFGEdge e) {
	Set V = (Set) EdgeToTemps.get(e);
	if (DEBUG1) {
	    if (V == null)
		System.out.println("\tget: {*}");
	    else
		System.out.println("\tget: " + V);
	}
	return V;
    }
    
    // update dataflow fact for CFGEdge e
    protected void raiseValue(CFGEdge e, Set raised) {
	if (DEBUG1) {
	    if (raised == null)
		System.out.println("\traiseValue: {*}");
	    else
		System.out.println("\traiseValue: " + raised);
	}
	// add successor to to-do list, if necessary
	if (!EdgeToTemps.containsKey(e)/*never processed before*/|| 
	    !((Set) EdgeToTemps.get(e)).equals(raised)/*changed value*/) {
	    EdgeToTemps.put(e, raised);
	    toDo.push(e.to());
	}
    }
    
    // handleSIGMAEdge handles the index'th outgoing edge of the
    // SIGMA q, given the set of aliases on the incoming edge
    // requires: aliases be a Set of Temps
    // modifies: aliases
    protected void handleSIGMAEdge(SIGMA q, Set aliases, int index) {
	Set insert = new HashSet();
	Set remove = new HashSet();
	// iterate over sigma functions
	for(int j = 0; j < q.numSigmas(); j++) {
	    Temp src = q.src(j);
	    if (aliases.contains(src)) {
		insert.add(q.dst(j, index));
		remove.add(src);
	    }
	}
	aliases.removeAll(remove);
	aliases.addAll(insert);
	raiseValue(q.nextEdge(index), aliases);
    }
}
