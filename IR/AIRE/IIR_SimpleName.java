// IIR_SimpleName.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SimpleName</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SimpleName.java,v 1.3 1998-10-11 01:25:02 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SimpleName extends IIR_Name
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_SIMPLE_NAME).
     * @return <code>IR_Kind.IR_SIMPLE_NAME</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_SIMPLE_NAME; }
    //CONSTRUCTOR:
    public IIR_SimpleName() { }
    //METHODS:  
    public void set_name(IIR_TextLiteral name)
    { _name = name; }
 
    public IIR_TextLiteral get_name()
    { return _name; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_TextLiteral _name;
} // END class

