// ColorFactory.java, created Wed Jan 13 17:39:31 1999 by pnkfelix
package harpoon.Analysis.GraphColoring;

import harpoon.ClassFile.*;
import java.util.Vector;
import java.util.Enumeration;

/**
 * <code>ColorFactory</code>
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: ColorFactory.java,v 1.1.2.1 1999-01-14 20:15:29 pnkfelix Exp $
 */

public abstract class ColorFactory  {
    
    private Vector colors;

    /** Creates a <code>ColorFactory</code>. */
    public ColorFactory() {
        colors = new Vector();
    }
    
    /** Color generator.
	<BR> modifies: <code>this.colors</code>
	<BR> effects: Constructs a new <code>Color</code>, adds it to
	the internal list of generated colors, and returns it. 
    */
    public Color makeColor() {
	Color c = newColor();
	colors.addElement(c);
	return c;
    }
    
    /** Color generator that subclasses must implement.
	<BR> effects: Constructs a new <code>Color</code> and returns
	              it.  
    */
    protected abstract Color newColor();

    public Vector getColors() {
	return colors;
    }
}
