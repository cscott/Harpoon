// IIR_ConditionalWaveform.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ConditionalWaveform</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ConditionalWaveform.java,v 1.3 1998-10-11 01:24:55 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ConditionalWaveform extends IIR_Tuple
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_CONDITIONAL_WAVEFORM).
     * @return <code>IR_Kind.IR_CONDITIONAL_WAVEFORM</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_CONDITIONAL_WAVEFORM; }
    //CONSTRUCTOR:
    public IIR_ConditionalWaveform() { }
    //METHODS:  
    public void set_condition(IIR condition)
    { _condition = condition; }
 
    public IIR get_condition()
    { return _condition; }
 
    //MEMBERS:  

// PROTECTED:
    IIR _condition;
} // END class

