// ColorFactory.java, created Wed Jan 13 17:39:31 1999 by pnkfelix
package harpoon.Analysis.GraphColoring;

import harpoon.ClassFile.*;
import java.util.Stack;
import java.util.Vector;

/**
 * <code>ColorFactory</code>
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: ColorFactory.java,v 1.1.2.2 1999-01-15 02:09:38 pnkfelix Exp $
 */

public abstract class ColorFactory  {
    
    private Stack colors;
    private Stack removedColors;

    /** Creates a <code>ColorFactory</code>. */
    public ColorFactory() {
        colors = new Stack();
	removedColors = new Stack();
    }
    
    /** Color generator.
	<BR> modifies: <code>this.colors</code>,
	               <code>this.removedColors</code> 
	<BR> effects: If <code>this.removedColors</code> is empty,
	              constructs a new <code>Color</code>, adds it to
		      the internal list of generated colors, and
		      returns it.  
		      Else returns the most recently removed
		      <code>Color</code> and removes it from
		      <code>this.removedColors</code> 
    */
    public Color makeColor() {
	Color c;
	if (removedColors.empty()) {
	    c = newColor();	
	} else {
	    c = (Color) removedColors.pop();
	}
	colors.push(c);
	return c;
    }

    /** Factory downsizer.
	<BR> modifies: <code>this.colors</code>, 
	               <code>this.removedColors</code>
	<BR> effects: If <code>this.colors</code> is not empty, 
	              Removes the last produced <code>Color</code>
	              from the internal colors list.  The factory is 
		      required to reproduce the colors removed on
		      subsequent calls to <code>newColor</code>.
		      Else does nothing.
    */
    public void removeColor() {
	if (! colors.empty() ) {
	    removedColors.push( colors.pop() );
	}
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
