// AllocationHoisting.java, created Fri Oct 19 11:32:41 2001 by kkz
// Copyright (C) 2000 Karen Zee <kkz@tmi.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PreciseGC;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.ReachingDefs;
import harpoon.Analysis.ReachingDefsImpl;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.Linker;
import harpoon.IR.Quads.ASET;
import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadKind;
import harpoon.IR.Quads.SET;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * <code>AllocationHoisting</code>
 * 
 * @author  Karen Zee <kkz@tmi.lcs.mit.edu>
 * @version $Id: AllocationHoisting.java,v 1.1.2.1 2001-10-21 21:39:47 kkz Exp $
 */
public class AllocationHoisting extends 
    harpoon.Analysis.Transformation.MethodSplitter {

    private final MRAFactory mraf;
    
    /** Token for the hoisted version of an initializer. */
    public static final Token HOISTED = new Token("allochoist") {
	public Object readResolve() { return HOISTED; }
    };
    /** Creates an <code>AllocationHoisting</code>.
     *  @param parent The input code factory.
     *  @param ch A class hierarchy for the application.
     */
    public AllocationHoisting(HCodeFactory parent, ClassHierarchy ch, 
			      Linker l, String rName) {
        super(parent, ch, true/*doesn't matter*/);
	this.mraf = new MRAFactory(ch, parent, l, rName);
    }
    /** Hoists allocation out of the split method. */
    protected HCode mutateHCode(HCodeAndMaps input, Token which) {
	Code c = (Code) input.hcode();
	if (which == ORIGINAL && optimizable(c))
	    System.out.println(c.getMethod()+" may be optimizable.\n");
	return input.hcode();
    }
    /** Checks whether the given <code>Code</code> can benefit
     *  from the transformation.
     */
    private boolean optimizable(Code c) {
	// only worry about safe initializers
	if (!mraf.isSafeInitializer(c.getMethod())) return false;
	// run a bunch of analyses
	MRA mra = mraf.mra(c);
	ReachingDefs rd = new ReachingDefsImpl(c);
	for (Iterator it = c.getElementsI(); it.hasNext(); ) {
	    Quad q = (Quad) it.next();
	    Temp src, dst;
	    if (q.kind() == QuadKind.ASET && !((ASET)q).type().isPrimitive()) {
		dst = ((ASET)q).objectref();
		src = ((ASET)q).src();
	    } else if (q.kind() == QuadKind.SET && !((SET)q).isStatic() &&
		       !((SET)q).field().getType().isPrimitive()) {
		dst = ((SET)q).objectref();
		src = ((SET)q).src();
	    } else {
		continue;
	    }
	    // first check whether the src is mra
	    if (!((Map) mra.mra_before(q).proj(0)).containsKey(src))
		return false;
	    System.out.println(q);
	    System.out.println((Map) mra.mra_before(q).proj(0));
	    // first check where the dst was def'd
	    Set ddefs = rd.reachingDefs(q, dst);
	    System.out.println("DDEFs: "+ddefs);
	    // next check src
	    Set sdefs = rd.reachingDefs(q, src);
	    System.out.println("SDEFs: "+sdefs);
	    // see if the def overwrote the previous mra
	    for (Iterator defs = sdefs.iterator(); defs.hasNext(); ) {
		System.out.println
		    ((Map)mra.mra_before((Quad)defs.next()).proj(0));
	    }
	    return true;
	    /*
	      boolean optimizable = true;
	    for (Iterator defs = sdefs.iterator(); defs.hasNext(); ) {
		Quad d = (Quad) defs.next();
		if (d.kind() != QuadKind.ANEW && 
		    d.kind() != QuadKind.NEW) {
		    optimizable = false;
		    break;
		    }
		}
		return optimizable;
	    */
	}
	return false;
    }

    /** Check the validity of a given <code>MethodSplitter.Token</code>.
     */
    protected boolean isValidToken(Token which) {
	return which==HOISTED || super.isValidToken(which);
    }
    
    
}
