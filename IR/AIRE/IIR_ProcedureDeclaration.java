// IIR_ProcedureDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ProcedureDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ProcedureDeclaration.java,v 1.1 1998-10-10 07:53:40 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ProcedureDeclaration extends IIR_SubprogramDeclaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_PROCEDURE_DECLARATION
    //CONSTRUCTOR:
    public IIR_ProcedureDeclaration() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

