// IIR_PhysicalTypeDefinition.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_PhysicalTypeDefinition</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_PhysicalTypeDefinition.java,v 1.2 1998-10-11 00:32:23 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_PhysicalTypeDefinition extends IIR_ScalarTypeDefinition
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_PHYSICAL_TYPE_DEFINITION; }
    //CONSTRUCTOR:
    public IIR_PhysicalTypeDefinition() { }
    //METHODS:  
    public void set_primary_unit(IIR_PhysicalUnit primary_unit)
    { _primary_unit = primary_unit; }
 
    public IIR_PhysicalUnit get_primary_unit()
    { return _primary_unit; }
 
    //MEMBERS:  
    public IIR_UnitList units;

// PROTECTED:
    IIR_PhysicalUnit _primary_unit;
} // END class

