// ColorableNode.java, created Wed Jan 13 14:12:37 1999 by pnkfelix
package harpoon.Analysis.GraphColoring;

/**
 * <code>ColorableNode</code>
 *  
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: ColorableNode.java,v 1.1.2.6 1999-01-22 00:06:58 pnkfelix Exp $
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
	    throw new NodeNotColoredException(this + " is not colored.");
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
	<BR> modifies: <code>this.hidden</code>
	<BR> effects: Sets <code>this.hidden</code> to the value
	              <code>hide</code> 
    */
    void setHidden( boolean hide ) {
	hidden = hide;
    }   

    /** Hidden field accessor.
	<BR> effects: returns <code>this.hidden</code>
    */
    boolean isHidden() {
	return hidden;
    }   

    public abstract boolean equals(Object o);

    public abstract int hashCode();
}
