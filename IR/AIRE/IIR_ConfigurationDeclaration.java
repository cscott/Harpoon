// IIR_ConfigurationDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ConfigurationDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ConfigurationDeclaration.java,v 1.1 1998-10-10 07:53:34 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ConfigurationDeclaration extends IIR_LibraryUnit
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_CONFIGURATION_DECLARATION
    //CONSTRUCTOR:
    public IIR_ConfigurationDeclaration() { }
    //METHODS:  
    public void set_block_configuration(IIR_BlockConfiguration block_configuration)
    { _block_configuration = block_configuration; }
 
    public IIR_BlockConfiguration get_block_configuration()
    { return _block_configuration; }
 
    public void set_entity(IIR_EntityDeclaration entity)
    { _entity = entity; }
 
    public IIR_EntityDeclaration get_entity()
    { return _entity; }
 
    //MEMBERS:  
    IIR_DeclarationList configuration_declarative_part;

// PROTECTED:
    IIR_BlockConfiguration _block_configuration;
    IIR_EntityDeclaration _entity;
} // END class

