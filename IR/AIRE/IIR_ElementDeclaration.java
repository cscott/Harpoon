// IIR_ElementDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ElementDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ElementDeclaration.java,v 1.1 1998-10-10 07:53:35 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ElementDeclaration extends IIR_Declaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_ELEMENT_DECLARATION
    //CONSTRUCTOR:
    public IIR_ElementDeclaration() { }
    //METHODS:  
    public void set_subtype(IIR_TypeDefinition subtype)
    { _subtype = subtype; }
 
    public IIR_TypeDefinition get_subtype()
    { return _subtype; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_TypeDefinition _subtype;
} // END class

