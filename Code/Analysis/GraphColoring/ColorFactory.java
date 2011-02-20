// ColorFactory.java, created Wed Jan 13 17:39:31 1999 by pnkfelix
// Copyright (C) 1998 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.GraphColoring;

import java.util.Stack;
import java.util.Vector;

/**
 * <code>ColorFactory</code>
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: ColorFactory.java,v 1.2 2002-02-25 20:57:15 cananian Exp $
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
	<BR> <B>modifies:</B> <code>this</code>
	<BR> <B>effects:</B> If no <code>Color</code>s have been
	                     removed from <code>this</code>,
			     constructs a new <code>Color</code> and
			     returns it.  Else returns the most
			     recently removed <code>Color</code> and
			     flags the <code>Color</code> returned as
			     no longer being removed from
			     <code>this</code>.
    */
    public Color makeColor() {
	// modifies: this.colors, this.removedColors
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
	<BR> <B>modifies:</B> <code>this</code>
	<BR> <B>effects:</B> If <code>Color</code>s exist in
	                     <code>this</code> for distribution,
			     removes the last produced
			     <code>Color</code> from the internal
			     colors list.  Else does nothing.
    */
    public void removeColor() {
	// modifies: this.colors, this.removedColors
	if (! colors.empty() ) {
	    removedColors.push( colors.pop() );
	}
    }

    /** Color generator that subclasses must implement.
	<BR> <B>effects:</B> Constructs a new <code>Color</code> and
	                     returns it.  
    */
    protected abstract Color newColor();

    /** Inventory accessor.
	<BR> <B>effects:</B> Returns a <code>Vector</code> of all of
	                     the <code>Color</code>s currently
			     distributable from <code>this</code>. 
	<BR> <B>requires:</B> <code>makeColor</code> and
	                      <code>removeColor</code> are not called
			      as long as the <code>Vector</code>
			      returned is in use.
    */
    public Vector getColors() { 
	return colors;
    }
}





