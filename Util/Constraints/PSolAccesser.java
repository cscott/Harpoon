// PSolAccesser.java, created Sun Apr  7 14:58:05 2002 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Constraints;

import java.util.Set;

/**
 * <code>PSolAccesser</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: PSolAccesser.java,v 1.1 2002-04-11 04:25:19 salcianu Exp $
 */
public interface PSolAccesser {
    
    /** Returns the set assigned to variable <code>v</code> in the
        underlying partial solution. */
    public Set getSet(Var v);

    /** Returns the new values that were added to the set attached to
	<code>v</code>, after <code>v</code> was processed by the
	fixed-point iterator for the last time.  Distributive
	constraints are expected to use this method to work with
	delta's instead of the full set.  Non-distributive constraints
	can use <code>getSet</code>. */
    public Set getDeltaSet(Var v);

    /** Adds the (possibly) new elements from the set <code>delta</code>
	to the set attached to <code>v</code> in the underlying
	partial solution. Constraints HAVE to be monotonic, that's
	just one there is no way of removing a value once it was added
	to a set. */
    public void updateSet(Var v, Set delta);

    /** Similar to <code>updateSet</code>but adds a single element. */
    public void updateSetWithOneElem(Var v, Object elem);
    
}
