// ObjectNotModifiableException.java, created Thu Jan 14 16:15:42 1999 by pnkfelix
package harpoon.Analysis.GraphColoring;

/**
 * <code>ObjectNotModifiableException</code>
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: ObjectNotModifiableException.java,v 1.1.2.2 1999-01-19 16:08:00 pnkfelix Exp $
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
