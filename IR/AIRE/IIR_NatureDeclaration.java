// IIR_NatureDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_NatureDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_NatureDeclaration.java,v 1.1 1998-10-10 07:53:38 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_NatureDeclaration extends IIR_Declaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_NATURE_DECLARATION
    //CONSTRUCTOR:
    public IIR_NatureDeclaration() { }
    //METHODS:  
    public void set_nature(IIR_NatureeDefinition nature)
    { _nature = nature; }
 
    public IIR_NatureDefinition get_nature()
    { return _nature; }
 
    //MEMBERS:  
    IIR_AttributeSpecificationList attributes;

// PROTECTED:
    IIR_NatureeDefinition _nature;
} // END class

