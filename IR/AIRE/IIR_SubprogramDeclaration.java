// IIR_SubprogramDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SubprogramDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SubprogramDeclaration.java,v 1.4 1998-10-11 00:32:27 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_SubprogramDeclaration extends IIR_Declaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    
    
    //METHODS:  
    //MEMBERS:  
    public IIR_InterfaceList interface_declarations;
    public IIR_DeclarationList subprogram_declarations;
    public IIR_SequentialStatementList subprogram_body;
    public IIR_AttributeSpecificationList attributes;

// PROTECTED:
} // END class

