// IIR_ConstantDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ConstantDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ConstantDeclaration.java,v 1.2 1998-10-11 00:32:18 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ConstantDeclaration extends IIR_ObjectDeclaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_CONSTANT_DECLARATION; }
    
    //METHODS:  
    public void set_value(IIR value)
    { _value = value; }
 
    public IIR get_value()
    { return _value; }
 
    //MEMBERS:  

// PROTECTED:
    IIR _value;
} // END class

