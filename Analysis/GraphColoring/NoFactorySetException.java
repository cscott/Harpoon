// NoFactorySetException.java, created Thu Jan 14 20:55:46 1999 by pnkfelix
package harpoon.Analysis.GraphColoring;

/**
 * <code>NoFactorySetException</code>
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: NoFactorySetException.java,v 1.1.2.2 1999-01-19 16:07:59 pnkfelix Exp $
 */

public class NoFactorySetException extends RuntimeException {
    
    /** Creates a <code>NoFactorySetException</code>. */
    public NoFactorySetException() {
        super();
    }
    
    /** Creates a <code>NoFactorySetException</code>. */
    public NoFactorySetException(String s) {
        super(s);
    }    
}
