// IIR_InterfaceDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_InterfaceDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_InterfaceDeclaration.java,v 1.6 1998-10-11 01:24:58 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_InterfaceDeclaration extends IIR_Declaration
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    
    
    //METHODS:  
    public void set_mode(IR_Mode mode)
    { _mode = mode; }
 
    public IR_Mode get_mode()
    { return _mode; }
 
    public void set_value(IIR value)
    { _value = value; }
 
    public IIR get_value()
    { return _value; }
 
    //MEMBERS:  
    public IIR_AttributeSpecificationList attributes;

// PROTECTED:
    IR_Mode _mode;
    IIR _value;
} // END class

