// IIR_ConstantDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ConstantDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ConstantDeclaration.java,v 1.1 1998-10-10 07:53:34 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ConstantDeclaration extends IIR_ObjectDeclaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_CONSTANT_DECLARATION
    
    //METHODS:  
    public void set_value(IIR value)
    { _value = value; }
 
    public IIR get_value()
    { return _value; }
 
    //MEMBERS:  

// PROTECTED:
    IIR _value;
} // END class

