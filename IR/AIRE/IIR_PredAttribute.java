// IIR_PredAttribute.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_PredAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_PredAttribute.java,v 1.4 1998-10-11 02:37:21 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_PredAttribute extends IIR_Attribute
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_PRED_ATTRIBUTE).
     * @return <code>IR_Kind.IR_PRED_ATTRIBUTE</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_PRED_ATTRIBUTE; }
    //CONSTRUCTOR:
    public IIR_PredAttribute() { }

    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

