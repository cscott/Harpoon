// DefaultMap.java, created Sat Sep 12 17:30:49 1998 by cananian
package harpoon.Analysis.Maps;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
/**
 * A <code>DefaultMap</code> returns conservative values for
 * const and exec information: namely that no temp corresponds
 * to a constant and that every node and edge is potentially
 * executable.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DefaultMap.java,v 1.1 1998-09-13 23:57:13 cananian Exp $
 */

public class DefaultMap implements ConstMap, ExecMap {
    public boolean isConst(HCode hc, Temp t) { return false; }
    public Object constMap(HCode hc, Temp t) {
	throw new Error("Temp "+t+" not constant.");
    }
    public boolean execMap(HCode hc, HCodeElement node) { return true; }
    public boolean execMap(HCode hc, HCodeEdge edge) { return true; }
}
