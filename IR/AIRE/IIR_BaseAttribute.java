// IIR_BaseAttribute.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_BaseAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_BaseAttribute.java,v 1.4 1998-10-11 02:37:13 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_BaseAttribute extends IIR_Attribute
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_BASE_ATTRIBUTE).
     * @return <code>IR_Kind.IR_BASE_ATTRIBUTE</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_BASE_ATTRIBUTE; }
    //CONSTRUCTOR:
    public IIR_BaseAttribute() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

