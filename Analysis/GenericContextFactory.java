// GenericContextFactory.java, created Tue Feb 26 03:57:04 2002 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * A <code>GenericContextFactory</code> can create <code>Context</code>
 * objects for any desired level of context-sensitivity.
 * The context-sensitivity and caching behavior is specified in the
 * <code>GenericContextFactory</code>'s constructor.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: GenericContextFactory.java,v 1.3 2003-06-17 16:44:56 cananian Exp $
 * @see Context
 */
public class GenericContextFactory<E> {
    private final int CONTEXT_SENSITIVITY;
    private final Map<Context<E>,Context<E>> cache;
    private final Context<E> root;
    
    /** Creates a <code>GenericContextFactory</code>. */
    public GenericContextFactory(int context_sensitivity, boolean use_cache) {
	this.CONTEXT_SENSITIVITY = context_sensitivity;
	this.cache = use_cache ? new HashMap<Context<E>,Context<E>>() : null;
	this.root = new ContextImpl();
	if (this.cache!=null)
	    cache.put(this.root, this.root);
    }
    // XXX should return a bivariant context; adding something to it
    // will make the context invariant.
    public Context<E> makeEmptyContext() { return root; }
    
    private class ContextImpl extends Context<E> {
	final E[] items;
	List<E> cached_list = null;
	ContextImpl() { this(new E[0]); }
	ContextImpl(E[] items) { this.items = items; }
	public Context<E> addElement(E o) {
	    // truncate context at CONTEXT_SENSITIVITY items.
	    E[] nitems =
		new E[Math.min(CONTEXT_SENSITIVITY, items.length+1)];
	    if (nitems.length>0)
		nitems[nitems.length-1] = o;
	    if (nitems.length>1)
		System.arraycopy(items, items.length-(nitems.length-1),
				 nitems, 0, nitems.length-1);
	    Context<E> nc = new ContextImpl(nitems);
	    // maybe cache.
	    if (cache!=null)
		if (!cache.containsKey(nc))
		    cache.put(nc, nc);
		else
		    nc = cache.get(nc);
	    // ta-da!
	    return nc;
	}
	public List<E> asList() {
	    List<E> l = cached_list;
	    if (l==null) {
		l = Collections.unmodifiableList(Arrays.asList(items));
		if (cache!=null)
		    cached_list = l;
	    }
	    return l;
	}
    }
}
