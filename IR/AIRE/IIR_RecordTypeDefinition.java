// IIR_RecordTypeDefinition.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_RecordTypeDefinition</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_RecordTypeDefinition.java,v 1.4 1998-10-11 02:37:22 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_RecordTypeDefinition extends IIR_TypeDefinition
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_RECORD_TYPE_DEFINITION).
     * @return <code>IR_Kind.IR_RECORD_TYPE_DEFINITION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_RECORD_TYPE_DEFINITION; }
    //CONSTRUCTOR:
    public IIR_RecordTypeDefinition() { }
    //METHODS:  
    //MEMBERS:  
    public IIR_ElementDeclarationList element_declarations;

// PROTECTED:
} // END class

