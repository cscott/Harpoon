// IIR_SubprogramDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SubprogramDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SubprogramDeclaration.java,v 1.2 1998-10-10 09:58:35 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_SubprogramDeclaration extends IIR_Declaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = 
    
    
    //METHODS:  
    //MEMBERS:  
    IIR_InterfaceList interface_declarations;
    IIR_DeclarationList subprogram_declarations;
    IIR_SequentialStatementList subprogram_body;
    IIR_AttributeSpecificationList attributes;

// PROTECTED:
} // END class

