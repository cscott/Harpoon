// WrongNodeTypeException.java, created Wed Jan 13 16:36:07 1999 by pnkfelix
package harpoon.Analysis.GraphColoring;

/**
 * <code>WrongNodeTypeException</code>
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: WrongNodeTypeException.java,v 1.1.2.3 1999-01-19 23:51:13 pnkfelix Exp $
 */

public class WrongNodeTypeException extends RuntimeException {
    
    /** Creates a <code>WrongNodeTypeException</code>. */
    public WrongNodeTypeException() {
        super();
    }

    /** Creates a <code>WrongNodeTypeException</code>. */
    public WrongNodeTypeException(String s) {
        super(s);
    }
    
}
