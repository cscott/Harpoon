// IIR_VariableDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_VariableDeclaration</code> class represents
 * variables which may take on a sequence of values as execution proceeds.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_VariableDeclaration.java,v 1.3 1998-10-11 00:32:28 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_VariableDeclaration extends IIR_ObjectDeclaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_VARIABLE_DECLARATION; }
    
    //METHODS:  
    public void set_value(IIR value)
    { _value = value; }
    public IIR get_value()
    { return _value; }
 
    //MEMBERS:  

// PROTECTED:
    IIR _value;
} // END class

