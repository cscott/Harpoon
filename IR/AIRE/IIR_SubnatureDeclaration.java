// IIR_SubnatureDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SubnatureDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SubnatureDeclaration.java,v 1.2 1998-10-10 09:21:39 cananian Exp $
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

    public void set_subnature(IIR_NatureDefinition subnature)
    { _subnature = subnature; }
 
    public IIR_NatureDefinition get_subnature()
    { return _subnature; }
 
    //MEMBERS:  
    public IIR_AttributeSpecificationList attributes;

// PROTECTED:
    IIR_NatureDefinition _subnature;
} // END class

