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
 * another.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: MethodSplitter.java,v 1.1.2.1 2000-10-05 20:37:38 cananian Exp $
 */
public class MethodSplitter {
    public static final Token ORIGINAL = new Token(null);
    
    /** Creates a <code>MethodSplitter</code>. */
    public MethodSplitter() {
        
    }
    
    /** Maps split methods to the original method they were derived from. */
    private final Map split2orig = new HashMap();
    /** Maps <original method, token> pairs to created split methods. */
    private final Map versions = new HashMap();

    /** Go from a (possibly already split) method to the version of the
     *  method identified by the token <code>which</code>. */
    public final synchronized HMethod select(HMethod source, Token which) {
	Util.assert(isValidToken(which), "token is not valid");
	HMethod orig = split2orig.containsKey(source) ?
	    (HMethod) split2orig.get(source) : source;
	if (which == ORIGINAL) return orig;
	HMethod splitM = (HMethod) versions.get(Default.pair(source, which));
	if (splitM == null) {
	    HClassMutator hcm = orig.getDeclaringClass().getMutator();
	    Util.assert(hcm!=null, "You're using a linker, not a relinker.");
	    String mname = orig.getName()+"$$"+which.suffix;
	    String mdesc = mutateDescriptor(orig, which);
	    for (int cnt=1; splitM==null; cnt++) {
		try {
		    splitM = hcm.addDeclaredMethod
			((cnt<2) ? mname : mname + cnt, mdesc);
		} catch (DuplicateMemberException dme) { /* try try again */ }
	    }
	    /* now add this to known versions */
	    versions.put(Default.pair(source, which), splitM);
	    split2orig.put(splitM, orig);
	}
	return splitM;
    }

    /** Override this method if you want to create mutated methods
     *  with descriptors differing from that of the original method.
     */
    protected String mutateDescriptor(HMethod hm, Token which) {
	return hm.getDescriptor();
    }
    /** Check the validity of a given <code>MethodSplitter.Token</code>.
     *  Override if (when) your subclass defines new tokens. */
    protected boolean isValidToken(Token which) {
	return which==ORIGINAL;
    }

    /** Subclasses of <code>MethodSplitter</code> specify "versions"
     *  of the underlying method which may be created by creating
     *  static instances of this <code>MethodSplitter.Token</code>
     *  class.  The argument to the constructor specifies a default
     *  suffix for the newly-split method's name. */
    protected static class Token {
	final String suffix;
	protected Token(String suggestedSuffix) {
	    this.suffix = suggestedSuffix;
	}
    }
}
