// ResilientNoSSA.java, created Fri Jan 24 15:18:27 2003 by kkz
// Copyright (C) 2000 Karen Zee <kkz@tmi.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;

/**
 * <code>ResilientNoSSA</code> is a code view with resilient exception
 * handling.  It does not have <code>HANDLER</code> quads, and is not
 * in SSA form.
 * 
 * @author  Karen Zee <kkz@tmi.lcs.mit.edu>
 * @version $Id: ResilientNoSSA.java,v 1.1 2003-02-19 20:08:07 kkz Exp $ */
public class ResilientNoSSA extends Code {
        /** The name of this code view. */
    public static final String codename = "resilient-no-ssa";
    
    /** Creates a <code>ResilientNoSSA</code> object from a
     *  <code>QuadWithTry</code> object. */
    ResilientNoSSA(Code qwt, boolean coalesce) {
	super(qwt.getMethod(), null);
	assert qwt.getName().equals(QuadWithTry.codename) :
	    "can't make resilient-no-ssa from "+qwt.getName();
	this.quads = ResilientUnHandler.unhandler(this.qf, qwt, coalesce);
	Peephole.optimize(this.quads);
	Prune.prune(this);
    }
    protected ResilientNoSSA(HMethod parent, Quad quads) {
	super(parent, quads);
    }
    /** Clone this code representation.  The clone has its own copy of
     *  the quad graph. */
    public HCodeAndMaps clone(HMethod newMethod) {
	return cloneHelper(new ResilientNoSSA(newMethod, null));
    }
    /**
     * Return the name of this code view.
     * @return the string <code>"quad-no-ssa"</code>.
     */
    public String getName() { return codename; }

    /** Return a code factory for <code>ResilientNoSSA</code>, given a
     *  code factory for <code>QuadWithTry</code>.  Given a code
     *  factory for <code>Bytecode</code>, chain through
     *  <code>QuadWithTry.codeFactory()</code>.  */
    public static HCodeFactory codeFactory(final HCodeFactory hcf) {
	if (hcf.getCodeName().equals(codename)) return hcf;
	if (hcf.getCodeName().equals(QuadWithTry.codename)) {
	    return new harpoon.ClassFile.SerializableCodeFactory() {
		public HCode convert(HMethod m) {
		    HCode c = hcf.convert(m);
		    return (c==null) ? null :
			new ResilientNoSSA((Code)c,
					   !Boolean.getBoolean
					   ("harpoon.quads.nocoalesce"));
		    // set harpoon.quads.nocoalesce to true to disable
		    // coalescing exception handling.  disabling this
		    // will result in shorter branches, on average.
		}
		public void clear(HMethod m) { hcf.clear(m); }
		public String getCodeName() { return codename; }
	    };
	}else if (hcf.getCodeName().equals(harpoon.IR.Bytecode.Code.codename)){
	    // implicit chaining
	    return codeFactory(QuadWithTry.codeFactory(hcf));
	} else throw new Error("don't know how to make " + codename +
			       " from " + hcf.getCodeName());
    }
    /** Return a code factory for ResilientNoSSA, using the default
     *  code factory for QuadWithTry. */
    public static HCodeFactory codeFactory() {
	return codeFactory(QuadWithTry.codeFactory());
    }
}

