// IIR_VariableDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_VariableDeclaration</code> class represents
 * variables which may take on a sequence of values as execution proceeds.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_VariableDeclaration.java,v 1.4 1998-10-11 01:25:04 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_VariableDeclaration extends IIR_ObjectDeclaration
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_VARIABLE_DECLARATION).
     * @return <code>IR_Kind.IR_VARIABLE_DECLARATION</code>
     */
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

