// ColorableNode.java, created Wed Jan 13 14:12:37 1999 by pnkfelix
// Copyright (C) 1998 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.GraphColoring;

/**
 * <code>ColorableNode</code>
 *  
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: ColorableNode.java,v 1.1.2.10 2001-06-17 22:29:38 cananian Exp $
 */

public abstract class ColorableNode extends Node {
    
    protected Color color;
    
    // hidden tracks whether the node is visible in a graph or has
    // been temporarily removed for coloring purposes...
    private boolean hidden;

    public ColorableNode() {
	super();
	// leave 'color' set to null
    }
    
    /** Modifiability check.
	<BR> <B>effects:</B> If <code>this</code> is allowed to be
	                     modified, returns true.  Else returns
			     false. 
    */
    public boolean isModifiable() {
	return ! hidden;
    }

    /** Returns the color of <code>this</code>.
	<BR> <B>requires:</B> <code>this</code> has been colored.
	<BR> <B>effects:</B> If the color of <code>this</code> has
	                     been set, returns the <code>Color</code>
			     object representing the color of
			     <code>this</code>.
     */
    public Color getColor() throws NodeNotColoredException {
	if (color == null) {
	    throw new NodeNotColoredException(this + " is not colored.");
	} else {
	    return color;
	}
    }
    
    /** Sets the color of <code>this</code>.
	<BR> <B>requires:</B> <code>this</code> has not already been
	                      colored, unless <code>color</code> ==
			      null. 
	<BR> <B>effects:</B> Sets the color of <code>this</code> to
	                     <code>color</code>.  If
			     <code>color</code> is <code>null</code>,
			     then <code>this</code> is given an
			     'uncolored' state. 
     */
    void setColor(Color color) throws NodeAlreadyColoredException {
	if (this.color != null && 
	    color != null) {
	    throw new NodeAlreadyColoredException
		(this + " already has color " + this.color +
		 " and cannot be given color " + color);
	} else {
	    this.color = color;
	}
    }

    /** Sets the hidden field of <code>this</code>
	<BR> <B>modifies:</B> <code>this</code>
	<BR> <B>effects:</B> Puts <code>this</code> into a 'hidden' state. 
    */
    void setHidden( boolean hide ) {
	// modifies: this.hiidden
	hidden = hide;
    }   

    /** Hidden field accessor.
	<BR> <B>effects:</B> returns true if <code>this</code> is in a
	                     'hidden' state, false otherwise.
    */
    boolean isHidden() {
	return hidden;
    }   

}
