// CleanHandlers.java, created Wed Nov 15 23:31:06 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
/**
 * <code>CleanHandlers</code> gets rid of unreachable
 * <code>Quad</code>s from the handlers of a <code>Code</code>.  It is
 * here because <code>Translate</code> is sloppy about removing
 * <code>Quad</code>s (especially <code>PHI</code>s) from the handler
 * set when they're unreachable or replaced.  But we don't want to
 * risk touching <code>Translate</code> after all this time.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CleanHandlers.java,v 1.1.2.2 2001-06-17 22:33:35 cananian Exp $
 */
class CleanHandlers {
    static void clean(Code code) {
	Set reachable = new HashSet(code.getElementsL());
	HEADER header = (HEADER) code.getRootElement();
	METHOD method = header.method();
	for (int i=1; i<method.nextLength(); i++) {
	    HANDLER handler = (HANDLER) method.next(i);
	    for (Iterator it=handler.protectedSet.iterator(); it.hasNext(); )
		if (!reachable.contains(it.next()))
		    it.remove();
	}
    }
}
