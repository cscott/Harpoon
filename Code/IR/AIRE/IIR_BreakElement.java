// IIR_BreakElement.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_BreakElement</code> denotes a single choice
 * within an <code>IIR_BreakElementList</code>.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_BreakElement.java,v 1.4 1998-10-11 02:37:13 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_BreakElement extends IIR_Tuple
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_BREAK_ELEMENT).
     * @return <code>IR_Kind.IR_BREAK_ELEMENT</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_BREAK_ELEMENT; }
    //CONSTRUCTOR:
    public IIR_BreakElement() { }
    //METHODS:  
    public void set_quantity_name(IIR value)
    { _quantity_name = value; }
 
    public IIR get_quantity_name()
    { return _quantity_name; }
 
    public void set_expression(IIR value)
    { _expression = value; }
 
    public IIR get_expression()
    { return _expression; }
 
    //MEMBERS:  

// PROTECTED:
    IIR _quantity_name;
    IIR _expression;
} // END class

