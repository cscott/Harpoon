// InterfaceSlotAllocation.java, created Mon Jan 18 15:08:49 1999 by pnkfelix
package harpoon.Analysis;

import harpoon.ClassFile.HClass;
import harpoon.Backend.Maps.MethodMap;
import java.util.Enumeration;

/**
 * <code>InterfaceSlotAllocation</code>
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: InterfaceSlotAllocation.java,v 1.1.2.2 1999-01-19 16:07:06 pnkfelix Exp $
 */

public abstract class InterfaceSlotAllocation  {
    
    /** Creates a <code>InterfaceSlotAllocation</code>. */
    public InterfaceSlotAllocation() {
        
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
    public abstract MethodMap sortMethods( Enumeration hclasses );    

}
