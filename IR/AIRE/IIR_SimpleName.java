// IIR_SimpleName.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SimpleName</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SimpleName.java,v 1.2 1998-10-11 00:32:25 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SimpleName extends IIR_Name
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
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

