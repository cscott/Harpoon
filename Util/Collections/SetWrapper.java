// SetWrapper.java, created Fri Jul 14 14:16:01 2000 by pnkfelix
// Copyright (C) 2000 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

/**
 * <code>SetWrapper</code> is analogous to
 * <code>CollectionWrapper</code>, specialized for <code>Set</code>s.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: SetWrapper.java,v 1.2.2.1 2002-02-27 22:24:15 cananian Exp $ */
public class SetWrapper<E> extends CollectionWrapper<E>
    implements java.util.Set<E> {
    
    /** Creates a <code>SetWrapper</code> backed by <code>set</code>. */
    public SetWrapper(java.util.Set<E> set) {
        super(set);
    }
    
}
