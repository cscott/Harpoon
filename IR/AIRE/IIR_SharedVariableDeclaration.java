// IIR_SharedVariableDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_SharedVariableDeclaration</code> class represents
 * variables which may take on a sequence of values, assigned from more
 * than one execution thread.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SharedVariableDeclaration.java,v 1.2 1998-10-10 09:21:39 cananian Exp $
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
    public void set_value (IIR value)
    { _value = value; }
    public IIR get_value()
    { return _value; }
 
    //MEMBERS:  

// PROTECTED:
    IIR _value;
} // END class

