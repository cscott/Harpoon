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
 * @version $Id: GenericContextFactory.java,v 1.1 2002-02-26 10:08:03 cananian Exp $
 * @see Context
 */
public class GenericContextFactory {
    private final int CONTEXT_SENSITIVITY;
    private final Map cache;
    private final Context root;
    
    /** Creates a <code>GenericContextFactory</code>. */
    public GenericContextFactory(int context_sensitivity, boolean use_cache) {
	this.CONTEXT_SENSITIVITY = context_sensitivity;
	this.cache = use_cache ? new HashMap() : null;
	this.root = new ContextImpl();
	if (this.cache!=null)
	    cache.put(this.root, this.root);
    }
    public Context makeEmptyContext() { return root; }
    
    private class ContextImpl extends Context {
	final Object[] items;
	List cached_list = null;
	ContextImpl() { this(new Object[0]); }
	ContextImpl(Object[] items) { this.items = items; }
	public Context addElement(Object o) {
	    // truncate context at CONTEXT_SENSITIVITY items.
	    Object[] nitems =
		new Object[Math.min(CONTEXT_SENSITIVITY, items.length+1)];
	    if (nitems.length>0)
		nitems[nitems.length-1] = o;
	    if (nitems.length>1)
		System.arraycopy(items, 1, nitems, 0, nitems.length-1);
	    Context nc = new ContextImpl(nitems);
	    // maybe cache.
	    if (cache!=null)
		if (!cache.containsKey(nc))
		    cache.put(nc, nc);
		else
		    nc = (Context) cache.get(nc);
	    // ta-da!
	    return nc;
	}
	public List asList() {
	    List l = cached_list;
	    if (l==null) {
		l = Collections.unmodifiableList(Arrays.asList(items));
		if (cache!=null)
		    cached_list = l;
	    }
	    return l;
	}
    }
}
