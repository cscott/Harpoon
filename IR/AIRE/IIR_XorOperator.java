// IIR_XorOperator.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_XorOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_XorOperator.java,v 1.4 1998-10-11 02:37:26 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_XorOperator extends IIR_DyadicOperator
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_XOR_OPERATOR).
     * @return <code>IR_Kind.IR_XOR_OPERATOR</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_XOR_OPERATOR; }
    //CONSTRUCTOR:
    public IIR_XorOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

