// IIR_SignalDeclaration.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_SignalDeclaration</code> class represents
 * signals which may take on a sequence of values as execution proceeds.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SignalDeclaration.java,v 1.5 1998-10-11 02:37:23 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SignalDeclaration extends IIR_ObjectDeclaration
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_SIGNAL_DECLARATION).
     * @return <code>IR_Kind.IR_SIGNAL_DECLARATION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_SIGNAL_DECLARATION; }
    //CONSTRUCTOR:
    public IIR_SignalDeclaration() { }
    //METHODS:  
    public void set_value(IIR value)
    { _value = value; }
    public IIR get_value()
    { return _value; }
 
    public void set_signal_kind(IR_SignalKind signal_kind)
    { _signal_kind = signal_kind; }
    public IR_SignalKind get_signal_kind()
    { return _signal_kind; }
 
    //MEMBERS:  

// PROTECTED:
    IIR _value;
    IR_SignalKind _signal_kind;
} // END class

