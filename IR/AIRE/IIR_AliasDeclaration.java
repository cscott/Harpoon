// IIR_AliasDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_AliasDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_AliasDeclaration.java,v 1.1 1998-10-10 07:53:31 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_AliasDeclaration extends IIR_Declaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_ALIAS_DECLARATION
    //CONSTRUCTOR:
    /** The constructor method initializes an alias declaration with
     *  an unspecified declarator, an unspecified subtype, and an
     *  unspecified name. */
    public IIR_AliasDeclaration() { }
    //METHODS:  
    public void set_subtype(IIR_TypeDefinition subtype)
    { _subtype = subtype; }
 
    public IIR_TypeDefinition get_subtype()
    { return _subtype; }
 
    public void set_name(IIR name)
    { _name = name; }
 
    public IIR get_name()
    { return _name; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_TypeDefinition _subtype = null;
    IIR _name = null;
} // END class

