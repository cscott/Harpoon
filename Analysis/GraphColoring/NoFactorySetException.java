// NoFactorySetException.java, created Thu Jan 14 20:55:46 1999 by pnkfelix
package harpoon.Analysis.GraphColoring;

import harpoon.ClassFile.*;
/**
 * <code>NoFactorySetException</code>
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: NoFactorySetException.java,v 1.1.2.1 1999-01-15 02:09:39 pnkfelix Exp $
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
