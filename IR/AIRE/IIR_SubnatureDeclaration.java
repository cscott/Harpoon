// IIR_SubnatureDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SubnatureDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SubnatureDeclaration.java,v 1.1 1998-10-10 07:53:44 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SubnatureDeclaration extends IIR_Declaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_SUBNATURE_DECLARATION
    //CONSTRUCTOR:
    public IIR_SubnatureDeclaration() { }
    //METHODS:  
    public void set_subnature(IIR_SubnatureDefinition subnature)
    { _subnature = subnature; }
 
    public IIR_SubnatureDefinition get_subnature()
    { return _subnature; }
 
    //MEMBERS:  
    IIR_AttributeSpecificationList attributes;

// PROTECTED:
    IIR_SubnatureDefinition _subnature;
} // END class

