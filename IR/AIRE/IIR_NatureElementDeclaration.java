// IIR_NatureElementDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_NatureElementDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_NatureElementDeclaration.java,v 1.3 1998-10-11 00:32:22 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_NatureElementDeclaration extends IIR_Declaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_NATURE_ELEMENT_DECLARATION; }
    //CONSTRUCTOR:
    public IIR_NatureElementDeclaration() { }
    //METHODS:  
    public void set_subnature(IIR_NatureDefinition subnature)
    { _subnature = subnature; }
 
    public IIR_NatureDefinition get_subnature()
    { return _subnature; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_NatureDefinition _subnature;
} // END class

