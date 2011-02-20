// IIR_WaveformElement.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_WaveformElement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_WaveformElement.java,v 1.4 1998-10-11 02:37:25 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_WaveformElement extends IIR_Tuple
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_WAVEFORM_ELEMENT).
     * @return <code>IR_Kind.IR_WAVEFORM_ELEMENT</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_WAVEFORM_ELEMENT; }
    //CONSTRUCTOR:
    public IIR_WaveformElement() { }
    //METHODS:  
    public void set_value(IIR value)
    { _value = value; }
 
    public IIR get_value()
    { return _value; }
 
    public void set_time(IIR time)
    { _time = time; }
 
    public IIR get_time()
    { return _time; }
 
    //MEMBERS:  

// PROTECTED:
    IIR _value;
    IIR _time;
} // END class

