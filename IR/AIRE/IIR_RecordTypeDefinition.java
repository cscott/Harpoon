// IIR_RecordTypeDefinition.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_RecordTypeDefinition</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_RecordTypeDefinition.java,v 1.2 1998-10-11 00:32:24 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_RecordTypeDefinition extends IIR_TypeDefinition
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_RECORD_TYPE_DEFINITION; }
    //CONSTRUCTOR:
    public IIR_RecordTypeDefinition() { }
    //METHODS:  
    //MEMBERS:  
    public IIR_ElementDeclarationList element_declarations;

// PROTECTED:
} // END class

