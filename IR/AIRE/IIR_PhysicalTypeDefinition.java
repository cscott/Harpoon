// IIR_PhysicalTypeDefinition.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_PhysicalTypeDefinition</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_PhysicalTypeDefinition.java,v 1.4 1998-10-11 02:37:21 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_PhysicalTypeDefinition extends IIR_ScalarTypeDefinition
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_PHYSICAL_TYPE_DEFINITION).
     * @return <code>IR_Kind.IR_PHYSICAL_TYPE_DEFINITION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_PHYSICAL_TYPE_DEFINITION; }
    //CONSTRUCTOR:
    public IIR_PhysicalTypeDefinition() { }
    //METHODS:  
    public void set_primary_unit(IIR_PhysicalUnit primary_unit)
    { _primary_unit = primary_unit; }
 
    public IIR_PhysicalUnit get_primary_unit()
    { return _primary_unit; }
 
    //MEMBERS:  
    public IIR_UnitList units;

// PROTECTED:
    IIR_PhysicalUnit _primary_unit;
} // END class

