// MRA.java, created Mon Oct  1 16:42:17 2001 by kkz
// Copyright (C) 2000 Karen Zee <kkz@tmi.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PreciseGC;

import harpoon.IR.Quads.Quad;
import harpoon.Util.Tuple;

/**
 * <code>MRA</code> is answers the question "which 
 * <code>Temp<code>s contain the address of the most 
 * recently allocated object at this program point?"
 * 
 * @author  Karen Zee <kkz@tmi.lcs.mit.edu>
 * @version $Id: MRA.java,v 1.1.2.5 2001-11-10 20:43:21 kkz Exp $
 */
public abstract class MRA {

    /** Returns a <code>Tuple</code>. The first element of the
     *  <code>Tuple</code> contains a <code>Map</code> of
     *  <code>Temp</code>s that point to the most recently
     *  allocated object at that program point, to a 
     *  <code>MRAToken</code> that indicates whether the 
     *  <code>Temp</code> points to the receiver object and 
     *  whether the <code>Temp</code> succeeded the receiver 
     *  object as the most-recently allocated. The second 
     *  element of the <code>Tuple</code> is a <code>Set</code> 
     *  of <code>HClass</code>es of which objects may have been 
     *  allocated that are more recent.
     */
    public abstract Tuple mra_before(Quad q);

    /** The <code>MRAToken</code> class represents the nodes
     *  on the lattice for the <code>MRA</code> analysis.
     */
    public static class MRAToken {
	public static MRAToken BOTTOM = new MRAToken("Bottom");
	public static MRAToken RCVR = new MRAToken("Receiver");
	public static MRAToken SUCC = new MRAToken("Successor");
	public static MRAToken TOP = new MRAToken("Top");

	private final String name;
	
	/** Creates an <code>MRAToken</code>. Modifier 
	 *  private since all instances should be declared 
	 *  and allocated here.
	 */
	private MRAToken(String name) {
	    this.name = name;
	}

	/** Returns an <code>MRAToken</code> representing
	 *  the join of <code>this</code> with the
	 *  argument. This operation is symmetric, so
	 *  a.join(b) == b.join(a)
	 */
	public MRAToken join(MRAToken t) {
	    if (this == t) return this;
	    if (this == TOP) return t;
	    return BOTTOM;
	}

	/** Returns a <code>String</code> representation
	 *  of the <code>Token</code>.
	 */
	public String toString() {
	    return "TOKEN<"+name+">";
	}
    }
}

