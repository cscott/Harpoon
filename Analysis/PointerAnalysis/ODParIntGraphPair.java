// ODParIntGraphPair.java, created Mon Mar 27 20:11:14 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

/**
 * <code>ODParIntGraphPair</code> is a simple pair of two
 <code>ODParIntGraph</code>s.

 The existence of this structure is motivated by some details
 of the FLEX intermediate representation.

 In the original algorithm, a single parallel interaction graph
 at the end of each BB was enough. However, as in FLEX CALL is 
 an implicit if, with a branch for normal execution and another
 one for exceptions, there could be two ways of finishing a BB,
 and hence, we need to maintain two graphs.

 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: ODParIntGraphPair.java,v 1.1.2.1 2000-12-11 22:58:49 vivien Exp $
 */
class ODParIntGraphPair {

    // Normally, pig0 is enough; if the BB is terminated with a CALL
    //  pig0 is the graph for normal return from the call, and
    //  pig1 is the graph in case an exception is returned
    ODParIntGraph pig[] = new ODParIntGraph[2];

    /** Creates a <code>ODParIntGraphPair</code>. */
    ODParIntGraphPair(ODParIntGraph _pig0, ODParIntGraph _pig1) {
        this.pig[0] = _pig0;
	this.pig[1] = _pig1;
    }

    /** Checks whether two pairs are equal or not. */
    static boolean identical(ODParIntGraphPair pp1, ODParIntGraphPair pp2){
	if((pp1 == null) || (pp2 == null))
	    return pp1 == pp2;
	return
	    ODParIntGraph.identical(pp1.pig[0], pp2.pig[0]) &&
	    ODParIntGraph.identical(pp1.pig[1], pp2.pig[1]);	
    }

    /** Join another pair of <code>ODParIntGraph</code>s to this one.
	The operation is done pairwise. This method is called only by
	the <code>InterProcPA</code> module. */
    void join(ODParIntGraphPair pp2){
	if(pp2 == null) return;
	partial_join(0, pp2.pig[0]);
	partial_join(1, pp2.pig[1]);
    }

    void join(ODParIntGraphPair pp2, boolean type){
	if(pp2 == null) return;
	partial_join(0, pp2.pig[0]);
	if(type)
	    partial_join(1, pp2.pig[1]);
    }

    private void partial_join(int k, ODParIntGraph pig2) {
	if(pig2 == null)
	    return;

	if(pig[k] == null)
	    pig[k] = (ODParIntGraph) pig2.clone();

	pig[k].join(pig2);
    }

}

