// DynamicSyncImpl.java, created Wed Jul  9 20:21:33 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Runtime;

/**
 * <code>DynamicSyncImpl</code> is a helper class to implement
 * dynamic synchronization removal.  It just contains a stub
 * method which will be invoked at runtime to determine whether
 * synchronization on a specific object will be skipped or not.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DynamicSyncImpl.java,v 1.2 2003-07-10 02:49:03 cananian Exp $
 */
public abstract class DynamicSyncImpl {
    /** Returns true iff synchronization on the given object must
     *  be performed, or false if it can be skipped. */
    public static native boolean isNoSync(Object o);
}
