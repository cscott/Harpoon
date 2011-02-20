// IIR_Signature.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_Signature</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_Signature.java,v 1.5 1998-10-11 02:37:23 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_Signature extends IIR_TypeDefinition
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_SIGNATURE).
     * @return <code>IR_Kind.IR_SIGNATURE</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_SIGNATURE; }
    //CONSTRUCTOR:
    public IIR_Signature() { }
    //METHODS:  
    public void set_return_type(IIR_TypeDefinition return_type)
    { _return_type = return_type; }
 
    public IIR_TypeDefinition get_return_type()
    { return _return_type; }
 
    //MEMBERS:  
    public IIR_DesignatorList argument_type_list;

// PROTECTED:
    IIR_TypeDefinition _return_type;
} // END class

