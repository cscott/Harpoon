// WrongNodeTypeException.java, created Wed Jan 13 16:36:07 1999 by pnkfelix
package harpoon.Analysis.GraphColoring;

import harpoon.ClassFile.*;
/**
 * <code>WrongNodeTypeException</code>
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: WrongNodeTypeException.java,v 1.1.2.1 1999-01-14 20:12:11 pnkfelix Exp $
 */

public class WrongNodeTypeException extends Exception {
    
    /** Creates a <code>WrongNodeTypeException</code>. */
    public WrongNodeTypeException() {
        super();
    }

    /** Creates a <code>WrongNodeTypeException</code>. */
    public WrongNodeTypeException(String s) {
        super(s);
    }
    
}
