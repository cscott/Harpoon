// ColorableNode.java, created Wed Jan 13 14:12:37 1999 by pnkfelix
package harpoon.Analysis.GraphColoring;

import harpoon.ClassFile.*;
/**
 * <code>ColorableNode</code>
 *  
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: ColorableNode.java,v 1.1.2.1 1999-01-14 20:12:11 pnkfelix Exp $
 */

public class ColorableNode extends Node {
    
    private Color color;
    
    // hidden tracks whether the node is visible in a graph or has
    // been temporarily removed for coloring purposes...
    private boolean hidden;

    public ColorableNode() {
	super();
	// leave 'color' set to null
    }
    
    /** Modifiability check.
	effects: if <code>this</code> is allowed to be modified,
	returns true.  Else returns false. 
    */
    public boolean isModifiable() {
	return ! hidden;
    }

    /** Returns the color of <code>this</code>.
     <BR>  effects: If the color of <code>this</code> has been set,
                    returns the <code>Color</code> object representing
		    the color of <code>this</code>. 
		    Else throws NodeNotColoredException.
     */
    public Color getColor() throws NodeNotColoredException {
	if (color == null) {
	    throw new NodeNotColoredException();
	} else {
	    return color;
	}
    }
    
    /** Sets the color of <code>this</code>.
	<BR> effects: Sets the color of <code>this</code> to
	              <code>color</code>.  If <code>color</code> is
		      <code>null</code> then <code>this</code> is
		      given an 'uncolored' state.
     */
    void setColor(Color color) {
	this.color = color;
    }

    /** Sets the hidden field of <code>this</code>
	<BR> modifies: <code>this.hidden</code>
	<BR> effects: Sets <code>this.hidden</code> to the value
	              <code>hide</code> 
    */
    protected void setHidden( boolean hide ) {
	hidden = hide;
    }   

    /** Hidden field accessor.
	<BR> effects: returns <code>this.hidden</code>
    */
    protected boolean isHidden() {
	return hidden;
    }   

    
}
