// IIR_BlockConfiguration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_BlockConfiguration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_BlockConfiguration.java,v 1.1 1998-10-10 07:53:32 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_BlockConfiguration extends IIR_ConfigurationItem
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_BLOCK_CONFIGURATION
    //CONSTRUCTOR:
    public IIR_BlockConfiguration() { }
    //METHODS:  
    public void set_block_specification(IIR block_specification)
    { _block_specification = block_specification; }
 
    public IIR get_block_specification()
    { return _block_specification; }
 
    //MEMBERS:  
    IIR_DeclarationList use_clause_list;
    IIR_ConfigurationItemList configuration_item_list;

// PROTECTED:
    IIR _block_specification;
} // END class

