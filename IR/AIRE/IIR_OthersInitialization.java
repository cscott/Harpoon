// IIR_OthersInitialization.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_OthersInitialization</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_OthersInitialization.java,v 1.3 1998-10-11 00:32:23 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_OthersInitialization extends IIR_Expression
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_OTHERS_INITIALIZATION; }
    //CONSTRUCTOR:
    public IIR_OthersInitialization() { }
    //METHODS:  
    public void set_expression(IIR v)
    { _expression = v; }
 
    public IIR get_expression()
    { return _expression; }
 
    //MEMBERS:  

// PROTECTED:
    IIR _expression;
} // END class

