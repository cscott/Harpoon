// IIR_ConditionalWaveform.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ConditionalWaveform</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ConditionalWaveform.java,v 1.1 1998-10-10 07:53:34 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ConditionalWaveform extends IIR_Tuple
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_CONDITIONAL_WAVEFORM
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

