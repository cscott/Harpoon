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
 * @version $Id: DefaultMap.java,v 1.3 2002-02-25 20:58:10 cananian Exp $
 */
public class DefaultMap implements ConstMap, ExecMap {
    public boolean isConst(HCodeElement hce, Temp t) { return false; }
    public Object constMap(HCodeElement hce, Temp t) {
	throw new Error("Temp "+t+" not constant.");
    }
    public boolean execMap(HCodeElement node) { return true; }
    public boolean execMap(HCodeEdge edge) { return true; }
}
