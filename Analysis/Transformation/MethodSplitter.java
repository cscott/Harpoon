// MethodSplitter.java, created Thu Oct  5 15:48:13 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Transformation;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Backend.Maps.FinalMap;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.DuplicateMemberException;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HClassMutator;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HConstructor;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.SerializableCodeFactory;
import harpoon.Util.Default;
import harpoon.Util.Util;
import harpoon.Util.WorkSet;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * <code>MethodSplitter</code> makes it easier to implement
 * transformations which specialize methods for one purpose or
 * another.  It is meant to be subclassed.  In your subclass,
 * you will likely want to create a few static fields of type
 * <code>MethodSplitter.Token</code> to name your specialized
 * versions, override the <code>isValidToken()</code> method
 * to include your new tokens, and override
 * <code>mutateDescriptor()</code> and/or <code>mutateHCode()</code>
 * to effect the specialization.
 * <p>
 * Note that if you mutate the <code>ORIGINAL</code> version of
 * a method, all split versions will inherit the mutation.
 * Be careful not to introduce cycles because of this ordering.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: MethodSplitter.java,v 1.1.2.11 2000-10-20 22:55:43 cananian Exp $
 */
public abstract class MethodSplitter {
    /** The <code>ORIGINAL</code> token represents the original pre-split
     *  version of a method. */
    public static final Token ORIGINAL = new Token(null);
    /** This is the code factory which contains the representations of the
     *  new split methods. */
    private final CachingCodeFactory hcf;
    /** This is a class hierarchy, needed to properly split virtual methods.
     *  (Virtual methods have to be also split in all subclasses, in order
     *  for dynamic dispatch to work correctly.) */
    private final ClassHierarchy ch;
    /** This is the FinalMap used in the implementation of isVirtual(). */
    private final FinalMap fm;

    /** Creates a <code>MethodSplitter</code>, based on the method
     *  representations in the <code>parent</code> <code>HCodeFactory</code>.
     */
    public MethodSplitter(final HCodeFactory parent, ClassHierarchy ch) {
        this.hcf = new CachingCodeFactory(new SerializableCodeFactory() {
	    public String getCodeName() { return parent.getCodeName(); }
	    public void clear(HMethod m) { parent.clear(m); }
	    public HCode convert(HMethod m) {
		List swpair = (List) split2orig.get(m);
		Token tok = (swpair==null) ? ORIGINAL : (Token) swpair.get(1);
		HCode hc = (tok==ORIGINAL) ? parent.convert(m) :
		    convert( (HMethod)swpair.get(0) );
		try {
		    if (hc!=null) hc = mutateHCode(hc.clone(m), tok);
		} catch (CloneNotSupportedException ex) {
		    Util.assert(false, "cloning HCode failed: "+ex);
		}
		return hc;
	    };
	}, true/* save cache */);
	this.ch = ch;
	this.fm = new harpoon.Backend.Maps.DefaultFinalMap();
    }
    
    /** Maps split methods to <original method, token> pairs. */
    private final Map split2orig = new HashMap();
    /** Maps <original method, token> pairs to created split methods. */
    private final Map versions = new HashMap();

    /** Go from a (possibly already split) method to the version of the
     *  method named by the token <code>which</code>. */
    public final synchronized HMethod select(HMethod source, Token which) {
	Util.assert(isValidToken(which), "token is not valid");
	HMethod orig = split2orig.containsKey(source) ?
	    (HMethod) ((List)split2orig.get(source)).get(0) : source;
	if (which == ORIGINAL) return orig;
	Util.assert(which.suffix!=null,"Null token suffixes are not allowed!");
	List swpair = Default.pair(source, which);
	HMethod splitM = (HMethod) versions.get(swpair);
	if (splitM == null) {
	    HClassMutator hcm = orig.getDeclaringClass().getMutator();
	    Util.assert(hcm!=null, "You're using a linker, not a relinker: "+orig+" "+orig.getDeclaringClass().getLinker());
	    String mname = orig.getName()+"$$"+which.suffix;
	    String mdesc = mutateDescriptor(orig, which);
	    try {
		splitM = hcm.addDeclaredMethod(mname, mdesc);
	    } catch (DuplicateMemberException dme) {
		// we can't rename the method, because then inheritance
		// would not work correctly.
		Util.assert(false, "Can't create method "+mname+mdesc+" in "+
			    orig.getDeclaringClass()+" because it already "+
			    "exists");
	    }
	    splitM.getMutator().setModifiers(orig.getModifiers());
	    splitM.getMutator().setSynthetic(orig.isSynthetic());
	    /* now add this to known versions */
	    versions.put(swpair, splitM);
	    split2orig.put(splitM, swpair);
	    /* now split all subclasses */
	    if (isVirtual(orig)) { //only keep splitting if orig is inheritable
		WorkSet cW= new WorkSet(ch.children(orig.getDeclaringClass()));
		while (!cW.isEmpty()) {
		    // pull a subclass off the list.
		    HClass c = (HClass) cW.pop();
		    // now check for decl'd methods with the same name as orig.
		    try {
			HMethod hm = c.getMethod(orig.getName(),
						 orig.getDescriptor());
			select(hm, which); // split hm and all children.
		    } catch (NoSuchMethodError nsme) {
			// keep looking for subclasses that declare method:
			// add all subclasses of this one to the worklist.
			cW.addAll(ch.children(c));
		    }
		}
	    }
	    /* done */
	}
	return splitM;
    }
    /** Utility method to determine whether a method is inheritable (and
     *  thus it's children should be split whenever it is). */
    protected boolean isVirtual(HMethod m) {
	if (m.isStatic()) return false;
	if (Modifier.isPrivate(m.getModifiers())) return false;
	if (m instanceof HConstructor) return false;
	if (fm.isFinal(m)) return false;
	return true;
    }

    /** Returns a <code>HCodeFactory</code> containing representations for
     *  the methods split by the <code>MethodSplitter</code>. */
    public final HCodeFactory codeFactory() { return hcf; }

    /** Override this method if you want to create mutated methods
     *  with descriptors differing from that of the original method.
     */
    protected String mutateDescriptor(HMethod hm, Token which) {
	return hm.getDescriptor();
    }
    /** Override this method to effect transformations on split
     *  methods. */
    protected HCode mutateHCode(HCodeAndMaps input, Token which) {
	return input.hcode();
    }

    /** Check the validity of a given <code>MethodSplitter.Token</code>.
     *  Override if (when) your subclass defines new tokens. */
    protected boolean isValidToken(Token which) {
	return which==ORIGINAL;
    }

    /** Subclasses of <code>MethodSplitter</code> refer to "versions"
     *  of the underlying method which may be named by creating
     *  static instances of this <code>MethodSplitter.Token</code>
     *  class.  The argument to the constructor specifies a default
     *  suffix for the newly-split method's name.  Don't forget
     *  to extend <code>MethodSplitter.isValidToken()</code> to
     *  include your new <code>MethodSplitter.Token</code> subclasses. */
    protected static class Token {
	final String suffix;
	protected Token(String suggestedSuffix) {
	    this.suffix = suggestedSuffix;
	}
    }
}
