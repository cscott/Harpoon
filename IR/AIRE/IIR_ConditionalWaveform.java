// IIR_ConditionalWaveform.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ConditionalWaveform</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ConditionalWaveform.java,v 1.2 1998-10-11 00:32:18 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ConditionalWaveform extends IIR_Tuple
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
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

