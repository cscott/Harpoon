// SSIStats.java, created Mon Aug 30 16:56:03 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.Quad;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
/**
 * <code>SSIStats</code> is a class to provide counts of uses, definitions
 * and variables in plain, SSA, and SSI forms.  Primarily, its purpose
 * is to generate numbers for papers which justify claims of algorithmic
 * linearity.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SSIStats.java,v 1.2 2002-02-25 20:59:23 cananian Exp $
 */
public class SSIStats {
    /** How many variable definitions are in this code. */
    public final int uses;
    /** How many variable uses are in this code. */
    public final int defs;
    /** How many variables are in this code. */
    public final int vars;
    /** How many statements are in this code. */
    public final int length;

    /** Creates a <code>SSIStats</code>. */
    public SSIStats(harpoon.IR.Quads.Code c) {
	Set v=new HashSet();
	int u=0,d=0,n=0;
	for (Iterator it=c.getElementsI(); it.hasNext(); n++) {
	    Quad q=(Quad)it.next();
	    Temp[] ut = q.use(), dt=q.def();
	    for (int i=0; i<ut.length; i++)
		u++;
	    for (int i=0; i<dt.length; i++)
		d++;
	    for (int i=0; i<dt.length; i++)
		v.add(dt[i]);
	}
	this.uses = u;
	this.defs = u;
	this.vars = v.size();
	this.length = n;
    }

    public static HCodeFactory codeFactory(final HCodeFactory hcf) {
	return new HCodeFactory() {
	    final Set counted = new HashSet();
	    public String getCodeName() { return hcf.getCodeName(); }
	    public void clear(HMethod m) { hcf.clear(m); }
	    public HCode convert(HMethod m) {
		HCode c = hcf.convert(m);
		if (!counted.contains(m)) {
		    counted.add(m);
		    SSIStats ss = new SSIStats((harpoon.IR.Quads.Code)c);
		    if (harpoon.Main.Options.statWriter!=null)
			harpoon.Main.Options.statWriter.println
			    ("PHISIG "+ss.length+" "+
			     ss.uses+" "+ss.defs+" "+ss.vars);
		}
		return c;
	    }
	};
    }
}
