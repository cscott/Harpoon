// IIR_SignalInterfaceDeclaration.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_SignalInterfaceDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SignalInterfaceDeclaration.java,v 1.4 1998-10-11 02:37:23 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SignalInterfaceDeclaration extends IIR_InterfaceDeclaration
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_SIGNAL_INTERFACE_DECLARATION).
     * @return <code>IR_Kind.IR_SIGNAL_INTERFACE_DECLARATION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_SIGNAL_INTERFACE_DECLARATION; }
    //CONSTRUCTOR:
    public IIR_SignalInterfaceDeclaration() { }
    //METHODS:  
    public void set_signal_kind(IR_SignalKind signal_kind)
    { _signal_kind = signal_kind; }
 
    public IR_SignalKind get_signal_kind()
    { return _signal_kind; }
 
    //MEMBERS:  

// PROTECTED:
    IR_SignalKind _signal_kind;
} // END class

