// IIR_ComponentDeclaration.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_ComponentDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ComponentDeclaration.java,v 1.4 1998-10-11 02:37:14 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ComponentDeclaration extends IIR_Declaration
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_COMPONENT_DECLARATION).
     * @return <code>IR_Kind.IR_COMPONENT_DECLARATION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_COMPONENT_DECLARATION; }
    //CONSTRUCTOR:
    public IIR_ComponentDeclaration() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

