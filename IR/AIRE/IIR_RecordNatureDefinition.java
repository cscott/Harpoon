// IIR_RecordNatureDefinition.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_RecordNatureDefinition</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_RecordNatureDefinition.java,v 1.6 1998-10-11 02:37:22 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_RecordNatureDefinition extends IIR_CompositeNatureDefinition
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_RECORD_NATURE_DEFINITION).
     * @return <code>IR_Kind.IR_RECORD_NATURE_DEFINITION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_RECORD_NATURE_DEFINITION; }
    //CONSTRUCTOR:
    public IIR_RecordNatureDefinition() { }
    //METHODS:  
    //MEMBERS:  
    public IIR_ElementDeclarationList element_declarations;

// PROTECTED:
} // END class

