// QuadWithTry.java, created Sat Dec 19 23:55:52 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.Analysis.Quads.DeadCode;
import harpoon.Util.Util;
import harpoon.Util.Tuple;
import harpoon.Analysis.Maps.TypeMap;
import harpoon.Temp.Temp;


import java.util.Map;
/**
 * <code>QuadWithTry</code> is a code view with explicit try-block
 * handlers.  <code>QuadWithTry</code> is not in SSA form.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: QuadWithTry.java,v 1.3 2002-09-01 07:47:20 cananian Exp $
 * @see QuadNoSSA
 * @see QuadSSI
 */
public class QuadWithTry extends Code /* which extends HCode */ {
    /** The name of this code view. */
    public static final String codename = "quad-with-try";
    TypeMap typemap;

    /** Creates a <code>QuadWithTry</code> object from a
     *  <code>harpoon.IR.Bytecode.Code</code> object. */
    QuadWithTry(harpoon.IR.Bytecode.Code bytecode) {
        super(bytecode.getMethod(), null);
	quads = Translate.trans(bytecode, this);
	CleanHandlers.clean(this); // translate is sloppy about handler sets
	Peephole.normalize(quads); // put variables where they belong.
	Peephole.optimize(quads); // move MOVEs more.
	CleanHandlers.clean(this); // be safe and clean again.
	// if we allow far moves, the state which the handlers expect is
	// destroyed.  not sure how to make the optimization handler-safe.
	// maybe don't allow moves past instructions that might throw
	// exceptions?
	this.typemap=null;
    }

    QuadWithTry(harpoon.IR.Quads.QuadSSI quad) {
        super(quad.getMethod(), null);
	ReHandler.QuadMapPair qmp = ReHandler.rehandler(this.qf, quad);
	quads = qmp.quad;
	final Map map=qmp.map;
	Peephole.normalize(quads,map);
	Peephole.optimize(quads, map);
	//ReHandler.clean doesn't need map, it just eats quads...
	ReHandler.clean(this);
	Pattern.patternMatch(this,map);
	
	this.typemap=new TypeMap() {
	    public HClass typeMap(HCodeElement hc, Temp t) {
		return (HClass) map.get(new Tuple(new Object[]{hc,t}));
	    }
	};
    }

    protected QuadWithTry(HMethod parent, Quad quads) {
	super(parent, quads);
	this.typemap=null;
    }
    /** Clone this code representation.  The clone has its own copy of
     *  the quad graph. */
    public HCodeAndMaps<Quad> clone(HMethod newMethod) {
	return cloneHelper(new QuadWithTry(newMethod, null));
    }
    /**
     * Return the name of this code view.
     * @return the string <code>"quad-with-try"</code>.
     */
    public String getName() { return codename; }

    /** Return a code factory for <code>QuadWithTry</code>, given a
     *  code factory for <code>Bytecode</code> or <code>QuadNoSSA</code>.
     *  Given a code factory for <code>QuadSSI</code>, chain through 
     *  <code>QuadNoSSA.codeFactory()</code>. */
    public static HCodeFactory codeFactory(final HCodeFactory hcf) {
	if (hcf.getCodeName().equals(codename)) return hcf;
	if (hcf.getCodeName().equals(harpoon.IR.Bytecode.Code.codename)) {
	    return new CloneSynthesizer // synthesize codes for array.clone()
		(new harpoon.ClassFile.SerializableCodeFactory() {
		public HCode convert(HMethod m) {
		    HCode c = hcf.convert(m);
		    return (c==null) ? null :
			new QuadWithTry((harpoon.IR.Bytecode.Code)c);
		}
		public void clear(HMethod m) { hcf.clear(m); }
		public String getCodeName() { return codename; }
	    });
	} else if (hcf.getCodeName().equals(harpoon.IR.Quads.QuadSSI.codename)) {
	    return new harpoon.ClassFile.SerializableCodeFactory() {
		public HCode convert(HMethod m) {
		    HCode c = hcf.convert(m);
		    return (c==null) ? null :
			new QuadWithTry((harpoon.IR.Quads.QuadSSI)c);
		}
		public void clear(HMethod m) { hcf.clear(m); }
		public String getCodeName() { return codename; }
	    };
	} else if (hcf.getCodeName().equals(harpoon.IR.Quads.QuadNoSSA.codename)) {
	    return codeFactory(harpoon.IR.Quads.QuadSSI.codeFactory(hcf));
	} else throw new Error("don't know how to make " + codename +
			       " from " + hcf.getCodeName());
    }
    /** Return a code factory for QuadWithTry, using the default
     *  code factory for Bytecode. */
    public static HCodeFactory codeFactory() {
	return codeFactory(harpoon.IR.Bytecode.Code.codeFactory());
    }
    /** Returns a TypeMap if there is one, or null otherwise**/
    public TypeMap typeMap() {
	return typemap;
    }
}
