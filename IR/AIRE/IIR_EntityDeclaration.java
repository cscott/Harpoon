// IIR_EntityDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_EntityDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_EntityDeclaration.java,v 1.1 1998-10-10 07:53:35 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_EntityDeclaration extends IIR_LibraryUnit
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_ENTITY_DECLARATION
    //CONSTRUCTOR:
    public IIR_EntityDeclaration() { }
    
    //METHODS:  
    public void set_last_analyzed_architecture(IIR_ArchitectureDeclaration architecture)
    { _last_analyzed_architecture = architecture; }
 
    public IIR_ArchitectureDeclaration get_last_analyzed_architecture()
    { return _last_analyzed_architecture; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_ArchitectureDeclaration _architecture;
} // END class

