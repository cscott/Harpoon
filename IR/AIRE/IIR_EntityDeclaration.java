// IIR_EntityDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_EntityDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_EntityDeclaration.java,v 1.3 1998-10-11 00:32:19 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_EntityDeclaration extends IIR_LibraryUnit
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_ENTITY_DECLARATION; }
    //CONSTRUCTOR:
    public IIR_EntityDeclaration() { }
    
    //METHODS:  
    public void set_last_analyzed_architecture(IIR_ArchitectureDeclaration architecture)
    { _last_analyzed_architecture = architecture; }
 
    public IIR_ArchitectureDeclaration get_last_analyzed_architecture()
    { return _last_analyzed_architecture; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_ArchitectureDeclaration _last_analyzed_architecture;
} // END class

