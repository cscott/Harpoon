// IIR_RecordNatureDefinition.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_RecordNatureDefinition</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_RecordNatureDefinition.java,v 1.1 1998-10-10 07:53:41 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_RecordNatureDefinition extends IIR_CompositeNatureDefinition
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_RECORD_NATURE_DEFINITION
    //CONSTRUCTOR:
    public IIR_RecordNatureDefinition() { }
    //METHODS:  
    //MEMBERS:  
    IIR_ElementDeclarationList element_declarations;

// PROTECTED:
} // END class

