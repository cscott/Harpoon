// IIR_ValAttribute.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_ValAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ValAttribute.java,v 1.4 1998-10-11 02:37:25 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ValAttribute extends IIR_Attribute
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_VAL_ATTRIBUTE).
     * @return <code>IR_Kind.IR_VAL_ATTRIBUTE</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_VAL_ATTRIBUTE; }
    //CONSTRUCTOR:
    public IIR_ValAttribute() { }

    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

