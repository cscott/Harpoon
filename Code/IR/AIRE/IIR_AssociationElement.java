// IIR_AssociationElement.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_AssociationElement</code>classes pair a formal
 * and (optional) actual.  During elaboration, a list of such
 * association elements serves to associate an actual value with a formal.
 * Association elements are derived into two sub-classes: associations where
 * the actual is an expression 
 * (<code>IIR_AssociationElementByExpression</code>) and associations where
 * the actual is open (<code>IIR_AssociationElementByOpen</code>).
 * Association elements are organized as individually allocated elements
 * of a list.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_AssociationElement.java,v 1.4 1998-10-11 02:37:13 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_AssociationElement extends IIR_Tuple
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    
    
    //METHODS:  
    public void set_formal(IIR formal)
    { _formal = formal; }
	
 
    public IIR get_formal()
    { return _formal; }
 
    //MEMBERS:  

// PROTECTED:
    IIR _formal;
} // END class

