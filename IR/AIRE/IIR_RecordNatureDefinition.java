// IIR_RecordNatureDefinition.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_RecordNatureDefinition</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_RecordNatureDefinition.java,v 1.4 1998-10-11 00:32:24 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_RecordNatureDefinition extends IIR_CompositeNatureDefinition
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_RECORD_NATURE_DEFINITION; }
    //CONSTRUCTOR:
    public IIR_RecordNatureDefinition() { }
    //METHODS:  
    //MEMBERS:  
    public IIR_ElementDeclarationList element_declarations;

// PROTECTED:
} // END class

