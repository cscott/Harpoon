// WriteBarrier.java, created Wed Aug 15 19:21:17 2001 by kkz
// Copyright (C) 2000 Karen Zee <kkz@tmi.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Runtime.PreciseGC;

/**
 * <code>WriteBarrier</code> is an abstract class that provides
 * a dummy write-barrier for generational garbage collection.
 * 
 * @author  Karen Zee <kkz@tmi.lcs.mit.edu>
 * @version $Id: WriteBarrier.java,v 1.1.2.1 2001-08-21 17:40:11 kkz Exp $
 */
public abstract class WriteBarrier {
    
    /** dummy write barrier */
    public static native void storeCheck(Object o);
    
}
