// IIR_ScalarTypeDefinition.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_ScalarTypeDefinition</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ScalarTypeDefinition.java,v 1.5 1998-10-11 02:37:23 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_ScalarTypeDefinition extends IIR_TypeDefinition
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    
    
    //METHODS:  
    public void set_left(IIR left)
    { _left = left; }
 
    public IIR get_left()
    { return _left; }
 
    public void set_direction(IIR direction)
    { _direction = direction; }
 
    public IIR get_direction()
    { return _direction; }
 
    public void set_right(IIR right)
    { _right = right; }
 
    //MEMBERS:  

// PROTECTED:
    IIR _left;
    IIR _direction;
    IIR _right;
} // END class

