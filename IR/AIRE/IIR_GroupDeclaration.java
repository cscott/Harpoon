// IIR_GroupDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_GroupDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_GroupDeclaration.java,v 1.1 1998-10-10 07:53:36 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_GroupDeclaration extends IIR_Declaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_GROUP_DECLARATION
    //CONSTRUCTOR:
    public IIR_GroupDeclaration() { }
    //METHODS:  
    public void set_group_template(IIR_Name group_template_name)
    { _group_template = group_template_name; }
 
    public IIR_Name get_group_template_name()
    { return _group_template_name; }
 
    //MEMBERS:  
    IIR_DesignatorList group_constituent_list;
    IIR_AttributeSpecificationList attributes;

// PROTECTED:
    IIR_Name _group_template_name;
} // END class

