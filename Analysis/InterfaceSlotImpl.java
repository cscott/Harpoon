// InterfaceSlotImpl.java, created Tue Jan 19 10:59:44 1999 by pnkfelix
package harpoon.Analysis;

import harpoon.ClassFile.HClass;
import harpoon.Backend.Maps.MethodMap;
import java.util.Enumeration;

/**
 * <code>InterfaceSlotImpl</code>
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: InterfaceSlotImpl.java,v 1.1.2.1 1999-01-19 16:07:07 pnkfelix Exp $
 */

public class InterfaceSlotImpl extends InterfaceSlotAllocation {
    
    /** Creates a <code>InterfaceSlotImpl</code>. */
    public InterfaceSlotImpl() {
        
    }
    
    /** Attempts to order the methods for the interface objects in
	<code>hclasses</code>.
	<BR> requires: <code>hclasses</code> is an
	<code>Enumeration</code> of <code>HClass</code> objects. 
	@see HClass
	<BR> modifies: <code>hclasses</code>
	<BR> effects: Iterates through <code>hclasses</code>,
	accumulating all of the interface-methods and returns a
	method->int mapping, where the integer returned represents the
	placement of the method.
    */
    public MethodMap sortMethods( Enumeration hclasses ) {
	return null;
    }
}
