// IIR_ScalarNatureDefinition.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * The <code>IIR_ScalarNatureDefinition</code> class represents predefined
 * methods, subprograms, and public data elements describing scalar natures.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ScalarNatureDefinition.java,v 1.5 1998-10-11 02:37:23 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_ScalarNatureDefinition extends IIR_NatureDefinition
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_SCALAR_NATURE_DEFINITION).
     * @return <code>IR_Kind.IR_SCALAR_NATURE_DEFINITION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_SCALAR_NATURE_DEFINITION; }
    //CONSTRUCTOR:
    public IIR_ScalarNatureDefinition() { }
    //METHODS:  
    public void set_across(IIR_NatureDefinition across)
    { _across = across; }
 
    public IIR_NatureDefinition get_across()
    { return _across; }
 
    public void set_through(IIR_NatureDefinition through)
    { _through = through; }
 
    public IIR_NatureDefinition get_through()
    { return _through; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_NatureDefinition _across;
    IIR_NatureDefinition _through;
} // END class

