// IIR_FreeQuantityDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_FreeQuantityDeclaration</code> class.
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_FreeQuantityDeclaration.java,v 1.1 1998-10-10 07:53:36 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_FreeQuantityDeclaration extends IIR_QuantityDeclaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_FREE_QUANTITY_DECLARATION

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

