// Worklist.java, created Sat Sep 12 19:38:31 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import harpoon.ClassFile.*;
/**
 * A <code>Worklist</code> is a unique set.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Worklist.java,v 1.2 1998-10-11 02:37:59 cananian Exp $
 */

public interface Worklist  {
    /** Pushes an item onto the Worklist if it is not already there. */
    public Object push(Object item);
    /** Removes an item from the Worklist and return it. */
    public Object pull();
    /** Determines if the Worklist contains an item. */
    public boolean contains(Object item);
    /** Determines if there are any more items left in the Worklist. */
    public boolean isEmpty();
}
