// IIR_BlockConfiguration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_BlockConfiguration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_BlockConfiguration.java,v 1.4 1998-10-11 01:24:53 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_BlockConfiguration extends IIR_ConfigurationItem
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_BLOCK_CONFIGURATION).
     * @return <code>IR_Kind.IR_BLOCK_CONFIGURATION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_BLOCK_CONFIGURATION; }
    //CONSTRUCTOR:
    public IIR_BlockConfiguration() { }
    //METHODS:  
    public void set_block_specification(IIR block_specification)
    { _block_specification = block_specification; }
 
    public IIR get_block_specification()
    { return _block_specification; }
 
    //MEMBERS:  
    public IIR_DeclarationList use_clause_list;
    public IIR_ConfigurationItemList configuration_item_list;

// PROTECTED:
    IIR _block_specification;
} // END class

