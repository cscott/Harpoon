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
import harpoon.Util.Collections.WorkSet;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
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
 * @version $Id: MethodSplitter.java,v 1.5 2002-06-11 20:25:29 cananian Exp $
 */
public abstract class MethodSplitter implements java.io.Serializable {
    /** The <code>ORIGINAL</code> token represents the original pre-split
     *  version of a method. */
    public static final Token ORIGINAL = new Token(null) {
	public Object readResolve() { return ORIGINAL; }
	public String toString() { return "TOKEN<ORIGINAL>"; }
    };
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
     *  @param mutateOriginalBeforeSplit if <code>true</code>, the
     *    <i>mutated</i> version of the original is cloned and given
     *    to <code>mutateHCode</code> as source for a split methods;
     *    otherwise, the <i>unmutated</i> version of the original is
     *    taken as the source for the split method.  If it doesn't
     *    matter, choose <code>true</code>, as this reduces the
     *    memory footprint.
     */
    public MethodSplitter(final HCodeFactory _parent, ClassHierarchy ch,
			  final boolean mutateOriginalBeforeSplit) {
	final Map origcache = //this is cache for unmutated version
	    mutateOriginalBeforeSplit ? null : new HashMap();
        this.hcf = new CachingCodeFactory(new SerializableCodeFactory() {
	    public String getCodeName() {
		return mutateCodeName(_parent.getCodeName());
	    }
	    public void clear(HMethod m) { _parent.clear(m); }
	    public HCode convert(HMethod m) {
		List swpair = (List) split2orig.get(m);
		Token tok = (swpair==null) ? ORIGINAL : (Token) swpair.get(1);
		HCode hc = (tok==ORIGINAL) ? _parent.convert(m) :
		    // get unmutated version from cache...
		    (origcache!=null) ? (HCode)origcache.get( swpair.get(0) ) :
		    hcf.convert( (HMethod)swpair.get(0) );
		// put unmutated version in cache
		if (origcache!=null && tok==ORIGINAL)
		    origcache.put(m, hc);
		try {
		    if (hc!=null) hc = mutateHCode(cloneHCode(hc, m), tok);
		} catch (CloneNotSupportedException ex) {
		    assert false : ("cloning HCode failed: "+ex);
		}
		return hc;
	    }
	}, true/* save cache */) {
	    public void clear(HMethod m) {
		// this version leaks some methods, but it is safe.
		if (select(m, ORIGINAL).equals(m) &&
		    (origcache==null || !origcache.containsKey(m)))
		    parent.clear(m);
		else
		    super.clear(m);
		/*
		//XXX: this doesn't work because we can clear original
		//method before all its mutated children are created.
		// top cache responsible for origcache too.
		super.clear(m);
		if (origcache!=null) origcache.remove(m);
		*/
	    }
	};
	this.ch = ch;
	this.fm = new harpoon.Backend.Maps.CHFinalMap(ch);
    }
    
    /** Maps split methods to <original method, token> pairs. */
    private final Map split2orig = new HashMap();
    /** Maps <original method, token> pairs to created split methods. */
    private final Map versions = new HashMap();

