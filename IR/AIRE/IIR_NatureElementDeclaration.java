// IIR_NatureElementDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_NatureElementDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_NatureElementDeclaration.java,v 1.1 1998-10-10 07:53:38 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_NatureElementDeclaration extends IIR_Declaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_NATURE_ELEMENT_DECLARATION
    //CONSTRUCTOR:
    public IIR_NatureElementDeclaration() { }
    //METHODS:  
    public void set_subnature(IIR_NatureDefinition subtype)
    { _subnature = subtype; }
 
    public IIR_NatureDefinition get_subnature()
    { return _subnature; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_NatureDefinition _subtype;
} // END class

