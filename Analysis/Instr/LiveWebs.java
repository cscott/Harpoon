// LiveWebs.java, created Mon Nov  8 23:56:44 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Instr;

import harpoon.IR.Properties.UseDef;

import harpoon.Analysis.DataFlow.LiveVars;
import harpoon.Analysis.BasicBlock;
import harpoon.Util.Collections.SetFactory;
import harpoon.Util.Collections.MultiMap;
import harpoon.Util.Collections.DefaultMultiMap;

import java.util.Set;
import java.util.Iterator;


/**
 * <code>LiveWebs</code>
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: LiveWebs.java,v 1.1.2.1 1999-11-09 06:28:26 pnkfelix Exp $
 */
public class LiveWebs extends LiveVars {
    
    // universe of values for this analysis
    Set webs;

    // maps a reference to the set of Webs containing that reference
    MultiMap refToWebs;

    /** Creates a <code>LiveWebs</code>, using <code>webs</code> as
	its universe of values. 
    */
    public LiveWebs(Set webs, Iterator basicBlocks) {
	super(basicBlocks);
        this.webs = webs;
	this.refToWebs = new DefaultMultiMap();
	Iterator webIter = webs.iterator();
	while(webIter.hasNext()) {
	    Web w = (Web) webIter.next();
	    Iterator instrs = w.refs.iterator();
	    while(instrs.hasNext()) {
		refToWebs.add(instrs.next(), w);
	    }
	}
    }

    protected Set findUniverse(Iterator bbs) {
	return webs;
    }
    
    protected LiveVars.LiveVarInfo makeUseDef(BasicBlock bb, SetFactory sf) {
	LiveVars.LiveVarInfo info = new LiveVars.LiveVarInfo(sf);
	Iterator instrs = bb.listIterator();
	
	while (instrs.hasNext()) {
	    UseDef ref = (UseDef) instrs.next();
	    
	    refToWebs.getValues(ref);
	    
	    

	}

	return null;
    }
}
