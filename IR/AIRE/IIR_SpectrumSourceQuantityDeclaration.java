// IIR_SpectrumSourceQuantityDeclaration.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_SpectrumSourceQuantityDeclaration</code> class.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SpectrumSourceQuantityDeclaration.java,v 1.5 1998-10-11 02:37:24 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SpectrumSourceQuantityDeclaration extends IIR_QuantityDeclaration
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_SPECTRUM_SOURCE_QUANTITY_DECLARATION).
     * @return <code>IR_Kind.IR_SPECTRUM_SOURCE_QUANTITY_DECLARATION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_SPECTRUM_SOURCE_QUANTITY_DECLARATION; }
    //CONSTRUCTOR:
    public IIR_SpectrumSourceQuantityDeclaration() { }
    //METHODS:  
    public void set_subnature_indication(IIR_NatureDefinition subnature_indication)
    { _subnature_indication = subnature_indication; }
    public IIR get_subnature_indication()
    { return _subnature_indication; }

    void set_magnitude_simple_expression(IIR value)
    { _magnitude_simple_expression = value; }
    public IIR get_magnitude_simple_expression()
    { return _magnitude_simple_expression; }
 
    public void set_phase_simple_expression(IIR value)
    { _phase_simple_expression = value; }
    public IIR get_phase_simple_expression()
    { return _phase_simple_expression; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_NatureDefinition _subnature_indication;
    IIR _magnitude_simple_expression;
    IIR _phase_simple_expression;
} // END class

