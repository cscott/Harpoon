// ReProtection.java, created Mon Aug 30 16:52:02 1999 by root
// Copyright (C) 1999 root <root@kikashi.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;
import harpoon.IR.Quads.HANDLER;
import java.util.HashSet;
/**
 * <code>ReProtection</code> <blink>please document me if I am public!</blink>
 * 
 * @author  root <root@kikashi.lcs.mit.edu>
 * @version $Id: ReProtection.java,v 1.1.2.1.2.1 1999-09-17 04:38:10 cananian Exp $
 */

public class ReProtection extends HashSet
    implements HANDLER.ProtectedSet {
    ReProtection() { super(); }
    ReProtection(Quad q) {
	super();
	this.insert(q);
    } 
    public boolean isProtected(Quad q) { return contains(q); }
    public void remove(Quad q) { super.remove(q); }
    public void insert(Quad q) { super.add(q); }
    public java.util.Enumeration elements() {
	return new harpoon.Util.IteratorEnumerator( iterator() );
    }
}
