// IIR_QuantityInterfaceDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_QuantityInterfaceDeclaration</code> class.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_QuantityInterfaceDeclaration.java,v 1.2 1998-10-11 00:32:24 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_QuantityInterfaceDeclaration extends IIR_InterfaceDeclaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_QUANTITY_INTERFACE_DECLARATION; }
    //CONSTRUCTOR:
    public IIR_QuantityInterfaceDeclaration() { }
    //METHODS:  
    public IIR_NatureDefinition get_subnature_indication()
    { return _subnature_indication; }
    public void set_subnature_indication(IIR_NatureDefinition subnature)
    { _subnature_indication = subnature; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_NatureDefinition _subnature_indication;
} // END class

