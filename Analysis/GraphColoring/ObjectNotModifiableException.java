// ObjectNotModifiableException.java, created Thu Jan 14 16:15:42 1999 by pnkfelix
package harpoon.Analysis.GraphColoring;

import harpoon.ClassFile.*;
/**
 * <code>ObjectNotModifiableException</code>
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: ObjectNotModifiableException.java,v 1.1.2.1 1999-01-14 23:16:29 pnkfelix Exp $
 */

public class ObjectNotModifiableException extends Exception {
    
    /** Creates a <code>ObjectNotModifiableException</code>. */
    public ObjectNotModifiableException() {
        super();
    }
    
    /** Creates a <code>ObjectNotModifiableException</code>. */
    public ObjectNotModifiableException(String s) {
        super(s);
    }
}
