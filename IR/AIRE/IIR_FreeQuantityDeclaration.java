// IIR_FreeQuantityDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_FreeQuantityDeclaration</code> class.
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_FreeQuantityDeclaration.java,v 1.2 1998-10-11 00:32:20 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_FreeQuantityDeclaration extends IIR_QuantityDeclaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_FREE_QUANTITY_DECLARATION; }

    /** The constructor method initializes a terminal declaration with
     *  an unspecified source location, and unspecified declarator, and
     *  an unspecified nature. */
    public IIR_FreeQuantityDeclaration() { }
    //METHODS:  
    public IIR_NatureDefinition get_subnature_indication()
    { return _subnature_indication; }
    public void set_subnature_indication(IIR_NatureDefinition subnature)
    { _subnature_indication = subnature; }

    public IIR get_value()
    { return _value; }
    public void set_value(IIR value)
    { _value = value; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_NatureDefinition _subnature_indication;
    IIR _value;
} // END class

