// UncolorableGraphException.java, created Wed Jan 13 14:22:42 1999 by pnkfelix
package harpoon.Analysis.GraphColoring;

/**
 * <code>UncolorableGraphException</code>
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: UncolorableGraphException.java,v 1.1.2.1 1999-01-14 20:12:11 pnkfelix Exp $
 */

public class UncolorableGraphException extends Exception {
    
    /** Creates a <code>UncolorableGraphException</code>. */
    public UncolorableGraphException() {
        super();
    }

    /** Creates a <code>UncolorableGraphException</code>. */
    public UncolorableGraphException(String s) {
        super(s);
    }
    
}
