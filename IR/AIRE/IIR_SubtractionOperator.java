// IIR_SubtractionOperator.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_SubtractionOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SubtractionOperator.java,v 1.4 1998-10-11 02:37:24 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SubtractionOperator extends IIR_DyadicOperator
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_SUBTRACTION_OPERATOR).
     * @return <code>IR_Kind.IR_SUBTRACTION_OPERATOR</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_SUBTRACTION_OPERATOR; }
    //CONSTRUCTOR:
    public IIR_SubtractionOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