    /** Go from a (possibly already split) method to the version of the
     *  method named by the token <code>which</code>. */
    public final synchronized HMethod select(HMethod source, Token which) {
	assert isValidToken(which) : "token is not valid";
	HMethod orig = split2orig.containsKey(source) ?
	    (HMethod) ((List)split2orig.get(source)).get(0) : source;
	if (which == ORIGINAL) return orig;
	assert which.suffix!=null : "Null token suffixes are not allowed!";
	List swpair = Default.pair(orig, which);
	HMethod splitM = (HMethod) versions.get(swpair);
	if (splitM == null) {
	    HClassMutator hcm = orig.getDeclaringClass().getMutator();
	    assert hcm!=null : "You're using a linker, not a relinker: "+orig+" "+orig.getDeclaringClass().getLinker();
	    String mname = orig.getName()+"$$"+which.suffix;
	    String mdesc = mutateDescriptor(orig, which);
	    try {
		splitM = hcm.addDeclaredMethod(mname, mdesc);
	    } catch (DuplicateMemberException dme) {
		// we can't rename the method, because then inheritance
		// would not work correctly.
		assert false : "Can't create method "+mname+mdesc+" in "+
			    orig.getDeclaringClass()+" because it already "+
			    "exists";
	    }
	    splitM.getMutator().setModifiers(orig.getModifiers());
	    splitM.getMutator().setSynthetic(orig.isSynthetic());
	    // XXX: we currently can't add "renamed" constructors, so
	    // if we're splitting a constructor make it private to
	    // ensure that it is non-virtual.
	    if (orig instanceof HConstructor)
		splitM.getMutator().addModifiers(Modifier.PRIVATE);
	    /* now add this to known versions */
	    versions.put(swpair, splitM);
	    split2orig.put(splitM, swpair);
	    /* now split all subclasses */
	    if (isVirtual(orig)) //only keep splitting if orig is inheritable
		for (Iterator it=ch.overrides(orig).iterator(); it.hasNext(); )
		    // XXX: a little conservative, since select() will then
		    // turn around and split all the child's children.
		    // ch.overrides(orig, ..., true) would give more accurate
		    // results.
		    select((HMethod)it.next(), which);
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
	if (!m.getDeclaringClass().isArray())
	    // XXX arrays are somewhat misleadingly classified as 'final'
	    // even though we provide for inheritance of clone() methods
	    // and such between array classes. =(
	    // (this is the fruit of the strange discrepancies in array
	    //  inheritance.  does A[] derive from Object[] or from Object?)
	    if (fm.isFinal(m)) return false;
	return true;
    }

    /** Returns a <code>HCodeFactory</code> containing representations for
     *  the methods split by the <code>MethodSplitter</code>. */
    public HCodeFactory codeFactory() { return hcf; }

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
    /** Override this method to change the codename which this
     *  <code>MethodMutator</code>'s codefactory reports. */
    protected String mutateCodeName(String codeName) {
	return codeName;
    }
    /** Override this method if you do not want the mutatable HCode to be
     *  a straight clone of the original HCode: for example, if the
     *  input HCodes were <code>QuadSSI</code> and you wanted to
     *  clone them into <code>QuadRSSI</code>s before mutating.
     *  By default, this method returns <code>hc.clone(newmethod)</code>. */
    protected HCodeAndMaps cloneHCode(HCode hc, HMethod newmethod)
	throws CloneNotSupportedException {
	return hc.clone(newmethod);
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
     *  include your new <code>MethodSplitter.Token</code> subclasses.
     *  <p>
     *  A typical subclass of <code>MethodSplitter</code> will include
     *  the following code fragment:<p>
     *  <pre>
     *  public class FooBlah extends <code>MethodSplitter</code> {
     *    /** Token for the foo-blah version of a method. *<b></b>/
     *    public static final Token FOOBLAH = new Token("fooblah") {
     *      /** This ensures that FOOBLAH is a singleton object. *<b></b>/
     *      public Object readResolve() { return FOOBLAH; }
     *    };
     *    /** Checks the token types handled by this 
     *     *  <code>MethodSplitter</code> subclass. *<b></b>/
     *    protected boolean isValidToken(Token which) {
     *      return which==FOOBLAH || super.isValidToken(which);
     *    };
     *  };
     *  </pre>
     */
    protected abstract static class Token implements java.io.Serializable {
	final String suffix;
	/** Create a token, specifying the suggested method suffix. */
	protected Token(String suggestedSuffix) {
	    this.suffix = suggestedSuffix;
	}
	/** This method must be overridden to ensure that <code>Token</code>s
	 *  are still singletons after deserialization.  See the template
	 *  in the class description above. */
	protected abstract Object readResolve();
	/** Returns a human-readable representation of this token. */
	public String toString() { return "TOKEN["+suffix+"]"; }
    }
}
