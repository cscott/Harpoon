// ToSSI.java, created Wed Mar 31 18:23:34 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.Analysis.SESE;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Util.PersistentSet;

import java.util.Iterator;
import java.util.Map;
/**
 * The <code>ToSSI</code> class places phi and sigma functions and
 * renames variables to convert to SSI form.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ToSSI.java,v 1.1.2.2 1999-06-11 11:31:31 cananian Exp $
 */
public class ToSSI  {
    
    /** Creates a <code>ToSSI</code>. */
    public ToSSI() {
    }

    private void place(HCode hc, Map info) {
	// REWRITE TO PROCESS REGIONS TOP-DOWN
	// WHICH MEANS WE NEED A FUNCTION TO ITERATE OVER ALL THE
	// HCODEELEMENTS IN A GIVEN REGION.

	SESE sese = new SESE(hc, true);
	// make sets for all regions
	for (Iterator it=sese.topDown(); it.hasNext(); )
	    info.put(it.next(), new PersistentSet());

	for (Iterator it=hc.getElementsI(); it.hasNext(); ) {
	    HCodeElement hce = (HCodeElement) it.next();
	    SESE.Region r = (SESE.Region) sese.smallestSESE.get(hce);
	    PersistentSet ps = (PersistentSet) info.get(r);
	    Temp[] use = ((harpoon.IR.Properties.UseDef) hce).use();
	    Temp[] def = ((harpoon.IR.Properties.UseDef) hce).def();
	    
	    for (int i=0; i<use.length; i++) ps = ps.add(use[i]);
	    for (int i=0; i<def.length; i++) ps = ps.add(def[i]);
	    
	    info.put(r, ps);
	}
	// smear down.
	for (Iterator it=sese.topDown(); it.hasNext(); ) {
	    SESE.Region r = (SESE.Region) it.next();
	    PersistentSet ps = (PersistentSet) info.get(r);
	    for (Iterator itC=r.children().iterator(); itC.hasNext(); ) {
		SESE.Region rC = (SESE.Region) itC.next();
		PersistentSet psC = (PersistentSet) info.get(rC);
		for (Iterator itT=psC.asSet().iterator(); itT.hasNext(); )
		    ps = ps.add( (Temp) itT.next());
	    }
	    info.put(r, ps);
	}
    }
    private void rename() {
    }
    private void prune() {
    }
}
