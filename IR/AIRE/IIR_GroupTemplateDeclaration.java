// IIR_GroupTemplateDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_GroupTemplateDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_GroupTemplateDeclaration.java,v 1.3 1998-10-11 00:32:20 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_GroupTemplateDeclaration extends IIR_Declaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_GROUP_TEMPLATE_DECLARATION; }
    //CONSTRUCTOR:
    public IIR_GroupTemplateDeclaration() { }
    //METHODS:  
    //MEMBERS:  
    public IIR_EntityClassEntryList entity_class_entry_list;

// PROTECTED:
} // END class

