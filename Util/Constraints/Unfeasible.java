// Unfeasible.java, created Sun Mar 31 22:01:45 2002 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Constraints;

/** <code>Unfeasible</code> is an exception that is thrown when a
    system of inclusion constraints is unfeasible. 

    @author  Alexandru SALCIANU <salcianu@MIT.EDU>
    @version $Id: Unfeasible.java,v 1.1 2002-04-02 23:54:15 salcianu Exp $
*/
public class Unfeasible extends Exception {
    
    /** Creates a <code>Unfeasible</code>.
	@param <code>message</code> is the string attached to
	<code>this</code> exception; it is supposed to be some short
	explanation of the unfeasibility. */
    public Unfeasible(String message) {
	super(message);
    }
    
}
