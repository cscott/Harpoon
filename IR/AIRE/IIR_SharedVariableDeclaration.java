// IIR_SharedVariableDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SharedVariableDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SharedVariableDeclaration.java,v 1.1 1998-10-10 07:53:43 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SharedVariableDeclaration extends IIR_ObjectDeclaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_SHARED_VARIABLE_DECLARATION
    //CONSTRUCTOR:
    public IIR_SharedVariableDeclaration() { }

    //METHODS:  
    public IIR get_value()
    { return _value; }
 
    //MEMBERS:  

// PROTECTED:
} // END class

