// DefaultMap.java, created Sat Sep 12 17:30:49 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Maps;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;

/**
 * A <code>DefaultMap</code> returns conservative values for
 * const and exec information: namely that no temp corresponds
 * to a constant and that every node and edge is potentially
 * executable.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DefaultMap.java,v 1.4 2002-09-02 19:23:26 cananian Exp $
 */
// we never refer to type 'HCE' in this class; this should perhaps not
// be parameterized.
public class DefaultMap<HCE extends HCodeElement>
    implements ConstMap<HCE>, ExecMap<HCE> {
    public boolean isConst(HCE hce, Temp t) { return false; }
    public Object constMap(HCE hce, Temp t) {
	throw new Error("Temp "+t+" not constant.");
    }
    public boolean execMap(HCE node) { return true; }
    public boolean execMap(HCodeEdge<HCE> edge) { return true; }
}
