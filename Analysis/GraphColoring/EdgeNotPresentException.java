// EdgeNotPresentException.java, created Wed Jan 13 18:13:13 1999 by pnkfelix
package harpoon.Analysis.GraphColoring;

/**
 * <code>EdgeNotPresentException</code>
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: EdgeNotPresentException.java,v 1.1.2.2 1999-01-19 16:07:59 pnkfelix Exp $
 */

public class EdgeNotPresentException extends Exception {
    
    /** Creates a <code>EdgeNotPresentException</code>. */
    public EdgeNotPresentException() {
        super();
    }

    /** Creates a <code>EdgeNotPresentException</code>. */
    public EdgeNotPresentException(String s) {
        super(s);
    }
    
}
