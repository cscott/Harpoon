// IIR_GroupTemplateDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_GroupTemplateDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_GroupTemplateDeclaration.java,v 1.2 1998-10-10 11:05:36 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_GroupTemplateDeclaration extends IIR_Declaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_GROUP_TEMPLATE_DECLARATION
    //CONSTRUCTOR:
    public IIR_GroupTemplateDeclaration() { }
    //METHODS:  
    //MEMBERS:  
    public IIR_EntityClassEntryList entity_class_entry_list;

// PROTECTED:
} // END class

