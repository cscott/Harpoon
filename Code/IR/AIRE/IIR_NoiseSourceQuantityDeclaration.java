// IIR_NoiseSourceQuantityDeclaration.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_NoiseSourceQuantityDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_NoiseSourceQuantityDeclaration.java,v 1.5 1998-10-11 02:37:20 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_NoiseSourceQuantityDeclaration extends IIR_QuantityDeclaration
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_NOISE_SOURCE_QUANTITY_DECLARATION).
     * @return <code>IR_Kind.IR_NOISE_SOURCE_QUANTITY_DECLARATION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_NOISE_SOURCE_QUANTITY_DECLARATION; }
    //CONSTRUCTOR:
    public IIR_NoiseSourceQuantityDeclaration() { }
    //METHODS:  
    public void set_subnature_indication(IIR_NatureDefinition subnature_indication)
    { _subnature_indication = subnature_indication; }
 
    public IIR_NatureDefinition get_subnature_indication()
    { return _subnature_indication; }
 
    public void set_magnitude_simple_expression(IIR value)
    { _magnitude_simple_expression = value; }
 
    public IIR get_magnitude_simple_expression()
    { return _magnitude_simple_expression; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_NatureDefinition _subnature_indication;
    IIR _magnitude_simple_expression;
} // END class

