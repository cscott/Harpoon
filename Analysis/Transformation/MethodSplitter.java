// MethodSplitter.java, created Thu Oct  5 15:48:13 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Transformation;

import harpoon.ClassFile.*;
import harpoon.Util.*;

import java.util.*;
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
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: MethodSplitter.java,v 1.1.2.4.2.1 2000-10-06 23:54:33 cananian Exp $
 */
public class MethodSplitter implements java.io.Serializable {
    /** The <code>ORIGINAL</code> token represents the original pre-split
     *  version of a method. */
    public static final Token ORIGINAL = new Token(null);
    /** This is the code factory which contains the representations of the
     *  original (unsplit) methods. */
    private final HCodeFactory parent;
    /** Creates a <code>MethodSplitter</code>, based on the method
     *  representations in the <code>parent</code> <code>HCodeFactory</code>.
     */
    public MethodSplitter(HCodeFactory parent) {
	// make parent a caching code factory, because we're going to
	// call convert(m) on a method m once *for each possible token*.
        this.parent = new CachingCodeFactory(parent);
    }
    
    /** Maps split methods to <original method, token> pairs. */
    private final Map split2orig = new HashMap();
    /** Maps <original method, token> pairs to created split methods. */
    private final Map versions = new HashMap();

    /** Go from a (possibly already split) method to the version of the
     *  method named by the token <code>which</code>. */
    public final synchronized HMethod select(HMethod source, Token which) {
	Util.assert(isValidToken(which), "token is not valid");
	HMethod orig = !split2orig.containsKey(source) ? source :
	    (HMethod) ((List) split2orig.get(source)).get(0);
	if (which == ORIGINAL) return orig;
	List swpair = Default.pair(source, which);
	HMethod splitM = (HMethod) versions.get(swpair);
	if (splitM == null) {
	    HClassMutator hcm = orig.getDeclaringClass().getMutator();
	    Util.assert(hcm!=null, "You're using a linker, not a relinker.");
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
	    /* now add this to known versions */
	    versions.put(swpair, splitM);
	    split2orig.put(splitM, swpair);
	    /* HCodes will now be created demand-driven */
	}
	return splitM;
    }
    /** Returns a <code>HCodeFactory</code> containing representations for
     *  the methods split by the <code>MethodSplitter</code>. */
    public final HCodeFactory codeFactory() { return hcf; }
    /** This is the code factory which contains the representations of the
     *  new split methods. */
    private final HCodeFactory hcf = new SerializableCodeFactory() {
	private final Map cache = new HashMap();
	public String getCodeName() { return parent.getCodeName(); }
	public HCode convert(HMethod m) {
	    if (cache.containsKey(m)) return (HCode) cache.get(m);
	    List swpair = (List) split2orig.get(m);
	    HCode hc = parent.convert(swpair==null?m:(HMethod)swpair.get(0));
	    if (hc!=null)
		try {
		    hc = mutateHCode(hc.clone(m), (Token) swpair.get(1));
		} catch (CloneNotSupportedException ex) {
		    Util.assert(false, "cloning HCode failed: "+ex);
		}
	    cache.put(m, hc);
	    return hc;
	}
	public void clear(HMethod m) {
	    cache.remove(m);
	    /*parent.clear(m);*/ // XXX can't clear original until all splits
	                         // have been made & we can't tell when that
	                         // might be, because creation is demand-driven
	}
    };

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
    protected static class Token implements java.io.Serializable {
	final String suffix;
	protected Token(String suggestedSuffix) {
	    this.suffix = suggestedSuffix;
	}
    }
}
