// IIR_Choice.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_Choice</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_Choice.java,v 1.4 1998-10-11 02:37:14 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_Choice extends IIR_Tuple
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_CHOICE).
     * @return <code>IR_Kind.IR_CHOICE</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_CHOICE; }
    //CONSTRUCTOR:
    public IIR_Choice( ) { }
    //METHODS:  
    public void set_value(IIR value)
    { _value = value; }
 
    public IIR get_value()
    { return _value; }
 
    //MEMBERS:  

// PROTECTED:
    IIR _value;
} // END class

