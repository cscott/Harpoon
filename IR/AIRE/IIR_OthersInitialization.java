// IIR_OthersInitialization.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_OthersInitialization</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_OthersInitialization.java,v 1.1 1998-10-10 07:53:39 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_OthersInitialization extends IIR_Expression
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_OTHERS_INITIALIZATION
    //CONSTRUCTOR:
    public IIR_OthersInitialization() { }
    //METHODS:  
    public void set_expression(IIR v)
    { _expression = v; }
 
    public IIR get_expression()
    { return _expression; }
 
    //MEMBERS:  

// PROTECTED:
    IIR _v;
} // END class

