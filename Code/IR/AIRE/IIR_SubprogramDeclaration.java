// IIR_SubprogramDeclaration.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_SubprogramDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SubprogramDeclaration.java,v 1.6 1998-10-11 02:37:24 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_SubprogramDeclaration extends IIR_Declaration
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    
    
    //METHODS:  
    //MEMBERS:  
    public IIR_InterfaceList interface_declarations;
    public IIR_DeclarationList subprogram_declarations;
    public IIR_SequentialStatementList subprogram_body;
    public IIR_AttributeSpecificationList attributes;

// PROTECTED:
} // END class

