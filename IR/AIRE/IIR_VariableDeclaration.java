// IIR_VariableDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_VariableDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_VariableDeclaration.java,v 1.1 1998-10-10 07:53:46 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_VariableDeclaration extends IIR_ObjectDeclaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_VARIABLE_DECLARATION
    
    //METHODS:  
    public IIR get_value()
    { return _value; }
 
    //MEMBERS:  

// PROTECTED:
} // END class

