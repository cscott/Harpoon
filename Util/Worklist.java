// Worklist.java, created Sat Sep 12 19:38:31 1998 by cananian
package harpoon.Util;

import harpoon.ClassFile.*;
/**
 * A <code>Worklist</code> is a unique set.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Worklist.java,v 1.1 1998-09-13 23:57:36 cananian Exp $
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
