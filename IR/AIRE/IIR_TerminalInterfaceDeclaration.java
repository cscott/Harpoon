// IIR_TerminalInterfaceDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_TerminalInterfaceDeclaration</code> class.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_TerminalInterfaceDeclaration.java,v 1.1 1998-10-10 07:53:45 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_TerminalInterfaceDeclaration extends IIR_InterfaceDeclaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_TERMINAL_INTERFACE_DECLARATION
    //CONSTRUCTOR:
    public IIR_TerminalInterfaceDeclaration() { }
    //METHODS:  
    public IIR_NatureDefinition get_subnature_indication()
    { return _subnature_indication; }
    public void set_subnature_indication(IIR_NatureDefinition subnature)
    { _subnature_indication = subnature; }

    //MEMBERS:  

// PROTECTED:
    IIR_NatureDefinition _subnature_indication;
} // END class

