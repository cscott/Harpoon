// QuadNoSSA.java, created Sat Dec 26 01:42:53 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.Analysis.Maps.TypeMap;
import harpoon.Analysis.Quads.TypeInfo;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.Util.Util;

import java.util.Hashtable;

/**
 * <code>QuadNoSSA</code> is a code view with explicit exception handling.
 * It does not have <code>HANDLER</code> quads, and is not in SSA form.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: QuadNoSSA.java,v 1.1.2.31 2001-11-14 08:12:05 cananian Exp $
 * @see QuadWithTry
 * @see QuadSSI
 */
public class QuadNoSSA extends Code /* which extends HCode */ {
    /** The name of this code view. */
    public static final String codename = "quad-no-ssa";
    /** Type information for this code view.
     *  Only non-null if you pass a non-null <code>TypeMap</code> to the
     *  constructor (or use the <code>codeFactoryWithTypes</code> to 
     *  generate your <code>QuadNoSSA</code>s). */
    public final TypeMap typeMap;

    /** Creates a <code>QuadNoSSA</code> object from a
     *  <code>QuadWithTry</code> object. */
    QuadNoSSA(Code qwt, boolean coalesce) {
	this(qwt, null, coalesce);
    }
    /** Creates a <code>QuadNoSSA</code> object from a
     *  <code>QuadSSI</code> object and a <code>TypeMap</code>. */
    QuadNoSSA(Code qsa, TypeMap tm) {
	this(qsa, tm, false);
    }
    QuadNoSSA(Code qsa) {
	this(qsa, null, false);
    }
    private QuadNoSSA(Code qcode, TypeMap tm, boolean coalesce) {
        super(qcode.getMethod(), null);
	if (qcode.getName().equals(QuadWithTry.codename)) {
	    this.quads = UnHandler.unhandler(this.qf, qcode, coalesce);
	    Peephole.optimize(this.quads);
	    Prune.prune(this);
	    this.typeMap = null;
	} else if (qcode.getName().equals(QuadRSSx.codename)) {
	    RSSxToNoSSA translate = new RSSxToNoSSA(this.qf, qcode);
	    this.quads=translate.getQuads();
	    this.typeMap = null;
	} else if (qcode.getName().equals(QuadSSI.codename)) {
	    ToNoSSA translator = new ToNoSSA(this.qf, qcode, tm);
	    this.quads = translator.getQuads();
	    this.typeMap = (tm==null) ? null : translator.getDerivation();
	    setAllocationInformation(translator.getAllocationInformation());
	} else throw new RuntimeException("can't make quad-no-ssa from "+
					  qcode.getName());
    }

    protected QuadNoSSA(HMethod parent, Quad quads) {
	super(parent, quads);
	this.typeMap = null;
    }
    /** Clone this code representation.  The clone has its own copy of
     *  the quad graph. */
    public HCodeAndMaps clone(HMethod newMethod) {
	return cloneHelper(new QuadNoSSA(newMethod, null));
    }
    /**
     * Return the name of this code view.
     * @return the string <code>"quad-no-ssa"</code>.
     */
    public String getName() { return codename; }

    /** Return a code factory for <code>QuadNoSSA</code>, given a code
     *  factory for <code>QuadWithTry</code> or <code>QuadSSI</code>.
     *  Given a code factory for <code>Bytecode</code>, chain through
     *  <code>QuadWithTry.codeFactory()</code>.  */
    public static HCodeFactory codeFactory(final HCodeFactory hcf) {
	if (hcf.getCodeName().equals(codename)) return hcf;
	if (hcf.getCodeName().equals(QuadWithTry.codename)) {
	    return new harpoon.ClassFile.SerializableCodeFactory() {
		public HCode convert(HMethod m) {
		    HCode c = hcf.convert(m);
		    return (c==null) ? null :
			new QuadNoSSA((Code)c, null,
				      !Boolean.getBoolean
				      ("harpoon.quads.nocoalesce"));
		    // set harpoon.quads.nocoalesce to true to disable
		    // coalescing exception handling.  disabling this
		    // will result in shorter branches, on average.
		}
		public void clear(HMethod m) { hcf.clear(m); }
		public String getCodeName() { return codename; }
	    };
	} else if (hcf.getCodeName().equals(QuadSSI.codename)) {
	    return new harpoon.ClassFile.SerializableCodeFactory() {
		public HCode convert(HMethod m) {
		    HCode c = hcf.convert(m);
		    return (c==null) ? null :
			new QuadNoSSA((Code)c, null, false);
		}
		public void clear(HMethod m) { hcf.clear(m); }
		public String getCodeName() { return codename; }
	    };
	} else if (hcf.getCodeName().equals(QuadRSSx.codename)) {
	    return new harpoon.ClassFile.SerializableCodeFactory() {
		public HCode convert(HMethod m) {
		    HCode c = hcf.convert(m);
		    return (c==null) ? null :
			new QuadNoSSA((Code)c, null, false);
		}
		public void clear(HMethod m) { hcf.clear(m); }
		public String getCodeName() { return codename; }
	    };	    
	} else if (hcf.getCodeName().equals(QuadSSA.codename)) {
	    // implicit chaining.
	    return codeFactory(QuadRSSx.codeFactory(hcf));
	}else if (hcf.getCodeName().equals(harpoon.IR.Bytecode.Code.codename)){
	    // implicit chaining
	    return codeFactory(QuadWithTry.codeFactory(hcf));
	} else throw new Error("don't know how to make " + codename +
			       " from " + hcf.getCodeName());
    }
    /** Return a code factory for <code>QuadNoSSA</code>, given a code
     *  factory for <code>QuadSSI</code>.  The <code>QuadNoSSA</code>s
     *  generated by the code factory will have valid typeMap fields,
     *  courtesy of <code>TypeInfo</code>. */
    public static HCodeFactory codeFactoryWithTypes(final HCodeFactory hcf) {
	if (hcf.getCodeName().equals(QuadSSI.codename)) {
	    return new harpoon.ClassFile.SerializableCodeFactory() {
		public HCode convert(HMethod m) {
		    HCode c = hcf.convert(m);
		    return (c==null) ? null :
			new QuadNoSSA((QuadSSI)c, new TypeInfo((QuadSSI)c));
		}
		public void clear(HMethod m) { hcf.clear(m); }
		public String getCodeName() { return codename; }
	    };
	} else throw new Error("don't know how to make " + codename +
			       " from " + hcf.getCodeName());
    }

    /** Return a code factory for QuadNoSSA, using the default code
     *  factory for QuadWithTry. */
    public static HCodeFactory codeFactory() {
	return codeFactory(QuadWithTry.codeFactory());
    }
}
