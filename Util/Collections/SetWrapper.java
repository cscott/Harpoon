// SetWrapper.java, created Fri Jul 14 14:16:01 2000 by pnkfelix
// Copyright (C) 2000 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

/**
 * <code>SetWrapper</code> is analogous to
 * <code>CollectionWrapper</code>, specialized for <code>Set</code>s.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: SetWrapper.java,v 1.1.2.1 2000-07-14 19:26:53 pnkfelix Exp $ */
public class SetWrapper extends CollectionWrapper
    implements java.util.Set {
    
    /** Creates a <code>SetWrapper</code> backed by <code>set</code>. */
    public SetWrapper(java.util.Set set) {
        super(set);
    }
    
}
