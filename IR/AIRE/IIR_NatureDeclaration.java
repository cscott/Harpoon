// IIR_NatureDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_NatureDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_NatureDeclaration.java,v 1.4 1998-10-11 00:32:22 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_NatureDeclaration extends IIR_Declaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_NATURE_DECLARATION; }
    //CONSTRUCTOR:
    public IIR_NatureDeclaration() { }
    //METHODS:  
    public void set_nature(IIR_NatureDefinition nature)
    { _nature = nature; }
 
    public IIR_NatureDefinition get_nature()
    { return _nature; }
 
    //MEMBERS:  
    public IIR_AttributeSpecificationList attributes;

// PROTECTED:
    IIR_NatureDefinition _nature;
} // END class

