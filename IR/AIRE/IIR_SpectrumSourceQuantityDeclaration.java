// IIR_SpectrumSourceQuantityDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_SpectrumSourceQuantityDeclaration</code> class.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SpectrumSourceQuantityDeclaration.java,v 1.2 1998-10-10 09:21:39 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SpectrumSourceQuantityDeclaration extends IIR_QuantityDeclaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_SPECTRUM_SOURCE_QUANTITY_DECLARATION
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

