// StaticValue.java, created Sat May  3 19:28:20 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Companions;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import net.cscott.jutil.Default;
/**
 * A <code>StaticValue</code> is a pair of variable and statement,
 * which represents all the variables possible values at the
 * given program point.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: StaticValue.java,v 1.2 2004-02-08 01:50:55 cananian Exp $
 */
public class StaticValue<HCE extends HCodeElement>
    extends Default.PairList<Temp,HCE>  {
    
    /** Creates a <code>StaticValue</code>. */
    public <HCE2 extends HCE> StaticValue(Temp t, HCE2 el) { super(t,el); }
}
