// PredicateWrapper.java, created Thu Feb 24 15:56:13 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

/**
 * <code>PredicateWrapper</code> wraps a predicate on an <code>Object</code>.
 This is supposed to allow us to send predicate functions as arguments to
 high-level functions.
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: PredicateWrapper.java,v 1.1.2.2 2001-06-17 22:36:00 cananian Exp $
 */
public interface PredicateWrapper {
    public boolean check(Object obj);
}
