// IIR_FunctionCall.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_FunctionCall</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_FunctionCall.java,v 1.2 1998-10-11 00:32:20 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_FunctionCall extends IIR_Expression
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_FUNCTION_CALL; }
    //CONSTRUCTOR:
    public IIR_FunctionCall() { }
    //METHODS:  
    public void set_implementation(IIR_SubprogramDeclaration implementation)
    { _implementation = implementation; }
 
    public IIR_SubprogramDeclaration get_implementation()
    { return _implementation; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_SubprogramDeclaration _implementation;
} // END class

