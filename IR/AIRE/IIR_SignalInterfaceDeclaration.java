// IIR_SignalInterfaceDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SignalInterfaceDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SignalInterfaceDeclaration.java,v 1.2 1998-10-11 00:32:25 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SignalInterfaceDeclaration extends IIR_InterfaceDeclaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
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

